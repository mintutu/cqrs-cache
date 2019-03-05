package com.cqrscache.application.requests

import com.cqrscache.application.utitlity.Commons
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, optional, text}
import play.api.data.Forms.mapping

case class Request(key: String, value: Option[String])

object Request {

  val form = Form(
    mapping(
      "key" -> nonEmptyText.verifying(Commons.keyUUIDFormatPattern),
      "value" -> optional(text))(Request.apply)(Request.unapply))
}
