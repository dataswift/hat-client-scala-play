/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package org.hatdex.hat.api.services

import io.dataswift.models.hat.applications.HatApplication
import org.hatdex.hat.api.services.Errors.{ ApiException, UnauthorizedActionException }
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{ JsError, JsSuccess }
import play.api.libs.ws._

import scala.concurrent.{ ExecutionContext, Future }

trait HatApplications extends ServiceHelper {
  protected val logger: Logger
  protected val ws: WSClient
  protected val username: String
  protected val serviceDomain: String
  protected val region: String
  protected val apiVersion: String
  protected val serviceUrl: String

  import io.dataswift.models.hat.json.ApplicationJsonProtocol._

  def getAllApplications(accessToken: String)(implicit ec: ExecutionContext): Future[Seq[HatApplication]] = {
    val request: WSRequest = ws
      .url(s"$serviceUrl/api/$apiVersion/applications")
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
          Future.failed(UnauthorizedActionException(s"Getting applications for hat $username forbidden"))
        case _ =>
          logger.error(s"Listing applications for $username failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Listing applications for $username failed unexpectedly"))
      }
    }
  }

  def enableApplication(
      accessToken: String,
      applicationId: String
    )(implicit ec: ExecutionContext): Future[Boolean] = {
    implicit val request: WSRequest = ws
      .url(s"$serviceUrl/api/$apiVersion/applications/$applicationId/setup")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> accessToken)

    transitionApplication
  }

  def disableApplication(
      accessToken: String,
      applicationId: String
    )(implicit ec: ExecutionContext): Future[Boolean] = {
    implicit val request: WSRequest = ws
      .url(s"$serviceUrl/api/$apiVersion/applications/$applicationId/disable")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> accessToken)

    transitionApplication
  }

  def getApplicationToken(
      accessToken: String,
      applicationId: String
    )(implicit ec: ExecutionContext): Future[String] = {
    val request: WSRequest = ws
      .url(s"$serviceUrl/api/$apiVersion/applications/$applicationId/access-token")
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
          Future.failed(UnauthorizedActionException(s"Getting applications for hat $username forbidden"))
        case _ =>
          logger.error(s"Listing applications for $username failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Listing applications for $username failed unexpectedly"))
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
          Future.failed(UnauthorizedActionException(s"Getting applications for hat $username forbidden"))
        case _ =>
          logger.error(s"Listing applications for $username failed, $response, ${response.body}")
          Future.failed(new ApiException(s"Listing applications for $username failed unexpectedly"))
      }
    }
  }

}
