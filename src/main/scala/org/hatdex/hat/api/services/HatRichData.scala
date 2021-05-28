/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package org.hatdex.hat.api.services

import io.dataswift.models.hat.{ EndpointData, ErrorMessage }
import org.hatdex.hat.api.services.Errors.{ ApiException, DuplicateDataException, UnauthorizedActionException }
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{ JsArray, JsError, JsSuccess, Json }
import play.api.libs.ws._

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

trait HatRichData {
  protected val logger: Logger
  protected val ws: WSClient
  protected val schema: String
  protected val hatAddress: String
  protected val apiVersion: String
  protected val host: String = if (hatAddress.isEmpty) "mock" else hatAddress

  import io.dataswift.models.hat.json.RichDataJsonFormats._

  def saveData(
      access_token: String,
      namespace: String,
      endpoint: String,
      data: JsArray,
      skipErrors: Boolean = false
    )(implicit ec: ExecutionContext): Future[Seq[EndpointData]] = {

    val request: WSRequest = ws
      .url(s"$schema$hatAddress/api/$apiVersion/data/$namespace/$endpoint")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)
      .withQueryStringParameters("skipErrors" -> skipErrors.toString)

    val futureResponse: Future[WSResponse] = request.post(data)

    futureResponse.flatMap { response =>
      response.status match {
        case CREATED =>
          response.json.validate[Seq[EndpointData]] match {
            case s: JsSuccess[Seq[EndpointData]] => Future.successful(s.get)
            case e: JsError =>
              logger.error(s"Error parsing response from a successful data records post: $e")
              Future.failed(new ApiException(s"Error parsing response from a successful data records post: $e"))
          }
        case FORBIDDEN =>
          Future.failed(
            UnauthorizedActionException(
              s"Saving data for hat $hatAddress, namespsace $namespace, endpoint $endpoint forbidden"
            )
          )
        case BAD_REQUEST =>
          response.json.validate[ErrorMessage] match {
            case s: JsSuccess[ErrorMessage] if s.get.cause.startsWith("Duplicate data") =>
              Future.failed(DuplicateDataException("Duplicate data"))
            case s: JsSuccess[ErrorMessage] => Future.failed(new ApiException(s.get.message))
            case e: JsError                 => Future.failed(new ApiException(s"Error deserializing Error Response: ${e.errors}"))
          }
        case _ =>
          logger.error(s"Creating new records for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Creating new records for $hatAddress failed unexpectedly"))
      }
    }
  }

  def saveData(
      access_token: String,
      data: Seq[EndpointData]
    )(implicit ec: ExecutionContext): Future[Seq[EndpointData]] = {
    val request: WSRequest = ws
      .url(s"$schema$hatAddress/api/$apiVersion/data-batch")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.post(Json.toJson(data))

    futureResponse.flatMap { response =>
      response.status match {
        case CREATED =>
          response.json.validate[Seq[EndpointData]] match {
            case s: JsSuccess[Seq[EndpointData]] => Future.successful(s.get)
            case e: JsError =>
              logger.error(s"Error parsing response from a successful data records post: $e")
              Future.failed(new ApiException(s"Error parsing response from a successful data records post: $e"))
          }
        case FORBIDDEN =>
          Future.failed(UnauthorizedActionException(s"Saving data for hat $hatAddress forbidden"))
        case BAD_REQUEST =>
          response.json.validate[ErrorMessage] match {
            case s: JsSuccess[ErrorMessage] if s.get.cause.startsWith("Duplicate data") =>
              Future.failed(DuplicateDataException("Duplicate data"))
            case s: JsSuccess[ErrorMessage] => Future.failed(new ApiException(s.get.message))
            case e: JsError                 => Future.failed(new ApiException(s"Error deserializing Error Response: ${e.errors}"))
          }
        case _ =>
          logger.error(s"Creating new records for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Creating new records for $hatAddress failed unexpectedly"))
      }
    }
  }

  def getData(
      access_token: String,
      namespace: String,
      endpoint: String,
      recordId: Option[UUID] = None,
      orderBy: Option[String] = None,
      orderingDescending: Boolean = false,
      skip: Option[Int] = None,
      take: Option[Int] = None
    )(implicit ec: ExecutionContext): Future[Seq[EndpointData]] = {

    val queryParameter = Seq(
      recordId.map(r => "recordId" -> r.toString),
      orderBy.map(r => "orderBy" -> r),
      if (orderingDescending) Some("ordering" -> "descending") else None,
      skip.map(r => "skip" -> r.toString),
      take.map(r => "take" -> r.toString)
    ).flatten

    val request: WSRequest = ws
      .url(s"$schema$hatAddress/api/$apiVersion/data/$namespace/$endpoint")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)
      .withQueryStringParameters(queryParameter: _*)

    val futureResponse: Future[WSResponse] = request.get()

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          response.json.validate[Seq[EndpointData]] match {
            case s: JsSuccess[Seq[EndpointData]] => Future.successful(s.get)
            case e: JsError =>
              val message = s"Error parsing response from a successful data request: $e"
              logger.error(message)
              Future.failed(new ApiException(message))
          }
        case FORBIDDEN =>
          Future.failed(
            UnauthorizedActionException(
              s"Retrieving data from $hatAddress, namespsace $namespace, endpoint $endpoint unauthorized"
            )
          )
        case _ =>
          logger.error(s"Retrieving records for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Retrieving records for $hatAddress failed unexpectedly"))
      }
    }
  }

}
