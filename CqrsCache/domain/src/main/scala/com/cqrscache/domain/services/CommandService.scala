package com.cqrscache.domain.services

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.ask
import com.cqrscache.domain.entity.{ FailedMessage, RecordMessage, ResponseMessage }
import com.cqrscache.infrastructure._
import com.cqrscache.infrastructure.entity.RequestMessage
import com.cqrscache.infrastructure.event._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

class CommandService(
    rawInMemoryActor:       ActorRef,
    aggregateInMemoryActor: ActorRef)(implicit val ec: ExecutionContext) {

  implicit val timeout: Timeout = Timeout(3 seconds)

  def handle(message: RequestMessage): Future[ResponseMessage] = {
    aggregateInMemoryActor ! message

    message.event match {
      case msg: AddingEvent =>
        val result = (rawInMemoryActor ? Add(msg.key, msg.value)).mapTo[Message]
        result.map {
          case ExecutionSuccess => RecordMessage(msg.key, msg.value)
          case _                => throw new Exception("Something wrong")
        }
      case msg: RemovingEvent =>
        val result = (rawInMemoryActor ? Remove(msg.key)).mapTo[Option[Element]]
        result.map {
          case Some(msg) => RecordMessage(msg.key, msg.value)
          case None      => FailedMessage("Key not found")
        }
      case msg: GettingEvent =>
        val result = (rawInMemoryActor ? Get(msg.key)).mapTo[Option[Element]]
        result.map {
          case Some(msg) => RecordMessage(msg.key, msg.value)
          case None      => FailedMessage("Key not found")
        }
      case _: PeekingEvent =>
        val result = (rawInMemoryActor ? Peek).mapTo[Option[Element]]
        result.map {
          case Some(msg) => RecordMessage(msg.key, msg.value)
          case None      => FailedMessage("Cache is empty")
        }
      case _: TakingEvent =>
        val result = (rawInMemoryActor ? Take).mapTo[Option[Element]]
        result.map {
          case Some(msg) => RecordMessage(msg.key, msg.value)
          case None      => FailedMessage("Cache is empty")
        }
    }
  }
}
