package com.cqrscache.application.responses

case class WarningParameter(
    field:   String,
    message: String
)

case class ErrorParameter(
    code:    Int,
    message: String
)

case class APIResult(
    success: Boolean,
    result:  Option[Any]                   = None,
    warning: Option[Seq[WarningParameter]] = None,
    error:   Option[ErrorParameter]        = None
)

object APIResult {

  def toSuccessJson: APIResult = {
    APIResult(
      success = true
    )
  }

  def toWarningJson(warnings: Seq[WarningParameter]): APIResult = {
    APIResult(
      success = false,
      None,
      Some(warnings),
      None
    )
  }
}
