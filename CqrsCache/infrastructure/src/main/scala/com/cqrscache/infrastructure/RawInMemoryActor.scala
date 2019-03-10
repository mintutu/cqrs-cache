package com.cqrscache.infrastructure

import java.util.UUID

import akka.actor.ActorLogging
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import org.apache.commons.collections4.map.LinkedMap

class RawInMemoryActor extends PersistentActor with ActorLogging {

  override def persistenceId: String = "persistenceRawActor"

  val snapShotInterval = 100
  val cacheMap = new LinkedMap[UUID, String]()

  override def receiveRecover: Receive = {
    case Add(key, value) =>
      cacheMap.put(key, value)
      ()
    case Remove(key) =>
      cacheMap.remove(key)
      ()
    case Take =>
      val lastKey = cacheMap.lastKey()
      val lastValue = cacheMap.get(lastKey)
      cacheMap.remove(lastKey)
      ()
    case RecoveryCompleted =>
      log.info(s"Recovered cache with size: ${cacheMap.size}.")
  }

  override def receiveCommand: Receive = {
    case Add(key, value) => {
      if (cacheMap.containsKey(key)) {
        sender() ! ExistedKey
      } else {
        persistAsync(Add(key, value)) { _ =>
          if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {
            saveSnapshot(cacheMap)
          }
        }
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
