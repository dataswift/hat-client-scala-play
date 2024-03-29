/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package io.dataswift.hat.api.services

import io.dataswift.hat.api.HatWsClient
import io.dataswift.models.hat.{ EndpointData, ErrorMessage }
import io.dataswift.hat.api.services.Errors.{ ApiException, DuplicateDataException, UnauthorizedActionException }
import play.api.http.Status._
import play.api.libs.json.{ JsArray, JsError, JsSuccess, Json }
import play.api.libs.ws._

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

trait HatRichData extends HatWsClient {
  import io.dataswift.models.hat.json.RichDataJsonFormats._

  def saveData(
      accessToken: String,
      namespace: String,
      endpoint: String,
      data: JsArray,
      skipErrors: Boolean = false
    )(implicit ec: ExecutionContext): Future[Seq[EndpointData]] = {

    val request: WSRequest = ws
      .url(s"$baseUrlWithPath/data/$namespace/$endpoint")
      .withHttpHeaders(jsonHeader, customAuthHeader -> accessToken)
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
              s"Saving data for hat $baseUrl, namespsace $namespace, endpoint $endpoint forbidden"
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
          logger.error(s"Creating new records for $baseUrl failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Creating new records for $baseUrl failed unexpectedly"))
      }
    }
  }

  def saveData(
      accessToken: String,
      data: Seq[EndpointData]
    )(implicit ec: ExecutionContext): Future[Seq[EndpointData]] = {
    val request: WSRequest = ws
      .url(s"$baseUrlWithPath/data-batch")
      .withHttpHeaders(jsonHeader, customAuthHeader -> accessToken)

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
          Future.failed(UnauthorizedActionException(s"Saving data for hat $baseUrl forbidden"))
        case BAD_REQUEST =>
          response.json.validate[ErrorMessage] match {
            case s: JsSuccess[ErrorMessage] if s.get.cause.startsWith("Duplicate data") =>
              Future.failed(DuplicateDataException("Duplicate data"))
            case s: JsSuccess[ErrorMessage] => Future.failed(new ApiException(s.get.message))
            case e: JsError                 => Future.failed(new ApiException(s"Error deserializing Error Response: ${e.errors}"))
          }
        case _ =>
          logger.error(s"Creating new records for $baseUrl failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Creating new records for $baseUrl failed unexpectedly"))
      }
    }
  }

  def getData(
      accessToken: String,
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
      .url(s"$baseUrlWithPath/data/$namespace/$endpoint")
      .withHttpHeaders(jsonHeader, customAuthHeader -> accessToken)
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
              s"Retrieving data from $baseUrl, namespsace $namespace, endpoint $endpoint unauthorized"
            )
          )
        case _ =>
          logger.error(s"Retrieving records for $baseUrl failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Retrieving records for $baseUrl failed unexpectedly"))
      }
    }
  }

}
