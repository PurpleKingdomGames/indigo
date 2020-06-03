//-----------------------------------
// The essentials.
//-----------------------------------
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")

addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")

addSbtPlugin("io.indigoengine" % "sbt-indigo" % "0.0.12-SNAPSHOT")

//-----------------------------------
// Everything below here is optional!
// Static analysis and test coverage
//-----------------------------------
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.7")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
