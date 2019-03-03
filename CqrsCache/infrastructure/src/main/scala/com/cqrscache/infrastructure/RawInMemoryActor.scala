package com.cqrscache.infrastructure

import java.util.UUID

import akka.actor.Actor
import org.apache.commons.collections4.map.LinkedMap

class RawInMemoryActor extends Actor {

  val cacheMap = new LinkedMap[UUID, String]()

  def receive: Receive = {
    case Add(key, value) => {
      if (cacheMap.containsKey(key)) {
        sender() ! ExistedKey
      } else {
        cacheMap.put(key, value)
        sender() ! ExecutionSuccess
      }
    }

    case Remove(key) => {
      val value = cacheMap.get(key)
      if (value != null) {
        cacheMap.remove(key)
        sender() ! Some(Element(key, value))
      } else {
        sender() ! None
      }
    }

    case Peek => {
      if (!cacheMap.isEmpty) {
        val lastElement = cacheMap.lastKey()
        val lastValue = cacheMap.get(lastElement)
        sender() ! Some(Element(lastElement, lastValue))
      } else {
        sender() ! None
      }
    }

    case Take => {
      if (!cacheMap.isEmpty) {
        val lastKey = cacheMap.lastKey()
        val lastValue = cacheMap.get(lastKey)
        cacheMap.remove(lastKey)
        sender() ! Some(Element(lastKey, lastValue))
      } else {
        sender() ! None
      }
    }
  }
}
