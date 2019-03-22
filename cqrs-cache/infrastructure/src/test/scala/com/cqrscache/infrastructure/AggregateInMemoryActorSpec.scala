package com.cqrscache.infrastructure

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, Props}
import com.cqrscache.infrastructure.utils.AkkaTestkitSupport
import org.specs2.mutable.Specification
import akka.pattern.ask
import akka.util.Timeout
import com.cqrscache.infrastructure.entity.RequestMessage
import com.cqrscache.infrastructure.event.PeekingEvent

import scala.concurrent.Await

class AggregateInMemoryActorSpec extends Specification {
  sequential

  implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
  val ipAddress = "10.0.0.1"

  "Receive RequestMessage" should {
    "aggregate data in memory successful" >> new AkkaTestkitSupport {
      val aggregateMemoryActor: ActorRef = system.actorOf(Props[AggregateInMemoryActor])

      aggregateMemoryActor ! RequestMessage(
        ipAddress,
        event = PeekingEvent(),
        executeTime = System.currentTimeMillis())
      Thread.sleep(1000)

      //query rate
      val futureResponse = aggregateMemoryActor ? RateByIpAddress(ipAddress)
      val result = Await.result(futureResponse, timeout.duration).asInstanceOf[Int]
      result must equalTo(1)

      //get rate report
      val futureReportResponse = aggregateMemoryActor ? RateReport
      val reportResult = Await.result(futureReportResponse, timeout.duration).asInstanceOf[RateReportResponse]
      reportResult must equalTo(RateReportResponse(List((ipAddress, 1))))

      //reset aggregate
      aggregateMemoryActor ! RateReset
      Thread.sleep(1000)
      val futureReportResponse2 = aggregateMemoryActor ? RateReport
      val reportResult2 = Await.result(futureReportResponse2, timeout.duration).asInstanceOf[RateReportResponse]
      reportResult2 must equalTo(RateReportResponse(Nil))
    }

  }
}
