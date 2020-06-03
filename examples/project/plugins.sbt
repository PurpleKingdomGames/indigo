lazy val sbtIndigoVersion = "0.0.12-SNAPSHOT"

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.0.1")
addSbtPlugin("io.indigoengine" % "sbt-indigo" % sbtIndigoVersion)
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.7")
addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.3.4+250-848d3a5d")
