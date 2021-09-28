/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package io.dataswift.hat.api.services

import io.dataswift.hat.api.services.MockHatServer.withHatClient
import org.specs2.concurrent.ExecutionEnv
import org.specs2.execute.Result
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import play.api.mvc.Results

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.io.Source._

class HatAuthenticationSpec(implicit ee: ExecutionEnv) extends Specification {

  def awaiting[T]: Future[MatchResult[T]] => Result = _.await

  sequential

  "HAT Authentication client" should {
    "retrieve public key" in {
      withHatClient { client =>
        val eventuallyResult = client.retrievePublicKey()
        val result           = Await.result(eventuallyResult, 20.seconds)
        result must startWith("-----BEGIN PUBLIC KEY-----")
      }
    }

    "return access token on successful login" in {
      withHatClient { client =>
        val eventuallyResult = client.authenticateForToken("user", "pa55")
        val validAccessToken = fromInputStream(
          Results.getClass.getClassLoader.getResourceAsStream("hat-test-messages/validAccessToken")
        ).mkString
        val result = Await.result(eventuallyResult, 20.seconds)
        result must beEqualTo(validAccessToken)
      }
    }

    "return error on failed login" in {
      withHatClient { client =>
        client.authenticateForToken("use", "pa55") map { res =>
          res must beEqualTo("")
        } recover {
          case e =>
            e.getMessage must beEqualTo("Unauthorized")
        }
      }
    }
  }
}
