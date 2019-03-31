package com.cqrscache.application.controllers

import com.github.tototoshi.play2.json4s.jackson.{ Json4s, Json4sModule }
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

trait Json4sTestHelper {
  val appJson: Application = new GuiceApplicationBuilder()
    .bindings(new Json4sModule)
    .build()
  val json4s: Json4s = appJson.injector.instanceOf[Json4s]
}
