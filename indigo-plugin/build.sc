import scala.util.Success
import scala.util.Try
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import coursier.maven.MavenRepository
import publish._

object `indigo-plugin` extends Cross[IndigoPluginModule]("2.12", "2.13")
class IndigoPluginModule(val crossScalaVersion: String) extends CrossScalaModule with PublishModule {

  def scalaVersion =
    crossScalaVersion match {
      case "2.12" => "2.12.10"
      case "2.13" => "2.13.4"
    }

  def artifactName = "indigo-plugin"

  def ivyDeps =
    Agg(
      ivy"com.lihaoyi::os-lib:0.7.1"
    )

  def repositories =
    super.repositories ++ Seq(
      MavenRepository("https://oss.sonatype.org/content/repositories/releases")
    )

  def compileIvyDeps      = T(super.compileIvyDeps() ++ Agg(ivy"org.wartremover::wartremover:2.4.13"))
  def scalacPluginIvyDeps = T(super.scalacPluginIvyDeps() ++ Agg(ivy"org.wartremover:::wartremover:2.4.13"))

  def scalacOptions =
    ScalacOptions.scala213Compile ++ Seq(
      "-P:wartremover:traverser:org.wartremover.warts.Unsafe"
    )

  object test extends Tests {
    def ivyDeps =
      Agg(
        ivy"com.lihaoyi::utest::0.7.4"
      )

    def testFrameworks = Seq("utest.runner.Framework")

    def scalacOptions = ScalacOptions.scala213Test
  }

  def publishVersion = IndigoVersion.getVersion

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
