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

import scala.io.Source._

import akka.util.ByteString
import io.dataswift.models.hat.{ EndpointData, ErrorMessage }
import play.api.Logger
import play.api.http.{ DefaultFileMimeTypes, DefaultFileMimeTypesProvider, FileMimeTypesConfiguration, HttpEntity }
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.routing.sird._
import play.core.server.Server

object MockHatServer {
  import io.dataswift.models.hat.json.RichDataJsonFormats._

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  private val logger = Logger(this.getClass)

  protected val apiVersion: String = "v2.6"

  implicit val fileMimeTypes: DefaultFileMimeTypes = new DefaultFileMimeTypesProvider(
    FileMimeTypesConfiguration(Map("json" -> "application/json", "pem" -> "text/plain"))
  ).get

  def withMockHatServerClient[T](block: WSClient => T): T =
    Server.withRouterFromComponents() { components =>
      import components.{ defaultActionBuilder => Action }
      {
        case GET(p"/publickey") =>
          Action {
            Results.Ok.sendResource("hat-test-messages/testPublicKey.pem")
          }
        case GET(p"/users/access_token") =>
          Action { request =>
            //        Logger.info("Responding to access token request")
            val requestHeaders = request.headers.toSimpleMap
            val maybeUsername  = requestHeaders.get("username")
            val maybePassword  = requestHeaders.get("password")

            (maybeUsername, maybePassword) match {
              case (Some("user"), Some("pa55")) => Results.Ok.sendResource("hat-test-messages/jwtValidToken.json")
              case _                            => Results.Unauthorized.sendResource("hat-test-messages/authInvalid.json")
            }
          }
        case POST(p"/api/$apiVersion/data/$namespace/$endpoint") =>
          Action { request =>
            logger.info(s"POST /api/$apiVersion/data/$namespace/$endpoint")
            request.body.asJson.map {
              case array: JsArray =>
                val result =
                  array.value.map(EndpointData(s"$namespace/$endpoint", Some(UUID.randomUUID), None, None, _, None))
                Results.Created.sendEntity(
                  HttpEntity.Strict(ByteString(Json.toJson(result).toString()), Some("application/json"))
                )
              case value: JsValue =>
                val result = EndpointData(s"$namespace/$endpoint", Some(UUID.randomUUID), None, None, value, None)
                Results.Created.sendEntity(
                  HttpEntity.Strict(ByteString(Json.toJson(result).toString()), Some("application/json"))
                )
            } getOrElse {
              Results.BadRequest.sendEntity(
                HttpEntity.Strict(ByteString(Json.toJson("Not JSON!").toString()), Some("application/json"))
              )
            }
          }
        case GET(p"/api/$apiVersion/data/$namespace/$endpoint") =>
          Action { request =>
            logger.info(s"GET /api/$apiVersion/data/$namespace/$endpoint")
            val maybeAccessToken = request.headers.toSimpleMap.get("X-Auth-Token")

            val validAccessToken = fromInputStream(
              Results.getClass.getClassLoader.getResourceAsStream("hat-test-messages/validAccessToken")
            ).mkString

            (namespace, endpoint, maybeAccessToken) match {
              case ("rumpel", "locations", Some(token)) if token == validAccessToken =>
                Results.Ok.sendResource("hat-test-messages/flexiRecordsSaved.json")
              case ("rumpel", _, Some(token)) if token == validAccessToken =>
                Results.Ok.sendEntity(HttpEntity.Strict(ByteString("[]"), Some("application/json")))
              case (_, _, Some(token)) if token == validAccessToken =>
                Results.Forbidden.sendEntity(
                  HttpEntity.Strict(ByteString(Json.toJson(ErrorMessage("Forbidden", "Access Denied")).toString),
                                    Some("application/json")
                  )
                )
              case _ => Results.Unauthorized.sendResource("hat-test-messages/authInvalid.json")
            }
          }

        case GET(p"/api/$apiVersion/data-debit/$dataDebitId/values") =>
          Action { request =>
            logger.info(s"GET /api/$apiVersion/data-debit/$dataDebitId/values")
            val maybeAccessToken = request.headers.toSimpleMap.get("X-Auth-Token")

            val validAccessToken = fromInputStream(
              Results.getClass.getClassLoader.getResourceAsStream("hat-test-messages/validAccessToken")
            ).mkString

            (dataDebitId, maybeAccessToken) match {
              case ("nodata", Some(token)) if token == validAccessToken =>
                Results.Ok.sendResource("hat-test-messages/dataDebitValuesEmpty.json")
              case ("locations", Some(token)) if token == validAccessToken =>
                Results.Ok.sendResource("hat-test-messages/dataDebitValuesLocations.json")
              case (_, Some(token)) if token == validAccessToken =>
                Results.Forbidden.sendEntity(
                  HttpEntity.Strict(ByteString(Json.toJson(ErrorMessage("Forbidden", "Access Denied")).toString),
                                    Some("application/json")
                  )
                )
              case _ => Results.Unauthorized.sendResource("hat-test-messages/authInvalid.json")
            }
          }
      }
    } { implicit port =>
      play.api.test.WsTestClient.withClient { client =>
        block(client)
      }
    }

  def withHatClient[T](block: HatClient => T): T =
    withMockHatServerClient { client =>
      block(new HatClient(client, "", "", "v2.6"))
    }
}
