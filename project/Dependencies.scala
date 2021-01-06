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
    val crossScala   = Seq("2.13.3", "2.12.12")
    val scalaVersion = crossScala.head
  }

  val resolvers = Seq(
    "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com",
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com",
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  )

  object Library {

    object DataswiftModels {
      private val version =
        "1.0.0"
      val hat     = "io.dataswift.models" %% "hat"      % version
      val hatPlay = "io.dataswift.models" %% "hat-play" % version
    }

    object Play {
      private val version    = play.core.PlayVersion.current
      val ws                 = "com.typesafe.play" %% "play-ahc-ws"           % version
      val test               = "com.typesafe.play" %% "play-test"             % version % "compile" // Used by the MockHatServer
      val json               = "com.typesafe.play" %% "play-json"             % "2.9.1"
      val jsonJoda           = "com.typesafe.play" %% "play-json-joda"        % "2.9.1"
      val playAkkaHttpServer = "com.typesafe.play" %% "play-akka-http-server" % version % Test
    }

    object Specs2 {
      private val version = "4.10.3"
      val core            = "org.specs2" %% "specs2-core"          % version % "test"
      val matcherExtra    = "org.specs2" %% "specs2-matcher-extra" % version % "test"
      val mock            = "org.specs2" %% "specs2-mock"          % version % "test"
    }

    object Utils {
      val jodaTime = "joda-time" % "joda-time" % "2.10"
    }
  }
}
