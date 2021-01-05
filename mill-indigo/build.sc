import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import publish._
import coursier.maven.MavenRepository


object `mill-indigo` extends ScalaModule with PublishModule {

  def scalaVersion = "2.13.4"

  def millLibVersion = "0.9.4"

  def ivyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millLibVersion}",
    ivy"com.lihaoyi::mill-main-api:${millLibVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millLibVersion}",
    ivy"com.lihaoyi::mill-scalalib-api:${millLibVersion}",
    ivy"com.lihaoyi::os-lib:0.7.1",
    ivy"io.indigoengine::indigo-plugin:${IndigoVersion.getVersion}"
  )

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases")
  )

  // def scalacOptions = Seq("-P:wartremover:only-warn-traverser:org.wartremover.warts.Unsafe")

  // def scalacPluginIvyDeps = T { super.scalacPluginIvyDeps() ++ Agg(ivy"org.wartremover:::wartremover:2.4.13") }

  object test extends Tests {
    def ivyDeps = Agg(ivy"org.scalameta::munit:0.7.20")

    def testFrameworks = Seq("munit.Framework")
  }

  def publishVersion = IndigoVersion.getVersion

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
