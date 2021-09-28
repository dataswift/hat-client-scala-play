package io.dataswift.hat.api.services

object Errors {
  class ApiException(
      message: String = "",
      cause: Throwable = None.orNull)
      extends Exception(message, cause)
  case class UnauthorizedActionException(
      message: String = "",
      cause: Throwable = None.orNull)
      extends ApiException(message, cause)
  case class DuplicateDataException(
      message: String = "",
      cause: Throwable = None.orNull)
      extends ApiException(message, cause)
}
