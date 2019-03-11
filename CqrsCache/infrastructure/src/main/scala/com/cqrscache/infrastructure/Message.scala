package com.cqrscache.infrastructure

import java.util.UUID

import akka.actor.ActorRef

trait Message

//Message for raw database
case class Record(key: UUID, value: String) extends Message
case class Add(key: UUID, value: String) extends Message
case class Remove(key: UUID) extends Message
case class Get(key: UUID) extends Message
case object Peek extends Message
case object Take extends Message

//Message for aggregate database
case class AggregateRecord()
case class RateByIpAddress(ipAddress: String) extends Message
case object RateReport extends Message
case object RateReset extends Message

case class Element(key: UUID, value: String) extends Message
case class RateReportResponse(report: List[(String, Int)]) extends Message

case object ExistedKey extends Message
case object ExecutionSuccess extends Message
case object ExecutionFailed extends Message
