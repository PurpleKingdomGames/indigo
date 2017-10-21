
import sbt.Keys.{organization, scalaVersion, version}

object IndigoSbtBuild {

  val indigoVersion = "0.0.6-SNAPSHOT"

  lazy val commonSettings = Seq(
    version := indigoVersion,
    scalaVersion := "2.12.3",
    organization := "com.purplekingdomgames"
  )
}
