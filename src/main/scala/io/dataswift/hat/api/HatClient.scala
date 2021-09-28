/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 2 / 2017
 *
 */

package io.dataswift.hat.api

import io.dataswift.hat.api.services._
import play.api.Logging
import play.api.libs.ws.WSClient

import javax.inject.Inject

trait HatWsClient extends Logging {
  protected val ws: WSClient
  protected val baseUrl: String
  protected val baseUrlWithPath: String
}

class HatClient(
    val ws: WSClient,
    val hatAddress: String,
    val schema: String,
    val apiVersion: String)
    extends HatWsClient
    with HatAuthentication
    with HatDataDebits
    with HatApplications
    with HatRichData
    with HatSystem {

  @Inject def this(
      ws: WSClient,
      hatAddress: String) = this(ws, hatAddress, "https://", "v2.6")

  @Inject def this(
      ws: WSClient,
      hatAddress: String,
      schema: String) = this(ws, hatAddress, schema, "v2.6")

  // In case http schema is already specified in the `hatAddress`, `schema` parameter value is ignored
  override val baseUrl: String = if (hatAddress.startsWith("http")) hatAddress else s"$schema$hatAddress"
  override val baseUrlWithPath = s"$baseUrl/api/$apiVersion"
}
