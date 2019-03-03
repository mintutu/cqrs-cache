package com.cqrscache.infrastructure.entity

import com.cqrscache.infrastructure.event.Event

case class RequestMessage(ipAddress: String, event: Event, executeTime: Long)
