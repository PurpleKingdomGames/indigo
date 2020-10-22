import scala.util.Success
import scala.util.Try
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import coursier.maven.MavenRepository

import $ivy.`io.indigoengine::mill-indigo:0.4.1-SNAPSHOT`, millindigo._

object snake extends ScalaJSModule with MillIndigo {
  def scalaVersion   = "2.13.3"
  def scalaJSVersion = "1.3.0"

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

  val indigoVersion = "0.4.1-SNAPSHOT"

  def ivyDeps = Agg(
    ivy"io.indigoengine::indigo-json-circe::$indigoVersion",
    ivy"io.indigoengine::indigo::$indigoVersion",
    ivy"io.indigoengine::indigo-extras::$indigoVersion"
  )

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases")
  )

  def compileIvyDeps      = T { super.compileIvyDeps() ++ Agg(ivy"org.wartremover::wartremover:2.4.9") }
  def scalacPluginIvyDeps = T { super.scalacPluginIvyDeps() ++ Agg(ivy"org.wartremover:::wartremover:2.4.9") }

  def scalacOptions = ScalacOptions.scala213Compile ++ Seq(
    "-P:wartremover:traverser:org.wartremover.warts.Unsafe"
  )

  object test extends Tests {
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest::0.7.4",
      ivy"org.scalacheck::scalacheck::1.14.3"
    )

    def testFrameworks = Seq("utest.runner.Framework")

    def scalacOptions = ScalacOptions.scala213Test
  }

}

object ScalacOptions {

  lazy val scala213Compile: Seq[String] =
    Seq(
      "-Yrangepos",
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                         // Specify character encoding used by source files.
      "-explaintypes",                 // Explain type errors in more detail.
      "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
      "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",        // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
      "-Ywarn-dead-code",              // Warn when dead code is identified.
      "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen",          // Warn when numerics are widened.
      "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals",          // Warn if a local definition is unused.
      "-Ywarn-unused:params",          // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",        // Warn if a private member is unused.
      "-Ywarn-value-discard"           // Warn when non-Unit expression results are unused.
    )

  lazy val scala213Test: Seq[String] =
    Seq(
      "-Yrangepos",
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8",                         // Specify character encoding used by source files.
      "-explaintypes",                 // Explain type errors in more detail.
      "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds",         // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
      "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
      "-Xlint:option-implicit",        // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload"  // Parameterized overloaded implicit methods are not visible as view bounds.
    )

}
