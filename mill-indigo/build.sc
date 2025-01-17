import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalalib.scalafmt._
import publish._
import coursier.maven.MavenRepository

object `mill-indigo` extends Cross[IndigoPluginModule]("2.13")
trait IndigoPluginModule extends CrossScalaModule with PublishModule with ScalafmtModule {

  def scalaVersion =
    crossScalaVersion match {
      case _ => "2.13.16"
    }

  def millLibVersion = "0.12.5"

  def indigoVersion = T.input { IndigoVersion.getVersion(T.workspace) }

  def ivyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millLibVersion}",
    ivy"com.lihaoyi::mill-main-api:${millLibVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millLibVersion}",
    ivy"com.lihaoyi::mill-scalajslib:${millLibVersion}",
    ivy"com.lihaoyi::mill-scalalib-api:${millLibVersion}",
    ivy"com.lihaoyi::os-lib:0.11.3",
    ivy"io.indigoengine::indigo-plugin:${indigoVersion()}"
  )

  def repositoriesTask = T.task {
    super.repositoriesTask() ++ Seq(
      MavenRepository("https://oss.sonatype.org/content/repositories/releases")
    )
  }

  object test extends ScalaTests {
    def ivyDeps = Agg(ivy"org.scalameta::munit:1.0.4")

    def testFramework = "munit.Framework"
  }

  def publishVersion = indigoVersion()

  def pomSettings = PomSettings(
    description = "mill-indigo",
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
  def getVersion(workspaceDir: os.Path): String = {
    def rec(wd: os.Path, file: String, levels: Int, version: Option[String]): String = {
      val msg = "ERROR: Couldn't find indigo version."
      version match {
        case Some(v) =>
          println(s"""Indigo version set to '$v'""")
          v

        case None if levels < 3 =>
          try {
            val v = os.read.lines(wd / file).head
            rec(wd, file, levels, Some(v))
          } catch {
            case _: Throwable =>
              rec(wd / os.RelPath.up, file, levels + 1, None)
          }

        case None =>
          println(msg)
          throw new Exception(msg)
      }
    }

    rec(workspaceDir, ".indigo-version", 0, None)
  }
}
