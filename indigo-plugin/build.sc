import scala.util.Success
import scala.util.Try
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import mill.scalalib.scalafmt._
import coursier.maven.MavenRepository
import publish._

object `indigo-plugin` extends Cross[IndigoPluginModule]("2.12", "2.13")
class IndigoPluginModule(val crossScalaVersion: String) extends CrossScalaModule with PublishModule with ScalafmtModule {

  def indigoVersion = T.input { IndigoVersion.getVersion }

  def scalaVersion =
    crossScalaVersion match {
      case "2.12" => "2.12.17"
      case "2.13" => "2.13.10"
      case _  => "2.13.10"
    }

  def artifactName = "indigo-plugin"

  def ivyDeps =
    Agg(ivy"com.lihaoyi::os-lib:0.8.0")

  def repositories =
    super.repositories ++ Seq(
      MavenRepository("https://oss.sonatype.org/content/repositories/releases")
    )

  def scalacOptions =
    ScalacOptions.scala213Compile 

  object test extends Tests {
    def ivyDeps =
      Agg(
        ivy"org.scalameta::munit:0.7.29"
      )

    def testFramework = "munit.Framework"

    def scalacOptions = ScalacOptions.scala213Test
  }

  def publishVersion = indigoVersion()

  def pomSettings =
    PomSettings(
      description = "indigo-plugin",
      organization = "io.indigoengine",
      url = "https://github.com/PurpleKingdomGames/indigo",
      licenses = Seq(License.MIT),
      versionControl = VersionControl.github("PurpleKingdomGames", "indigo"),
      developers = Seq(
        Developer("davesmith00000", "David Smith", "https://github.com/davesmith00000")
      )
    )

}

object IndigoVersion {
  def getVersion: String = {
    def rec(path: String, levels: Int, version: Option[String]): String = {
      val msg = "ERROR: Couldn't find indigo version."
      version match {
        case Some(v) =>
          println(s"""Indigo version set to '$v'""")
          v

        case None if levels < 3 =>
          try {
            val v = scala.io.Source.fromFile(path).getLines.toList.head
            rec(path, levels, Some(v))
          } catch {
            case _: Throwable =>
              rec("../" + path, levels + 1, None)
          }

        case None =>
          println(msg)
          throw new Exception(msg)
      }
    }

    rec(".indigo-version", 0, None)
  }
}

object ScalacOptions {

  lazy val scala213Compile: Seq[String] =
    Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                         // Specify character encoding used by source files.
      "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings"               // Fail the compilation if there are any warnings.
    )

  lazy val scala213Test: Seq[String] =
    Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                         // Specify character encoding used by source files.
      "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
    )

}
