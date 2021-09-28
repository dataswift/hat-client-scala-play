package io.dataswift.hat.api.utils

import io.dataswift.models.hat.applications.ApplicationKind
import play.api.mvc.QueryStringBindable

object QueryBindables extends Bindables {}

trait Bindables {
  // Builds a [[QueryStringBindable]] for ApplicationKind
  implicit val applicationKindQueryBinder: QueryStringBindable[ApplicationKind.Kind] =
    new QueryStringBindable[ApplicationKind.Kind] {

      def unbind(
          key: String,
          value: ApplicationKind.Kind): String =
        s"$key=${value.kind.toLowerCase}"

      def bind(
          key: String,
          params: Map[String, Seq[String]]): Option[Either[String, ApplicationKind.Kind]] =
        params.get(key).flatMap(_.headOption).map(_.toLowerCase).map {
          case "dataplug" => Right(ApplicationKind.DataPlug(""))
          case "app"      => Right(ApplicationKind.App("", None, None))
          case "contract" => Right(ApplicationKind.Contract(""))
          case _          => Left(s"Cannot parse parameter $key as an ApplicationKind")
        }
    }
}
