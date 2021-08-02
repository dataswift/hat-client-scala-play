package org.hatdex.hat.api.services

import io.dataswift.models.hat._
import org.hatdex.hat.api.services.Errors.{ ApiException, UnauthorizedActionException }
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{ JsError, JsSuccess, Json }
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }

import java.net.URL
import scala.concurrent.{ ExecutionContext, Future }

trait HatDataDebits {
  protected val logger: Logger
  protected val ws: WSClient
  protected val hatAddress: String
  protected val apiVersion: String

  import io.dataswift.models.hat.json.RichDataJsonFormats._

  def getDataDebit(
      access_token: String,
      dataDebitId: String
    )(implicit ec: ExecutionContext): Future[DataDebit] = {

    val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/data-debit/$dataDebitId")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.get()

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          response.json.validate[DataDebit] match {
            case s: JsSuccess[DataDebit] => Future.successful(s.get)
            case e: JsError =>
              val message = s"Error parsing response from a successful data debit request: $e"
              logger.error(message)
              Future.failed(new ApiException(message))
          }
        case FORBIDDEN =>
          Future.failed(
            UnauthorizedActionException(s"Retrieving data debit $dataDebitId from $hatAddress unauthorized")
          )
        case _ =>
          logger.error(s"Retrieving data debit $dataDebitId from $hatAddress failed ${response.body}")
          Future.failed(
            new ApiException(s"Retrieving data debit $dataDebitId from $hatAddress failed ${response.body}")
          )
      }
    }
  }

  def listDataDebits(access_token: String)(implicit ec: ExecutionContext): Future[Seq[DataDebit]] = {

    val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/data-debit")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.get()

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          response.json.validate[Seq[DataDebit]] match {
            case s: JsSuccess[Seq[DataDebit]] => Future.successful(s.get)
            case e: JsError =>
              val message = s"Error parsing response from a successful data debit request: $e"
              logger.error(message)
              Future.failed(new ApiException(message))
          }
        case FORBIDDEN =>
          Future.failed(UnauthorizedActionException(s"Retrieving data debits from $hatAddress unauthorized"))
        case _ =>
          logger.error(s"Retrieving data debits from $hatAddress failed ${response.body}")
          Future.failed(new ApiException(s"Retrieving data debits from $hatAddress failed ${response.body}"))
      }
    }
  }

  def getDataDebitValues(
      access_token: String,
      dataDebitId: String
    )(implicit ec: ExecutionContext): Future[DataDebitData] = {

    val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/data-debit/$dataDebitId/values")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.get()

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          response.json.validate[DataDebitData] match {
            case s: JsSuccess[DataDebitData] => Future.successful(s.get)
            case e: JsError =>
              val message = s"Error parsing response from a successful data debit values request: $e"
              logger.error(message)
              Future.failed(new ApiException(message))
          }
        case FORBIDDEN =>
          Future.failed(
            UnauthorizedActionException(s"Retrieving data debit $dataDebitId values from $hatAddress unauthorized")
          )
        case _ =>
          logger.error(s"Retrieving data debit $dataDebitId values from $hatAddress failed ${response.body}")
          Future.failed(
            new ApiException(s"Retrieving data debit $dataDebitId values from $hatAddress failed ${response.body}")
          )
      }
    }
  }

  def registerDataDebit(
      access_token: String,
      dataDebitId: String,
      dataDebit: DataDebitSetupRequest
    )(implicit ec: ExecutionContext): Future[DataDebit] = {
    val request: WSRequest = ws
      .url(s"$hatAddress/api/$apiVersion/data-debit/$dataDebitId")
      .withHttpHeaders("Accept" -> "application/json", "X-Auth-Token" -> access_token)

    val futureResponse: Future[WSResponse] = request.post(Json.toJson(dataDebit))

    futureResponse.flatMap { response =>
      response.status match {
        case CREATED =>
          response.json.validate[DataDebit] match {
            case s: JsSuccess[DataDebit] => Future.successful(s.get)
            case e: JsError =>
              val message = s"Error parsing response from a successful data debit registration request: $e"
              logger.error(message)
              Future.failed(new ApiException(message))
          }
        case FORBIDDEN =>
          Future.failed(
            UnauthorizedActionException(s"Registering data debit $dataDebitId with $hatAddress unauthorized")
          )
        case _ =>
          logger.error(s"Registering data debit $dataDebitId with $hatAddress failed ${response.body}")
          Future.failed(
            new ApiException(s"Registering data debit $dataDebitId with $hatAddress failed ${response.body}")
          )
      }
    }
  }
}
