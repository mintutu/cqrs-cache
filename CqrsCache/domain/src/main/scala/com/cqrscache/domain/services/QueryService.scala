package com.cqrscache.domain.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.cqrscache.domain.entity.{ FailedMessage, RateMessage, ResponseMessage }
import com.cqrscache.infrastructure.RateByIpAddress
import com.cqrscache.infrastructure.entity.RequestMessage
import com.cqrscache.infrastructure.event.RateEvent

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

class QueryService(aggregateInMemoryActor: ActorRef)(implicit val ec: ExecutionContext) {

  implicit val timeout: Timeout = Timeout(3 seconds)

  def handle(message: RequestMessage): Future[ResponseMessage] = {

    message.event match {
      case msg: RateEvent =>
        val result = (aggregateInMemoryActor ? RateByIpAddress(msg.ipAddress))
        result.map {
          case rate: Int => RateMessage(msg.ipAddress, rate)
          case _         => FailedMessage("Something wrong")
        }
    }
  }
}
