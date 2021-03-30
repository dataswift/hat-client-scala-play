/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

import sbt._

object Dependencies {

  object Version {
    val Play         = play.core.PlayVersion.current
    val JodaTime     = "2.10.10"
    val PlayJson     = "2.9.1"
    val DsTestCommon = "0.2.3"
    val DsBackend    = "2.2.1"
  }

  val resolvers = Seq(
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
  )

  object Library {
    val hatPlayModels      = "io.dataswift"      %% "hat-play"              % Version.DsBackend
    val playWs             = "com.typesafe.play" %% "play-ahc-ws"           % Version.Play
    val playServer         = "com.typesafe.play" %% "play-server"           % Version.Play
    val playJson           = "com.typesafe.play" %% "play-json"             % Version.PlayJson
    val playJsonJoda       = "com.typesafe.play" %% "play-json-joda"        % Version.PlayJson
    val playAkkaHttpServer = "com.typesafe.play" %% "play-akka-http-server" % Version.Play
    val testCommon         = "io.dataswift"      %% "test-common"           % Version.DsTestCommon
    val jodaTime           = "joda-time"          % "joda-time"             % Version.JodaTime

    object Specs2 {
      private val version = "4.10.3"
      val core            = "org.specs2" %% "specs2-core"          % version
      val matcherExtra    = "org.specs2" %% "specs2-matcher-extra" % version
      val mock            = "org.specs2" %% "specs2-mock"          % version
    }
  }

}
