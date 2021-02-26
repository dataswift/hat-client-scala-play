import Dependencies._

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.4"

libraryDependencies ++= Seq(
  Library.playWs,
  Library.playJson,
  Library.playJsonJoda,
  Library.jodaTime,
  Library.hatPlayModels,
  Library.Specs2.matcherExtra % Test,
  Library.Specs2.mock         % Test,
  Library.Specs2.core         % Test,
  Library.playTest
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
