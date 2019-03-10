package com.cqrscache.infrastructure

import akka.actor.{ Actor, ActorLogging }
import com.cqrscache.infrastructure.entity.RequestMessage
import akka.persistence._
import scala.collection.mutable

class AggregateInMemoryActor extends PersistentActor with ActorLogging {

  override def persistenceId: String = "persistenceAggregateActor"
  val snapShotInterval = 100

  val aggregateRateMap = new mutable.HashMap[String, Int]

  override def receiveRecover: Receive = {
    case RequestMessage(ipAddress, event, executeTime) =>
      val rate = aggregateRateMap.getOrElseUpdate(ipAddress, 0)
      aggregateRateMap.update(ipAddress, rate + 1)
      ()
    case RecoveryCompleted =>
      log.info(s"Recovered cache with size: ${aggregateRateMap.size}.")
  }

  override def receiveCommand: Receive = {
    case RequestMessage(ipAddress, event, executeTime) =>
      persistAsync(RequestMessage(ipAddress, event, executeTime)) { _ =>
        val rate = aggregateRateMap.getOrElseUpdate(ipAddress, 0)
        aggregateRateMap.update(ipAddress, rate + 1)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {
          saveSnapshot(aggregateRateMap)
        }
      }
    case RateByIpAddress(ipAddress) =>
      val rate = aggregateRateMap.getOrElseUpdate(ipAddress, 0)
      sender() ! rate
    case RateReport =>
      sender() ! RateReportResponse(aggregateRateMap.toList)
    case RateReset =>
      aggregateRateMap.clear()
    case _ =>
      throw new Exception("Unknown message")
  }
}
