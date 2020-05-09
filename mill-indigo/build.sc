import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import publish._
import coursier.maven.MavenRepository

object `mill-indigo` extends ScalaModule with PublishModule {
  def scalaVersion = "2.12.10"
  def millVersion = "0.6.0"

  def ivyDeps = Agg(
    ivy"com.lihaoyi::mill-main:${millVersion}",
    ivy"com.lihaoyi::mill-main-api:${millVersion}",
    ivy"com.lihaoyi::mill-scalalib:${millVersion}",
    ivy"com.lihaoyi::mill-scalalib-api:${millVersion}",
    ivy"com.lihaoyi::os-lib:0.6.2"
  )

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases")
  )

  def scalacOptions = Seq("-P:wartremover:only-warn-traverser:org.wartremover.warts.Unsafe")

  // This line breaks the Scala.js fastOpt build.
  def scalacPluginIvyDeps = T { super.scalacPluginIvyDeps() ++ Agg(ivy"org.wartremover:::wartremover:2.4.7") }

  object test extends Tests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.7.4")

    def testFrameworks = Seq("utest.runner.Framework")
  }

  def publishVersion = "0.0.1-SNAPSHOT"

  def pomSettings = PomSettings(
    description = "mill-indigo",
    organization = "indigo",
    url = "",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("purple-kingdom-games", "indigo"),
    developers = Seq(
      Developer("memyself", "Me Myself", "https://github.com/davesmith00000")
    )
  )

}
