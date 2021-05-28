import Dependencies._
import play.sbt.PlayImport

libraryDependencies ++= Seq(
  PlayImport.akkaHttpServer,
  PlayImport.ws,
  Lib.PlayJson,
  Lib.PlayJsonJoda,
  Library.HatPlay,
  Library.Specs2.matcherExtra % Test,
  Library.Specs2.mock         % Test,
  Library.Specs2.core         % Test
)
publishMavenStyle := true
publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(
    "Models" + prefix at "s3://library-artifacts-" + prefix + ".hubofallthings.com"
  )
}

inThisBuild(
  List(
    scalaVersion := "2.13.5",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13",
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
  )
)
