package org.hatdex.hat.api.services

import akka.http.scaladsl.model.Uri

trait ServiceHelper {
  def getServiceUrl(
      serviceDomain: String,
      region: String,
      username: String): String = {
    val uri = Uri(serviceDomain)
    s"${uri.scheme}://$region.${uri.authority}/pds/$username"
  }
}
