package com.cqrscache.domain.services

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.util.Timeout
import com.cqrscache.domain.entity.{ FailedMessage, RecordMessage }
import com.cqrscache.infrastructure.entity.RequestMessage
import com.cqrscache.infrastructure.event._
import com.cqrscache.infrastructure.utils.AkkaTestkitSupport
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

class CommandServiceSpec extends Specification with Mockito {

  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val timeout = Timeout(3, TimeUnit.SECONDS)
  val ipAddress = "10.0.0.1"
  val uuid = UUID.randomUUID()
  val nonExistKey = UUID.fromString("7dc53df5-703e-49b3-8670-b1c468f47f1f")

  "receive AddingEvent" should {
    "return record element successfully" in new AkkaTestkitSupport {
      val rawInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val aggregateInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val commandService = new CommandService(rawInMemoryActorMock, aggregateInMemoryActorMock)
      val resultFuture = commandService.handle(RequestMessage(ipAddress, AddingEvent(uuid, "sample"), System.currentTimeMillis()))
      val result = Await.result(resultFuture, 3 seconds)
      result must equalTo(RecordMessage(uuid, "sample"))
    }
  }

  "receive RemovingEvent or GettingEvent" should {
    "return if record exist" in new AkkaTestkitSupport {
      val rawInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val aggregateInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val commandService = new CommandService(rawInMemoryActorMock, aggregateInMemoryActorMock)
      val removeResultFuture = commandService.handle(RequestMessage(ipAddress, RemovingEvent(uuid), System.currentTimeMillis()))
      val removeResult = Await.result(removeResultFuture, 3 seconds)
      removeResult must equalTo(RecordMessage(uuid, "sample"))

      val gettingResultFuture = commandService.handle(RequestMessage(ipAddress, GettingEvent(uuid), System.currentTimeMillis()))
      val gettingResult = Await.result(removeResultFuture, 3 seconds)
      gettingResult must equalTo(RecordMessage(uuid, "sample"))
    }

    "return not exist" in new AkkaTestkitSupport {
      val rawInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val aggregateInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val commandService = new CommandService(rawInMemoryActorMock, aggregateInMemoryActorMock)
      val removeResultFuture = commandService.handle(RequestMessage(ipAddress, RemovingEvent(nonExistKey), System.currentTimeMillis()))
      val removeResult = Await.result(removeResultFuture, 3 seconds)
      removeResult must equalTo(FailedMessage("Key not found"))

      val gettingResultFuture = commandService.handle(RequestMessage(ipAddress, GettingEvent(uuid), System.currentTimeMillis()))
      val gettingResult = Await.result(removeResultFuture, 3 seconds)
      gettingResult must equalTo(FailedMessage("Key not found"))
    }
  }

  "receive PeekingEvent" should {
    "return success " in new AkkaTestkitSupport {
      val rawInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val aggregateInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val commandService = new CommandService(rawInMemoryActorMock, aggregateInMemoryActorMock)
      val removeResultFuture = commandService.handle(RequestMessage(ipAddress, PeekingEvent(), System.currentTimeMillis()))
      val removeResult = Await.result(removeResultFuture, 3 seconds)
      removeResult must equalTo(FailedMessage("Cache is empty"))
    }
  }

  "receive TakingEvent" should {
    "return success " in new AkkaTestkitSupport {
      val rawInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val aggregateInMemoryActorMock = system.actorOf(Props[MockInMemoryActor])
      val commandService = new CommandService(rawInMemoryActorMock, aggregateInMemoryActorMock)
      val removeResultFuture = commandService.handle(RequestMessage(ipAddress, TakingEvent(), System.currentTimeMillis()))
      val removeResult = Await.result(removeResultFuture, 3 seconds)
      removeResult.getClass must equalTo(classOf[RecordMessage])
    }
  }
}
