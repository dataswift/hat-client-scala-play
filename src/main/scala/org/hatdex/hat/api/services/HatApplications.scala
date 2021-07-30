/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package org.hatdex.hat.api.services

import io.dataswift.models.hat.HatService
import io.dataswift.models.hat.applications.HatApplication
import org.hatdex.hat.api.services.Errors.{ApiException, UnauthorizedActionException}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws._

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait HatApplications {
  protected val logger: Logger
  protected val ws: WSClient
  protected val hatAddress: String
  protected val apiVersion: String
  protected val host: String = if (hatAddress.isEmpty) "mock" else new URL(hatAddress).getHost

  import io.dataswift.models.hat.json.HatJsonFormats._
  import io.dataswift.models.hat.json.ApplicationJsonProtocol._

  @Deprecated
  def getApplications(access_token: String)(implicit ec: ExecutionContext): Future[Seq[HatService]] = {

    val request: WSRequest = ws
      .url(s"$hatAddress/api/v2/application")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.get()

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          response.json.validate[Seq[HatService]] match {
            case s: JsSuccess[Seq[HatService]] => Future.successful(s.get)
            case e: JsError =>
              logger.error(s"Error parsing Application listing response: $e")
              Future.failed(new ApiException(s"Error parsing Application listing response: $e"))
          }
        case FORBIDDEN =>
          Future.failed(UnauthorizedActionException(s"Getting applications for hat $hatAddress forbidden"))
        case _ =>
          logger.error(s"Listing applications for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Listing applications for $hatAddress failed unexpectedly"))
      }
    }
  }

  @Deprecated
  def saveApplication(
      access_token: String,
      application: HatService
    )(implicit ec: ExecutionContext): Future[HatService] = {
    val request: WSRequest = ws
      .url(s"$hatAddress/api/v2/application")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.post(Json.toJson(application))

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          response.json.validate[HatService] match {
            case s: JsSuccess[HatService] => Future.successful(s.get)
            case e: JsError =>
              logger.error(s"Error parsing response from a successful application update: $e")
              Future.failed(new ApiException(s"Error parsing response from a successful application update: $e"))
          }
        case FORBIDDEN =>
          Future.failed(UnauthorizedActionException(s"Saving application for hat $hatAddress forbidden"))
        case _ =>
          logger.error(s"Saving application for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Saving application for $hatAddress failed unexpectedly"))
      }
    }
  }

  def getAllApplications(accessToken: String)(implicit ec: ExecutionContext): Future[Seq[HatApplication]] = {
    val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/applications")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> accessToken)

    val eventualResponse: Future[WSResponse] = request.get()

    eventualResponse.flatMap { response =>
      response.status match {
        case OK =>
          response.json.validate[Seq[HatApplication]] match {
            case s: JsSuccess[Seq[HatApplication]] => Future.successful(s.get)
            case e: JsError =>
              logger.error(s"Error parsing Application listing response: $e")
              Future.failed(new ApiException(s"Error parsing Application listing response: $e"))
          }
        case FORBIDDEN =>
          Future.failed(UnauthorizedActionException(s"Getting applications for hat $hatAddress forbidden"))
        case _ =>
          logger.error(s"Listing applications for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Listing applications for $hatAddress failed unexpectedly"))
      }
    }
  }

  def enableApplication(
      accessToken: String,
      applicationId: String
    )(implicit ec: ExecutionContext): Future[Boolean] = {
    implicit val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/applications/$applicationId/setup")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> accessToken)

    transitionApplication
  }

  def disableApplication(
      accessToken: String,
      applicationId: String
    )(implicit ec: ExecutionContext): Future[Boolean] = {
    implicit val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/applications/$applicationId/disable")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> accessToken)

    transitionApplication
  }

  def getApplicationToken(
      accessToken: String,
      applicationId: String
    )(implicit ec: ExecutionContext): Future[String] = {
    val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/applications/$applicationId/access-token")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> accessToken)

    val eventualResponse: Future[WSResponse] = request.get()

    eventualResponse.flatMap { response =>
      response.status match {
        case OK =>
          (response.json \ "accessToken").asOpt[String] match {
            case Some(token) => Future.successful(token)
            case None =>
              Future.failed(new ApiException("Unable to parse application token"))
          }
        case FORBIDDEN =>
          Future.failed(UnauthorizedActionException(s"Getting applications for hat $hatAddress forbidden"))
        case _ =>
          logger.error(s"Listing applications for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Listing applications for $hatAddress failed unexpectedly"))
      }
    }
  }

  private def transitionApplication(
      implicit
      ec: ExecutionContext,
      request: WSRequest): Future[Boolean] = {
    val eventualResponse: Future[WSResponse] = request.get()

    eventualResponse.flatMap { response =>
      response.status match {
        case OK =>
          Future.successful(true)
        //          response.json.validate[HatApplication] match {
        //            case _: JsSuccess[HatApplication] => Future.successful(true)
        //            case e: JsError =>
        //              logger.error(s"Error parsing Application enable response: $e")
        //              Future.failed(new ApiException(s"Error parsing Application listing response: $e"))
        //          }
        case BAD_REQUEST =>
          Future.failed(new ApiException("Invalid application ID"))
        case UNAUTHORIZED =>
          Future.failed(new ApiException(s"Invalid authentication token"))
        case FORBIDDEN =>
          Future.failed(UnauthorizedActionException(s"Getting applications for hat $hatAddress forbidden"))
        case _ =>
          logger.error(s"Listing applications for $hatAddress failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Listing applications for $hatAddress failed unexpectedly"))
      }
    }
  }

}
