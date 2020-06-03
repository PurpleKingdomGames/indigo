import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import publish._
import coursier.maven.MavenRepository

object `mill-indigo` extends ScalaModule with PublishModule {
  def scalaVersion = "2.13.2"
  def millLibVersion  = "0.7.3"

  def ivyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millLibVersion}",
    ivy"com.lihaoyi::mill-main-api:${millLibVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millLibVersion}",
    ivy"com.lihaoyi::mill-scalalib-api:${millLibVersion}",
    ivy"com.lihaoyi::os-lib:0.7.0"
  )

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases")
  )

  def scalacOptions = Seq("-P:wartremover:only-warn-traverser:org.wartremover.warts.Unsafe")

  def scalacPluginIvyDeps = T { super.scalacPluginIvyDeps() ++ Agg(ivy"org.wartremover:::wartremover:2.4.7") }

  object test extends Tests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.7.4")

    def testFrameworks = Seq("utest.runner.Framework")
  }

  def publishVersion = "0.0.1-SNAPSHOT"

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
