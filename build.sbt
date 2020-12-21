import Dependencies._

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.4"

libraryDependencies ++= Seq(
  Library.Play.ws,
  Library.Play.test,
  Library.Play.json,
  Library.Play.jsonJoda,
  Library.Specs2.matcherExtra,
  Library.Specs2.mock,
  Library.Specs2.core,
  Library.Utils.jodaTime,
  Library.Play.playAkkaHttpServer,
  Library.DataswiftModels.hat,
  Library.DataswiftModels.hatPlay,
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
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)
