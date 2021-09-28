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
import play.api.http.Status._
import play.api.libs.ws._

import scala.concurrent.{ ExecutionContext, Future }

trait HatSystem extends HatWsClient {
  def update(access_token: String)(implicit ec: ExecutionContext): Future[Unit] = {
    logger.debug(s"Update HAT database")

    val request: WSRequest = ws
      .url(s"$baseUrl/system/update")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.get()
    futureResponse.flatMap { response =>
      response.status match {
        case OK => Future.successful(())
        case _ =>
          logger.error(s"Updating $baseUrl failed, $response, ${response.body}")
          Future.failed(new RuntimeException(s"Updating $baseUrl failed"))
      }
    }
  }

  /**
    * @param access_token - Expect Milliner Shared Secret
    * @param ec
    * @return
    */
  def destroyCache(access_token: String)(implicit ec: ExecutionContext): Future[Unit] = {
    logger.debug(s"Destroying HAT Cache")

    val request: WSRequest = ws
      .url(s"$baseUrlWithPath/system/destroy-cache")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.delete()
    futureResponse.flatMap { response =>
      response.status match {
        case OK => Future.successful(())
        case NOT_FOUND =>
          logger.error(s"Destroying $baseUrl cache failed - HAT NOT FOUND, $response, ${response.body}")
          Future.failed(new RuntimeException(s"Destroying $baseUrl cache failed - HAT NOT FOUND"))
        case _ =>
          logger.error(s"Destroying $baseUrl cache failed, $response, ${response.body}")
          Future.failed(new RuntimeException(s"Destroying $baseUrl cache failed"))
      }
    }
  }

}
