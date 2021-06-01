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

  private object Version {
    val DsBackend = "2.4.1"
  }

  val resolvers = Seq(
    "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
  )

  object Library {
    val HatPlay = "io.dataswift" %% "hat-play" % Version.DsBackend

    object Specs2 {
      private val version = "4.10.3"
      val core            = "org.specs2" %% "specs2-core"          % version
      val matcherExtra    = "org.specs2" %% "specs2-matcher-extra" % version
      val mock            = "org.specs2" %% "specs2-mock"          % version
    }
  }

}
