package com.cqrscache.application.utitlity

import play.api.data.validation.Constraints

object Commons {

  val keyUUIDFormatPattern = Constraints.pattern(regex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".r, error = "Key must be UUID")

}
