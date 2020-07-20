package org.hatdex.hat.api.models.applications

import play.api.libs.json.JsValue

case class PayloadWrapper(data: JsValue,
                           next: Option[String] = None,
                           paginationParameters: PaginationParameters,
                           filters: ApplicationFilters)
