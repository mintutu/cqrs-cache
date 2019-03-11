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
      saveEvent(Add(key, value))
      cacheMap.put(key, value)
      sender() ! ExecutionSuccess
    }

    case Get(key) => {
      val value = cacheMap.get(key)
      if (value != null) {
        sender() ! Some(Element(key, value))
      } else {
        sender() ! None
      }
    }

    case Remove(key) => {
      val value = cacheMap.get(key)
      if (value != null) {
        saveEvent(Remove(key))
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
        saveEvent(Take)
        val lastKey = cacheMap.lastKey()
        val lastValue = cacheMap.get(lastKey)
        cacheMap.remove(lastKey)
        sender() ! Some(Element(lastKey, lastValue))
      } else {
        sender() ! None
      }
    }
  }

  private def saveEvent(message: Message): Unit = {
    persistAsync(message) { _ =>
      if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {
        saveSnapshot(cacheMap)
      }
    }
  }
}
