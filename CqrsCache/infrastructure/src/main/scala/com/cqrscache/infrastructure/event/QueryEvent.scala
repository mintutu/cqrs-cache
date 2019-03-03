package com.cqrscache.infrastructure.event

trait QueryEvent extends Event

case class RateEvent(ipAddress: String) extends QueryEvent
