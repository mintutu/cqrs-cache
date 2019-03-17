package com.cqrscache.domain.services

import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.util.Timeout
import com.cqrscache.domain.entity.RateMessage
import com.cqrscache.infrastructure.entity.RequestMessage
import com.cqrscache.infrastructure.event.RateEvent
import com.cqrscache.infrastructure.utils.AkkaTestkitSupport
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

class QueryServiceSpec extends Specification with Mockito {

  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)
  val ipAddress = "127.0.0.1"
  "handle" should {
    "process RateEvent success" in new AkkaTestkitSupport {
      val queryMockActor = system.actorOf(Props[MockInMemoryActor])
      val queryService = new QueryService(queryMockActor)
      val resultFuture = queryService.handle(RequestMessage(ipAddress, RateEvent(ipAddress), System.currentTimeMillis()))
      val result = Await.result(resultFuture, 3 seconds)
      result must equalTo(RateMessage(ipAddress, 1))
    }

  }

}
