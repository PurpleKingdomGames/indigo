import scala.util.Success
import scala.util.Try
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import mill.scalalib.scalafmt._
import coursier.maven.MavenRepository

import $ivy.`io.indigoengine::mill-indigo:0.15.0-RC4`, millindigo._

object snake extends ScalaJSModule with MillIndigo with ScalafmtModule {
  def scalaVersion   = "3.3.0"
  def scalaJSVersion = "1.13.1"

  override def generatedSources: T[Seq[PathRef]] = T {
    IndigoGenerators.embedText(os.pwd / "out", "Foo", "com.example", """foo, bar, "baz"""").map(p => PathRef(p)) ++
      super.generatedSources()
  }

  val indigoOptions: IndigoOptions =
    IndigoOptions.defaults
      .withTitle("Snake - Made with Indigo")
      .withWindowWidth(720)
      .withWindowHeight(516)
      .withBackgroundColor("black")

  def buildGame() = T.command {
    T {
      compile()
      fastLinkJS()
      indigoBuild()()
    }
  }

  def buildGameFull() = T.command {
    T {
      compile()
      fullLinkJS()
      indigoBuildFull()()
    }
  }

  def runGame() = T.command {
    T {
      compile()
      fastLinkJS()
      indigoRun()()
    }
  }

  def runGameFull() = T.command {
    T {
      compile()
      fullLinkJS()
      indigoRunFull()()
    }
  }

  val indigoVersion = "0.15.0-RC3"

  def ivyDeps = Agg(
    ivy"io.indigoengine::indigo-json-circe::$indigoVersion",
    ivy"io.indigoengine::indigo::$indigoVersion",
    ivy"io.indigoengine::indigo-extras::$indigoVersion"
  )

  object test extends ScalaJSTests {

    def ivyDeps = Agg(
      ivy"org.scalameta::munit::0.7.29"
    )

    def testFramework = "munit.Framework"

    override def moduleKind = T(mill.scalajslib.api.ModuleKind.CommonJSModule)

  }

}
