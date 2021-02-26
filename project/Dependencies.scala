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

  object Versions {
    val crossScala = Seq("2.13.3", "2.12.12")
    val scalaVersion = crossScala.head
    val playVersion = play.core.PlayVersion.current
    val testCommon = "0.2.3"
    val jodaTime = "2.10"
    val modelsVersion = "2.0.4"
    val playJson = "2.9.1"
  }

  val resolvers = Seq(
    "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com",
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com",
  )

  object Library {
    val hatPlayModels = "io.dataswift" %% "hat-play" % Versions.modelsVersion
    val playWs = "com.typesafe.play" %% "play-ahc-ws"           % Versions.playVersion
    val playTest = "com.typesafe.play" %% "play-test" % Versions.playVersion % "compile" // Used by the MockHatServer
    val playJson = "com.typesafe.play" %% "play-json" % Versions.playJson
    val playJsonJoda = "com.typesafe.play" %% "play-json-joda" % Versions.playJson
    val testCommon = "io.dataswift" %% "test-common" % Versions.testCommon
    val jodaTime = "joda-time" % "joda-time" % Versions.jodaTime

    object Specs2 {
      private val version = "4.10.3"
      val core            = "org.specs2" %% "specs2-core"          % version
      val matcherExtra    = "org.specs2" %% "specs2-matcher-extra" % version
      val mock            = "org.specs2" %% "specs2-mock"          % version
    }
  }

}
