addSbtPlugin("com.typesafe.play"  % "sbt-plugin"         % "2.8.7")
addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.19.0")

resolvers += "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
addSbtPlugin("org.scalameta" % "sbt-scalafmt"          % "2.3.4")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix"          % "0.9.19")
addSbtPlugin("io.dataswift"  % "sbt-scalatools-common" % "0.2.3")
addSbtPlugin("com.dwijnand"  % "sbt-dynver"            % "4.1.1")
