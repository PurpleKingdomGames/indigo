//-----------------------------------
// The essentials.
//-----------------------------------
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.32")

addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")

addSbtPlugin("indigo" % "sbt-indigo" % "0.0.12-SNAPSHOT")

//-----------------------------------
// Everything below here is optional!
// Static analysis and test coverage
//-----------------------------------
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.3")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
