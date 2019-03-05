import sbt._
import Keys._
import Dependencies._
import Settings._

name := """CQRS Cache"""

version := "1.0"

//use for persistence actor testing
resolvers += "dnvriend" at "http://dl.bintray.com/dnvriend/maven"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .aggregate(application, domain, infrastructure)
  .dependsOn(application, domain, infrastructure)
  .settings(
    libraryDependencies ++= Seq( scalaTest % Test, guice)
  )
  .settings(commonSettings)
  .settings(routesGenerator := InjectedRoutesGenerator)

lazy val application = (project in file("CqrsCache/application"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(domain)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(playJson4s, playJson4sTest, filters))

lazy val domain = (project in file("CqrsCache/domain"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(infrastructure % "test->test;compile->compile")
  .settings(commonSettings)

lazy val infrastructure = (project in file("CqrsCache/infrastructure"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(akkaPersistence, akkaPersistenceTesting, akkaRemote, akkaTestkit, levelDb, levelDbJni, apacheCommon))
  .settings(
    fork in Test := true
  )
