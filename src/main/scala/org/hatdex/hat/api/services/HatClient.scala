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

class HatClient(
    val ws: WSClient,
    val hatAddress: String,
    override val apiVersion: String)
    extends HatAuthentication
    with HatDataDebits
    with HatApplications
    with HatRichData
    with HatSystem
    with Logging {
  @Inject def this(
      ws: WSClient,
      hatAddress: String) = this(ws, hatAddress, "v2.6")
  override val host: String = if (hatAddress.isEmpty) "mock" else hatAddress
}
