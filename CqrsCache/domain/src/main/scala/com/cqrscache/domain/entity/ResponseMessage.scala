package com.cqrscache.domain.entity

import java.util.UUID

trait ResponseMessage

case class RecordMessage(key: UUID, value: String) extends ResponseMessage

case class RateMessage(ipAddress: String, rate: Int) extends ResponseMessage

case class RateReportMessage(report: Seq[RateMessage]) extends ResponseMessage

case class FailedMessage(message: String) extends ResponseMessage
