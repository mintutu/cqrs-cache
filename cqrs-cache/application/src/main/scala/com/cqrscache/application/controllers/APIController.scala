package com.cqrscache.application.controllers

import com.cqrscache.application.responses.{ APIResult, WarningParameter }
import com.github.tototoshi.play2.json4s.Json4s
import org.json4s.{ DefaultFormats, Extraction }
import play.api.mvc._
import play.api.data.FormError
import play.api.i18n._

abstract class APIController(
    cc: ControllerComponents) extends AbstractController(cc) with play.api.i18n.I18nSupport {
  protected val json4s: Json4s
  import json4s.implicits._
  implicit val formats = DefaultFormats
  val lang: Lang = Lang.defaultLang
  implicit val messages: Messages = MessagesImpl(lang, messagesApi)

  protected def success(): Result =
    Ok(Extraction.decompose(APIResult.toSuccessJson))

  protected def notFound(field: String, message: String): Result =
    NotFound(Extraction.decompose(APIResult.toWarningJson(Seq(WarningParameter(field, message)))))

  protected def badRequestFormWarning(field: String, message: String): Result =
    BadRequest(Extraction.decompose(APIResult.toWarningJson(Seq(WarningParameter(field, message)))))

  protected def badRequestFormWarning(formErrors: Seq[FormError]): Result =
    BadRequest(Extraction.decompose(APIResult.toWarningJson(formErrors.map { formError =>
      {
        WarningParameter(
          formError.key,
          formError.messages.mkString
        )
      }
    })))
}
