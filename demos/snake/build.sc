import scala.util.Success
import scala.util.Try
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import coursier.maven.MavenRepository

import $ivy.`io.indigoengine::mill-indigo:0.9.3-SNAPSHOT`, millindigo._

object snake extends ScalaJSModule with MillIndigo {
  def scalaVersion   = "3.0.2"
  def scalaJSVersion = "1.7.0"

  val gameAssetsDirectory: os.Path = os.pwd / "assets"
  val showCursor: Boolean          = true
  val title: String                = "Snake - Made with Indigo"
  val windowStartWidth: Int        = 720
  val windowStartHeight: Int       = 516

  def buildGame() = T.command {
    T {
      compile()
      fastOpt()
      indigoBuild()()
    }
  }

  def runGame() = T.command {
    T {
      compile()
      fastOpt()
      indigoRun()()
    }
  }

  val indigoVersion = "0.9.3-SNAPSHOT"

  def ivyDeps = Agg(
    ivy"io.indigoengine::indigo-json-circe::$indigoVersion",
    ivy"io.indigoengine::indigo::$indigoVersion",
    ivy"io.indigoengine::indigo-extras::$indigoVersion"
  )

  def scalacOptions = super.scalacOptions() ++ ScalacOptions.compile

  object test extends Tests {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.26"
    )

    def testFramework = "munit.Framework"

    override def moduleKind = T(mill.scalajslib.api.ModuleKind.CommonJSModule)

    def scalacOptions = super.scalacOptions() ++ ScalacOptions.test
  }

}

object ScalacOptions {

  lazy val compile: Seq[String] =
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
      "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
      "-language:strictEquality"       // Scala 3 - Multiversal Equality
    )

  lazy val test: Seq[String] =
    Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                         // Specify character encoding used by source files.
      "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked"                     // Enable additional warnings where generated code depends on assumptions.
    )

}
