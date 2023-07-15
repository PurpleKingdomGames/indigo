import scala.util.Success
import scala.util.Try
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import mill.scalalib.scalafmt._
import coursier.maven.MavenRepository

import $ivy.`io.indigoengine::mill-indigo:0.15.0-RC3`, millindigo._

object snake extends ScalaJSModule with MillIndigo with ScalafmtModule {
  def scalaVersion   = "3.3.0"
  def scalaJSVersion = "1.13.1"

  val gameAssetsDirectory: os.Path     = os.pwd / "assets"
  val showCursor: Boolean              = true
  val title: String                    = "Snake - Made with Indigo"
  val windowStartWidth: Int            = 720
  val windowStartHeight: Int           = 516
  val disableFrameRateLimit: Boolean   = false
  val electronInstall: ElectronInstall = ElectronInstall.Latest
  val backgroundColor: String          = "black"

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
