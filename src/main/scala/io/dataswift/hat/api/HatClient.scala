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

  protected val customAuthHeader: String     = "X-Auth-Token"
  protected val jsonHeader: (String, String) = "Accept" -> "application/json"
}

class HatClient(
    val ws: WSClient,
    val hatAddress: String,
    val scheme: String,
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
      scheme: String) = this(ws, hatAddress, scheme, "v2.6")

  @Inject def this(
      ws: WSClient,
      region: String,
      serviceDomain: String,
      username: String,
      scheme: String = "https://") = this(ws, s"$region.$serviceDomain/pds/$username", scheme, "v2.6")

  // In case http schema is already specified in the `hatAddress`, `schema` parameter value is ignored
  // 2021-09-28 The check is needed for backward compatibility with different consumers of the library
  override val baseUrl: String = if (hatAddress.startsWith("http")) hatAddress else s"$scheme$hatAddress"
  override val baseUrlWithPath = s"$baseUrl/api/$apiVersion"
}
