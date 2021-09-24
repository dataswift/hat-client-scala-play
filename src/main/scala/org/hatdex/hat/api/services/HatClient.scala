/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package org.hatdex.hat.api.services

import play.api.Logging
import play.api.libs.ws.WSClient

import javax.inject.Inject

trait HatWsClient extends Logging {
  protected val ws: WSClient
  protected val baseUrl: String
  protected val baseUrlWithPath: String
}

//@deprecated("HAT deployments migrated to a new domain structure", since = "2.5")
class HatClient(
    val ws: WSClient,
    val hatAddress: String,
    val apiVersion: String)
    extends HatWsClient
    with HatAuthentication
    with HatDataDebits
    with HatApplications
    with HatRichData
    with HatSystem {
  @Inject def this(
      ws: WSClient,
      hatAddress: String) = this(ws, hatAddress, "v2.6")

  override val baseUrl = if (hatAddress.startsWith("http")) hatAddress else s"https://$hatAddress"
  override val baseUrlWithPath = s"$baseUrl/api/$apiVersion"
}
