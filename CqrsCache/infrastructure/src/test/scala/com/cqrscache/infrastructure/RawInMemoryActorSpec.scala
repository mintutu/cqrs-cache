package com.cqrscache.infrastructure

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.cqrscache.infrastructure.utils.AkkaTestkitSupport
import akka.actor.{ ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import org.specs2.mutable.Specification

import scala.concurrent.Await

class RawInMemoryActorSpec extends Specification {
  sequential

  implicit val timeout: Timeout = Timeout(3, TimeUnit.SECONDS)
  val sampleKey: UUID = UUID.fromString("01234567-9ABC-DEF0-1124-56789ABC1004")
  val sampleValue = "sample01"

  "Receive Add message" should {
    "return ExecutionSuccess if element does not exist in memory cache" >> new AkkaTestkitSupport {
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse = inMemoryActor ? Add(sampleKey, sampleValue)
      val result = Await.result(futureResponse, timeout.duration).asInstanceOf[Message]
      result must equalTo(ExecutionSuccess)
    }
  }

  "Receive Remove message" should {
    "return an value if element already existed in memory cache" >> new AkkaTestkitSupport {
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Add(sampleKey, sampleValue)
      val futureResponse02 = inMemoryActor ? Remove(sampleKey)
      val result = Await.result(futureResponse02, timeout.duration).asInstanceOf[Option[Element]]
      result must equalTo(Some(Element(sampleKey, sampleValue)))
    }

    "return None if element does not exist in memory cache" >> new AkkaTestkitSupport {
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Remove(sampleKey)
      val result = Await.result(futureResponse01, timeout.duration).asInstanceOf[Option[Element]]
      result must equalTo(None)
    }
  }

  "Receive Get message" should {
    "return Some(ElementRecord) if it exists in cache" >> new AkkaTestkitSupport {
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Add(sampleKey, sampleValue)
      val futureResponse02 = inMemoryActor ? Get(sampleKey)
      val result = Await.result(futureResponse02, timeout.duration).asInstanceOf[Option[Element]]
      result must equalTo(Some(Element(sampleKey, sampleValue)))
    }

    "return None if it does not exist in cache" >> new AkkaTestkitSupport {
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Get(sampleKey)
      val result = Await.result(futureResponse01, timeout.duration).asInstanceOf[Option[Element]]
      result must equalTo(None)
    }
  }

  "Receive Peek message" should {
    "return Some(ElementRecord) if the memory cache is not empty" >> new AkkaTestkitSupport {
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Add(sampleKey, sampleValue)
      val futureResponse02 = inMemoryActor ? Peek
      val result = Await.result(futureResponse02, timeout.duration).asInstanceOf[Option[Element]]
      result must equalTo(Some(Element(sampleKey, sampleValue)))
    }

    "return None if the memory cache is empty" >> new AkkaTestkitSupport {
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Peek
      val result = Await.result(futureResponse01, timeout.duration).asInstanceOf[Option[Element]]
      result must equalTo(None)
    }
  }

  "Receive Take message" should {
    "return Some(ElementRecord) and remove it if the memory cache is not empty" >> new AkkaTestkitSupport {
      val sampleKey1: UUID = UUID.fromString("01234567-9ABC-DEF0-1124-56789ABC1004")
      val sampleValue1 = "sample01"
      val sampleKey2: UUID = UUID.fromString("01234568-9ABC-DEF0-1124-56789ABC1004")
      val sampleValue2 = "sample02"
      val inMemoryActor: ActorRef = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Add(sampleKey1, sampleValue1)
      val futureResponse02 = inMemoryActor ? Add(sampleKey2, sampleValue2)
      val futureResponse03 = inMemoryActor ? Take
      val futureResponse04 = inMemoryActor ? Peek

      val resultOfTaking = Await.result(futureResponse03, timeout.duration).asInstanceOf[Option[Element]]
      val resultOfPeeking = Await.result(futureResponse04, timeout.duration).asInstanceOf[Option[Element]]

      resultOfTaking must equalTo(Some(Element(sampleKey2, sampleValue2)))
      resultOfPeeking must equalTo(Some(Element(sampleKey1, sampleValue1)))
    }

    "return None if the memory cache is empty (timeout 3s)" >> new AkkaTestkitSupport {
      val inMemoryActor = system.actorOf(Props[RawInMemoryActor])

      val futureResponse01 = inMemoryActor ? Take
      val result = Await.result(futureResponse01, timeout.duration).asInstanceOf[Option[Element]]
      result must equalTo(None)
    }
  }
}
