/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package org.hatdex.hat.api.services

import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future }

import io.dataswift.models.hat.json.HatJsonFormats
import io.dataswift.models.hat.{ PdaEmailVerificationRequest, User }
import org.hatdex.hat.api.services.Errors.ApiException
import play.api.Logger
import play.api.http.Status._
import play.api.i18n.Lang
import play.api.libs.json._
import play.api.libs.ws._

trait HatAuthentication {
  protected val logger: Logger
  protected val ws: WSClient
  protected val schema: String
  protected val hatAddress: String
  protected val apiVersion: String
  protected val host: String = if (hatAddress.isEmpty) "mock" else hatAddress

  import HatJsonFormats._
  import io.dataswift.models.hat.json.ApiAuthenticationFormats._

  def retrievePublicKey()(implicit ec: ExecutionContext): Future[String] = {
    val request: WSRequest = ws
      .url(s"$schema$hatAddress/publickey")
      .withHttpHeaders("Accept" -> "text/plain")
      .withVirtualHost(host)

    request.get().map { response =>
      logger.debug(s"Hat $hatAddress public key response: $response")
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
      .url(s"$schema$hatAddress/users/access_token")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "username" -> username, "password" -> password)

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
      access_token: String,
      hatUser: User
    )(implicit ec: ExecutionContext): Future[UUID] = {
    val request: WSRequest = ws
      .url(s"$schema$hatAddress/users/user")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    logger.debug(s"Create account request ${request.uri}")
    val futureResponse: Future[WSResponse] = request.post(Json.toJson(hatUser))
    futureResponse.map { response =>
      response.status match {
        case CREATED =>
          logger.info(s"Account for ${hatUser.name} on HAT $hatAddress created")
          hatUser.userId
        case _ =>
          logger.error(s"Account creation for ${hatUser.name} on HAT $hatAddress failed, $response, ${response.body}")
          throw new RuntimeException(s"Account creation for ${hatUser.name} failed")
      }
    }
  }

  def updateAccount(
      access_token: String,
      hatUser: User
    )(implicit ec: ExecutionContext): Future[UUID] = {
    val request: WSRequest = ws
      .url(s"$schema$hatAddress/users/user/${hatUser.userId}/update")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    logger.debug(s"Update account request ${request.uri}")
    val futureResponse: Future[WSResponse] = request.put(Json.toJson(hatUser))
    futureResponse.map { response =>
      response.status match {
        case CREATED =>
          logger.info(s"Account for ${hatUser.name} on HAT $hatAddress updated")
          hatUser.userId
        case _ =>
          logger.error(s"Account updating for ${hatUser.name} on HAT $hatAddress failed, $response, ${response.body}")
          throw new RuntimeException(s"Account updating for ${hatUser.name} failed")
      }
    }
  }

  def enableAccount(
      access_token: String,
      userId: UUID
    )(implicit ec: ExecutionContext): Future[Boolean] = {
    val request: WSRequest = ws
      .url(s"$schema$hatAddress/users/user/$userId/enable")
      .withVirtualHost(host)
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    logger.debug(s"Enable account $userId on $hatAddress")
    val futureResponse: Future[WSResponse] = request.put("")
    futureResponse.map { response =>
      response.status match {
        case OK =>
          logger.debug(s"Account for $userId on HAT $hatAddress enabled")
          true
        case _ =>
          logger.error(s"Account enabling for $userId on HAT $hatAddress failed, $response, ${response.body}")
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
      .url(s"$schema$hatAddress/control/v2/auth/request-verification")
      .withVirtualHost(host)
      .withQueryStringParameters("lang" -> lang.language)
      .withHttpHeaders("Accept" -> "application/json")

    logger.debug(s"Trigger HAT claim process on $hatAddress")
    val eventualResponse = request.post(Json.toJson(PdaEmailVerificationRequest(email, applicationId, redirectUri)))

    eventualResponse.flatMap { response =>
      response.status match {
        case OK =>
          logger.debug(s"Claim trigger on $hatAddress")
          val message = (response.json \ "message").validate[String].getOrElse("")

          Future.successful(message)

        case _ =>
          logger.error(s"Failed to trigger claim on $hatAddress. HAT response: ${response.body}")
          Future.failed(new ApiException(s"Failed to trigger claim on $hatAddress."))
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
      .url(s"$schema$hatAddress/control/v2/auth/claim")
      .withVirtualHost(host)
      .withQueryStringParameters("lang" -> lang)
      .withHttpHeaders("Accept" -> "application/json")

    logger.debug(s"Trigger HAT claim process on $hatAddress")
    val eventualResponse =
      request.post(Json.obj("email" -> email, "applicationId" -> applicationId, "redirectUri" -> redirectUri))

    eventualResponse.flatMap { response =>
      response.status match {
        case OK =>
          logger.debug(s"Claim trigger on $hatAddress")
          val message = (response.json \ "message").validate[String].getOrElse("")

          Future.successful(message)

        case _ =>
          logger.error(s"Failed to trigger claim on $hatAddress. HAT response: ${response.body}")
          Future.failed(new ApiException(s"Failed to trigger claim on $hatAddress."))
      }
    }
  }

}
