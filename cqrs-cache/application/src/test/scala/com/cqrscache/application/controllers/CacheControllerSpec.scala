package com.cqrscache.application.controllers

import java.util.UUID

import com.cqrscache.domain.entity.{ FailedMessage, RecordMessage }
import com.cqrscache.domain.services.{ CommandService, QueryService }
import com.cqrscache.infrastructure.entity.RequestMessage
import com.cqrscache.infrastructure.event.{ AddingEvent, RemovingEvent }
import org.specs2.mock.Mockito
import play.api.libs.json.{ JsBoolean, JsValue, Json }
import play.api.mvc.Results
import play.api.test.{ FakeRequest, Helpers, PlaySpecification, WithApplication }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CacheControllerSpec extends PlaySpecification with Mockito with Json4sTestHelper with Results {

  val mockCommandService: CommandService = mock[CommandService]
  val mockQueryService: QueryService = mock[QueryService]

  val controller: CacheController = new CacheController(
    json4s = json4s,
    commandService = mockCommandService,
    queryService = mockQueryService,
    cc = Helpers.stubControllerComponents()
  )

  "add" should {
    "return failed if key is not UUID format" >> new WithApplication {
      val fakeRequest: FakeRequest[JsValue] = FakeRequest(POST, "/cache/add")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(Json.parse(
          s"""
             |{
             |    "key": "1234",
             |    "value": "1234"
             |}
             |""".stripMargin
        ))
      val apiResult = controller.add().apply(fakeRequest)

      val statusExpected: JsValue = JsBoolean(false)
      val messageExpected: JsValue = Json.parse(
        """
          |{
          |    "success": false,
          |    "warning": [
          |        {
          |            "field": "key",
          |            "message": "Key must be UUID"
          |        }
          |    ]
          |}
        """.stripMargin
      )

      status(apiResult) must equalTo(BAD_REQUEST)
      val jsonResult: JsValue = contentAsJson(apiResult)
      jsonResult must beEqualTo(messageExpected)
    }
  }

  "return success if key is UUID and value is normal string" >> new WithApplication {
    val sampleKey: UUID = UUID.fromString("01234567-9ABC-DEF0-1124-56789ABC1004")
    val sampleValue = "sample01"
    mockCommandService.handle(any[RequestMessage]) returns Future(RecordMessage(sampleKey, sampleValue))
    val fakeRequest: FakeRequest[JsValue] = FakeRequest(POST, "/cache/add")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.parse(
        s"""
           |{
           |    "key": "01234567-9ABC-DEF0-1124-56789ABC1004",
           |    "value": "sample01"
           |}
           |""".stripMargin
      ))
    val apiResult = controller.add().apply(fakeRequest)

    val statusExpected: JsValue = JsBoolean(false)
    val messageExpected: JsValue = Json.parse(
      """
        |{
        |    "success": true
        |}
      """.stripMargin
    )

    status(apiResult) must equalTo(OK)
    val jsonResult: JsValue = contentAsJson(apiResult)
    jsonResult must beEqualTo(messageExpected)
  }

  "return failed if key does not exist" >> new WithApplication {
    val sampleKey: UUID = UUID.fromString("01234567-9ABC-DEF0-1124-56789ABC1024")
    val sampleValue = "sample01"
    mockCommandService.handle(any[RequestMessage]) returns Future(FailedMessage("Key not found"))
    val fakeRequest: FakeRequest[JsValue] = FakeRequest(POST, "/cache/remove")
      .withHeaders("Content-Type" -> "application/json")
      .withBody(Json.parse(
        s"""
           |{
           |    "key": "01234567-9ABC-DEF0-1124-56789ABC1024"
           |}
           |""".stripMargin
      ))
    val apiResult = controller.remove().apply(fakeRequest)

    val statusExpected: JsValue = JsBoolean(false)
    val messageExpected: JsValue = Json.parse(
      """
        |{
        |   "success":false,
        |   "warning":[
        |      {
        |         "field":"key",
        |         "message":"Key not found"
        |      }
        |   ]
        |}
      """.stripMargin
    )

    status(apiResult) must equalTo(BAD_REQUEST)
    val jsonResult: JsValue = contentAsJson(apiResult)
    jsonResult must beEqualTo(messageExpected)
  }
}
