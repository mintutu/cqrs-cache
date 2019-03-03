package com.cqrscache.modules

import akka.actor.{ ActorSystem, Props }
import com.cqrscache.domain.services.{ CommandService, QueryService }
import com.cqrscache.infrastructure.{ AggregateInMemoryActor, RawInMemoryActor }
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.ExecutionContext

class CachingModule extends AbstractModule with AkkaGuiceSupport {

  val actorSystem: ActorSystem = ActorSystem("LocalSystem")
  implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.global

  //Actors
  val rawInMemoryActor = actorSystem.actorOf(Props[RawInMemoryActor], name = "raw-in-memory-actor")
  val aggregateInMemoryActor = actorSystem.actorOf(Props[AggregateInMemoryActor], name = "aggregate-in-memory-actor")

  val commandService: CommandService = new CommandService(rawInMemoryActor, aggregateInMemoryActor)
  val queryService: QueryService = new QueryService(aggregateInMemoryActor)

  override def configure(): Unit = {
    bind(classOf[CommandService]).toInstance(commandService)
    bind(classOf[QueryService]).toInstance(queryService)
  }
}
