import sbt._
import Keys._
import Dependencies._
import Settings._

name := """CQRS Cache"""

version := "1.0"

enablePlugins(sbtdocker.DockerPlugin, UniversalPlugin, JavaServerAppPackaging)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .aggregate(application, domain, infrastructure)
  .dependsOn(application, domain, infrastructure)
  .settings(
    libraryDependencies ++= Seq(scalaTest % Test, guice, evolutions, jdbc)
  )
  .settings(commonSettings)
  .settings(routesGenerator := InjectedRoutesGenerator)

lazy val application = (project in file("cqrs-cache/application"))
  .enablePlugins(PlayScala, SbtWeb)
  .dependsOn(domain)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(playJson4s, playJson4sTest, filters))

lazy val domain = (project in file("cqrs-cache/domain"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(infrastructure % "test->test;compile->compile")
  .settings(commonSettings)

lazy val infrastructure = (project in file("cqrs-cache/infrastructure"))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(akkaPersistence, akkaPersistenceTesting, akkaPersistenceJDBC, akkaRemote, akkaTestkit, apacheCommon, postgresql))
  .settings(
    //use for persistence actor testing
    resolvers += "dnvriend" at "http://dl.bintray.com/dnvriend/maven"
  )

dockerfile in docker := {
  val appDir: File = stage.value
  val targetDir = "/app"

  new Dockerfile {
    from("openjdk:8-jre")
    env(("HTTP_PORT", "8080"))
    env(("JMX_PORT", "3338"))
    env(("JMX_REMOTE_SSL", "false"))
    env(("JMX_REMOTE_AUTHENTICATE", "false"))
    entryPointRaw(
      s"""
         |-Dhttp.port=$${HTTP_PORT}
         |-Ddb.default.driver=org.postgresql.Driver
         |-Dcom.sun.management.jmxremote.port=$${JMX_PORT}
         |-Dcom.sun.management.jmxremote.ssl=$${JMX_REMOTE_SSL}
         |-Dcom.sun.management.jmxremote.authenticate=$${JMX_REMOTE_AUTHENTICATE}
         |-Dlog.dir=./logs
         |-J-Xms4g -J-Xmx6g -J-server
         |""".stripMargin.replaceAll("\n+", " ")
    )
    copy(appDir, targetDir)
  }
}

imageNames in docker := Seq(
  // Sets the latest tag
  ImageName(s"${organization.value}/${name.value}:latest"),

  // Sets a name with a tag that contains the project version
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value)
  )
)

buildOptions in docker := BuildOptions(
  cache = false,
  removeIntermediateContainers = BuildOptions.Remove.Always,
  pullBaseImage = BuildOptions.Pull.Always
)
