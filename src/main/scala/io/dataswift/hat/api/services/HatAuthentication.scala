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
import io.dataswift.models.hat.json.HatJsonFormats
import io.dataswift.models.hat.{ PdaEmailVerificationRequest, User }
import io.dataswift.hat.api.services.Errors.ApiException
import play.api.http.Status._
import play.api.i18n.Lang
import play.api.libs.json._
import play.api.libs.ws._

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }

trait HatAuthentication extends HatWsClient {
  import HatJsonFormats._
  import io.dataswift.models.hat.json.ApiAuthenticationFormats._

  def retrievePublicKey()(implicit ec: ExecutionContext): Future[String] = {
    val request: WSRequest = ws
      .url(s"$baseUrl/publickey")
      .withHttpHeaders("Accept" -> "text/plain")

    request.get().map { response =>
      logger.debug(s"Hat $baseUrl public key response: $response")
      response.status match {
        case OK => response.body
        case _  => throw new RuntimeException("Public Key could not be retrieved")
      }
    }
  }

  def authenticateForToken(
      username: String,
      password: String
    )(implicit ec: ExecutionContext): Future[String] = {
    val request: WSRequest = ws
      .url(s"$baseUrl/users/access_token")
      .withHttpHeaders(jsonHeader, "username" -> username, "password" -> password)

    logger.debug(s"Authenticate for token with HAT at ${request.method} ${request.url} (headers: ${request.headers})")

    val futureResponse: Future[WSResponse] = request.get()
    futureResponse.map { response =>
      logger.debug(s"Authenticate for token with HAT at ${request.url} responded ${response.status}: ${response.body}")
      response.status match {
        case OK =>
          logger.debug(s"Response: ${response.status} ${response.body}")
          (response.json \ "accessToken")
            .validate[String]
            .getOrElse {
              throw new RuntimeException("Unauthorized")
            }
        case _ =>
          logger.error(s"Could not get auth response: ${response.status} ${response.body}")
          throw new RuntimeException("Unauthorized")
      }
    } recover {
      case e =>
        logger.error(s"Could not get auth response: ${e.getMessage}", e)
        throw new RuntimeException("Unauthorized")
    }
  }

  def createAccount(
      accessToken: String,
      hatUser: User
    )(implicit ec: ExecutionContext): Future[UUID] = {
    val request: WSRequest = ws
      .url(s"$baseUrl/users/user")
      .withHttpHeaders(jsonHeader, customAuthHeader -> accessToken)

    logger.debug(s"Create account request ${request.uri}")
    val futureResponse: Future[WSResponse] = request.post(Json.toJson(hatUser))
    futureResponse.map { response =>
      response.status match {
        case CREATED =>
          logger.info(s"Account for ${hatUser.name} on HAT $baseUrl created")
          hatUser.userId
        case _ =>
          logger.error(s"Account creation for ${hatUser.name} on HAT $baseUrl failed, $response, ${response.body}")
          throw new RuntimeException(s"Account creation for ${hatUser.name} failed")
      }
    }
  }

  def updateAccount(
      accessToken: String,
      hatUser: User
    )(implicit ec: ExecutionContext): Future[UUID] = {
    val request: WSRequest = ws
      .url(s"$baseUrl/users/user/${hatUser.userId}/update")
      .withHttpHeaders(jsonHeader, customAuthHeader -> accessToken)

    logger.debug(s"Update account request ${request.uri}")
    val futureResponse: Future[WSResponse] = request.put(Json.toJson(hatUser))
    futureResponse.map { response =>
      response.status match {
        case CREATED =>
          logger.info(s"Account for ${hatUser.name} on HAT $baseUrl updated")
          hatUser.userId
        case _ =>
          logger.error(s"Account updating for ${hatUser.name} on HAT $baseUrl failed, $response, ${response.body}")
          throw new RuntimeException(s"Account updating for ${hatUser.name} failed")
      }
    }
  }

  def enableAccount(
      accessToken: String,
      userId: UUID
    )(implicit ec: ExecutionContext): Future[Boolean] = {
    val request: WSRequest = ws
      .url(s"$baseUrl/users/user/$userId/enable")
      .withHttpHeaders(jsonHeader, customAuthHeader -> accessToken)

    logger.debug(s"Enable account $userId on $baseUrl")
    val futureResponse: Future[WSResponse] = request.put("")
    futureResponse.map { response =>
      response.status match {
        case OK =>
          logger.debug(s"Account for $userId on HAT $baseUrl enabled")
          true
        case _ =>
          logger.error(s"Account enabling for $userId on HAT $baseUrl failed, $response, ${response.body}")
          throw new RuntimeException(s"Account enabling for $userId failed")
      }
    }
  }

  def requestEmailVerification(
      email: String,
      applicationId: String,
      redirectUri: String
    )(implicit ec: ExecutionContext,
      lang: Lang): Future[String] = {
    val request: WSRequest = ws
      .url(s"$baseUrl/control/v2/auth/request-verification")
      .withQueryStringParameters("lang" -> lang.language)
      .withHttpHeaders(jsonHeader)

    logger.debug(s"Trigger HAT claim process on $baseUrl")
    val eventualResponse = request.post(Json.toJson(PdaEmailVerificationRequest(email, applicationId, redirectUri)))

    eventualResponse.flatMap { response =>
      response.status match {
        case OK =>
          logger.debug(s"Claim trigger on $baseUrl")
          val message = (response.json \ "message").validate[String].getOrElse("")

          Future.successful(message)

        case _ =>
          logger.error(s"Failed to trigger claim on $baseUrl. HAT response: ${response.body}")
          Future.failed(new ApiException(s"Failed to trigger claim on $baseUrl."))
      }
    }
  }

  @Deprecated
  def legacyHatClaimTrigger(
      email: String,
      applicationId: String,
      redirectUri: String,
      lang: String = "en"
    )(implicit ec: ExecutionContext): Future[String] = {
    val request: WSRequest = ws
      .url(s"$baseUrl/control/v2/auth/claim")
      .withQueryStringParameters("lang" -> lang)
      .withHttpHeaders(jsonHeader)

    logger.debug(s"Trigger HAT claim process on $baseUrl")
    val eventualResponse =
      request.post(Json.obj("email" -> email, "applicationId" -> applicationId, "redirectUri" -> redirectUri))

    eventualResponse.flatMap { response =>
      response.status match {
        case OK =>
          logger.debug(s"Claim trigger on $baseUrl")
          val message = (response.json \ "message").validate[String].getOrElse("")

          Future.successful(message)

        case _ =>
          logger.error(s"Failed to trigger claim on $baseUrl. HAT response: ${response.body}")
          Future.failed(new ApiException(s"Failed to trigger claim on $baseUrl."))
      }
    }
  }

}
