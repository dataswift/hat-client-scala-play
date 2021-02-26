import Dependencies._

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.4.4"

libraryDependencies ++= Seq(
  Library.playWs,
  Library.playTest,
  Library.playJson,
  Library.playJsonJoda,
  Library.jodaTime,
  Library.hatPlayModels,
  Library.testCommon % Test
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
