package com.cqrscache.infrastructure.utils

import akka.actor.{ ActorSystem, Terminated }
import akka.testkit.{ ImplicitSender, TestKit }
import org.specs2.mutable.After

import scala.concurrent.Future

abstract class AkkaTestkitSupport
  extends TestKit(ActorSystem("mock-actor-system"))
  with After
  with ImplicitSender {

  def after: Future[Terminated] = system.terminate()

}