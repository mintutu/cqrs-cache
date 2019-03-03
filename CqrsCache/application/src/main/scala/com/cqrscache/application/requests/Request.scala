package com.cqrscache.application.requests

import com.cqrscache.application.utitlity.Commons
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.Forms.mapping

case class Request(key: String, value: Option[String])

object Request {

  val form = Form(
    mapping(
      "key" -> nonEmptyText.verifying(Commons.keyUUIDFormatPattern),
      "value" -> optional(text))(Request.apply)(Request.unapply))
}
