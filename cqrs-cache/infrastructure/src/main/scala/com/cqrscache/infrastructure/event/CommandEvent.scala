package com.cqrscache.infrastructure.event

import java.util.UUID

trait CommandEvent extends Event

case class AddingEvent(key: UUID, value: String) extends CommandEvent
case class PeekingEvent() extends CommandEvent
case class RemovingEvent(key: UUID) extends CommandEvent
case class GettingEvent(key: UUID) extends CommandEvent
case class TakingEvent() extends CommandEvent

