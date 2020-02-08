lazy val sbtIndigoVersion = "0.0.12-SNAPSHOT"

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "0.6.1")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "0.6.32")
addSbtPlugin("indigo" % "sbt-indigo" % sbtIndigoVersion)
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.3")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.3.4+250-848d3a5d")
