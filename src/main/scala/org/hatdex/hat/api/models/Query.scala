package org.hatdex.hat.api.models
import play.api.libs.json.JsValue
import org.hatdex.hat.api.models.applications.ApplicationKind

case class PaginationParameters(startId: Option[String] = None,
                                 limit: Option[Int] = None)

case class FilterParameters(unpublished: Option[Boolean], kind: Option[ApplicationKind.Kind])
case class ApplicationFilters(unpublished: Option[Boolean], kind: Option[String])

case class PayloadWrapper(
                           data: JsValue,
                           next: Option[String] = None,
                           paginationParameters: PaginationParameters,
                           filters: ApplicationFilters)

