package com.cqrscache.application.controllers

import java.util.UUID

import com.cqrscache.application.requests.{ Rate, Request }
import com.cqrscache.domain.entity.{ RateMessage, RateReportMessage, RecordMessage }
import com.cqrscache.domain.services.{ CommandService, QueryService }
import com.cqrscache.infrastructure.entity.RequestMessage
import com.cqrscache.infrastructure.event._
import com.github.tototoshi.play2.json4s.Json4s
import javax.inject.Inject
import org.json4s.Extraction
import play.api.libs.json.{ JsError, JsValue, Json }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

class CacheController @Inject() (
    override val json4s: Json4s,
    commandService:      CommandService,
    queryService:        QueryService,
    cc:                  ControllerComponents)(implicit ec: ExecutionContext) extends APIController(cc) {

  import json4s.implicits._

  def add(): Action[JsValue] = Action.async(parse.json) { request =>
    Request.form.bind(request.body).fold(
      formWithError => {
        Future.successful(badRequestFormWarning(formWithError.errors))
      },
      requestBody => {
        val ipAddress = request.remoteAddress
        val uuidKey = UUID.fromString(requestBody.key)
        val event = AddingEvent(uuidKey, requestBody.value.getOrElse(""))
        val result = commandService.handle(RequestMessage(ipAddress, event, executeTime = System.currentTimeMillis()))
        result.map { _ => success() }.recover {
          case _: Exception => InternalServerError("Something wrong")
        }
      }
    )
  }

  def remove(): Action[JsValue] = Action.async(parse.json) { request =>
    Request.form.bind(request.body).fold(
      formWithError => {
        Future.successful(badRequestFormWarning(formWithError.errors))
      },
      requestBody => {
        val ipAddress = request.remoteAddress
        val uuidKey = UUID.fromString(requestBody.key)
        val event = RemovingEvent(uuidKey)
        val result = commandService.handle(RequestMessage(ipAddress, event, executeTime = System.currentTimeMillis()))
        result.map { _ => success() }.recover {
          case _: Exception => InternalServerError("Something wrong")
        }
      }
    )
  }

  def peek(): Action[AnyContent] = Action.async {
    request =>
      val ipAddress = request.remoteAddress
      val event = PeekingEvent()
      val result = commandService.handle(RequestMessage(ipAddress, event, executeTime = System.currentTimeMillis()))
      result.map {
        case msg: RecordMessage => Ok(Extraction.decompose(Request(msg.key.toString, Some(msg.value))))
        case _                  => InternalServerError("Something wrong")
      }
  }

  def take(): Action[AnyContent] = Action.async {
    request =>
      val ipAddress = request.remoteAddress
      val event = TakingEvent()
      val result = commandService.handle(RequestMessage(ipAddress, event, executeTime = System.currentTimeMillis()))
      result.map {
        case msg: RecordMessage => Ok(Extraction.decompose(Request(msg.key.toString, Some(msg.value))))
        case _                  => InternalServerError("Something wrong")
      }
  }

  def getRate(ipAddress: String): Action[AnyContent] = Action.async {
    request =>
      val event = RateEvent(ipAddress)
      val result = queryService.handle(RequestMessage(ipAddress, event, executeTime = System.currentTimeMillis()))
      result.map {
        case msg: RateMessage => Ok(Extraction.decompose(Rate(msg.ipAddress, msg.rate)))
        case _                => InternalServerError("Something wrong")
      }
  }

  def getRateReport(): Action[AnyContent] = Action.async {
    request =>
      val ipAddress = request.remoteAddress
      val result = queryService.handle(RequestMessage(ipAddress, RateReportEvent, executeTime = System.currentTimeMillis()))
      result.map {
        case msg: RateReportMessage => Ok(Extraction.decompose(msg))
        case _                      => InternalServerError("Something wrong")
      }
  }
}
