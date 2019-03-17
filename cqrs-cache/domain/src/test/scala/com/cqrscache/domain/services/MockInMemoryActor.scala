package com.cqrscache.domain.services

import akka.actor.Actor
import com.cqrscache.infrastructure.{ Peek, RateByIpAddress, RateReport, RateReportResponse }

class MockInMemoryActor extends Actor {

  override def receive: Receive = {

    case RateByIpAddress(_) => sender() ! 1

    case RateReport         => sender() ! RateReportResponse(report = Nil)

    case Peek               => sender() ! None
  }
}