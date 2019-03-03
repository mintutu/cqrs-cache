package com.cqrscache.modules

import akka.actor.{ ActorSystem, Props }
import com.cqrscache.domain.services.{ CommandService, QueryService }
import com.cqrscache.infrastructure.{ AggregateInMemoryActor, RateReset, RawInMemoryActor }
import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class CachingModule extends AbstractModule with AkkaGuiceSupport {

  val actorSystem: ActorSystem = ActorSystem("LocalSystem")
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.global
  val config = ConfigFactory.load()
  val rateSchedule = Duration.fromNanos(config.getDuration("rate-schedule").toNanos)

  //Actors
  val rawInMemoryActor = actorSystem.actorOf(Props[RawInMemoryActor], name = "raw-in-memory-actor")
  val aggregateInMemoryActor = actorSystem.actorOf(Props[AggregateInMemoryActor], name = "aggregate-in-memory-actor")

  val commandService: CommandService = new CommandService(rawInMemoryActor, aggregateInMemoryActor)
  val queryService: QueryService = new QueryService(aggregateInMemoryActor)
  actorSystem.scheduler.schedule(0 seconds, rateSchedule)(aggregateInMemoryActor ! RateReset)

  override def configure(): Unit = {
    bind(classOf[CommandService]).toInstance(commandService)
    bind(classOf[QueryService]).toInstance(queryService)
  }
}
