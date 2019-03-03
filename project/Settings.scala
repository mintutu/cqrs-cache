import com.typesafe.sbt.SbtScalariform.autoImport.scalariformPreferences
import org.scalastyle.sbt.ScalastylePlugin.autoImport.scalastyle
import play.sbt.PlayImport.specs2
import sbt.Keys._
import sbt._
import scalariform.formatter.preferences._

object Settings {
  lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

  lazy val commonSettings = Seq(
    scalaVersion := "2.12.4",
    scalacOptions ++= Seq(
      "utf8",
      "-deprecation",
      "-feature",
      "-unchecked",
      //"-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfatal-warnings"
    ),
    aggregate in Test := true,
    parallelExecution in Test := true,
    fork in Test := true,
    doc in Compile <<= target.map(_ / "none"),
    libraryDependencies ++= Seq(
      specs2 % Test,
      "org.specs2" %% "specs2-core" % "3.6.4" % "test",
      "org.specs2" %% "specs2-mock" % "3.6.4" % "test"
    ),
    scalaSource in Compile := baseDirectory.value / "src" / "main" / "scala",
    scalaSource in Test := baseDirectory.value / "src" / "test" / "scala",
    resourceDirectory in Compile := baseDirectory.value / "src" / "main" / "resources",
    resourceDirectory in Test := baseDirectory.value / "src" / "test" / "resources",
    compileScalastyle := scalastyle.in(Compile).toTask("").value,
    (compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle,
    scalariformPreferences := scalariformPreferences.value
      .setPreference(DoubleIndentConstructorArguments, true)
      .setPreference(DanglingCloseParenthesis, Preserve)
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 40)
      .setPreference(CompactControlReadability, false)
      .setPreference(CompactStringConcatenation, false)
      .setPreference(FormatXml, true)
      .setPreference(IndentLocalDefs, false)
      .setPreference(IndentPackageBlocks, true)
      .setPreference(IndentSpaces, 2)
      .setPreference(IndentWithTabs, false)
      .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
      .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
      .setPreference(PreserveSpaceBeforeArguments, false)
      .setPreference(RewriteArrowSymbols, false)
      .setPreference(SpaceBeforeColon, false)
      .setPreference(SpaceInsideBrackets, false)
      .setPreference(SpaceInsideParentheses, false)
      .setPreference(SpacesWithinPatternBinders, true)
  )
}
