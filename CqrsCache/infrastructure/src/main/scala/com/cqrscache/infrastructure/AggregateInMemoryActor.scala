package com.cqrscache.infrastructure

import akka.actor.Actor
import com.cqrscache.infrastructure.entity.RequestMessage

import scala.collection.mutable

class AggregateInMemoryActor extends Actor {

  val aggregateRateMap = new mutable.HashMap[String, Int]

  def receive: Receive = {
    case RequestMessage(ipAddress, event, executeTime) => {
      val rate = aggregateRateMap.getOrElseUpdate(ipAddress, 0)
      aggregateRateMap.update(ipAddress, rate + 1)
      println(this.context + " " + aggregateRateMap.toString())
    }
    case RateByUser(ipAddress) => {
      val rate = aggregateRateMap.getOrElseUpdate(ipAddress, 0)
      println(this.context + " " + aggregateRateMap.toString())
      sender() ! rate
    }
    case RateReset => {
      //aggregateRateMap.clear()
    }
  }
}
