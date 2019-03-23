package com.cqrscache.domain.services

import java.util.UUID

import akka.actor.Actor
import com.cqrscache.infrastructure._

class MockInMemoryActor extends Actor {

  val nonExistKey = "7dc53df5-703e-49b3-8670-b1c468f47f1f"

  override def receive: Receive = {

    //Command
    case Add(_, _) => sender() ! ExecutionSuccess
    case Remove(key) => {
      if (key == UUID.fromString(nonExistKey)) {
        sender() ! None
      } else {
        sender() ! Some(Element(key, "sample"))
      }
    }
    case Get(key) => {
      if (key == UUID.fromString(nonExistKey)) {
        sender() ! None
      } else {
        sender() ! Some(Element(key, "sample"))
      }
    }
    case Take               => sender() ! Some(Element(UUID.randomUUID(), "sample"))
    case Peek               => sender() ! None

    //Query
    case RateByIpAddress(_) => sender() ! 1
    case RateReport         => sender() ! RateReportResponse(report = Nil)
  }
}