import sbt.{Def, _}
import sbtwelcome.UsefulTask
import sbtwelcome.WelcomePlugin.autoImport._

import scala.sys.process._

object Misc {

  lazy val code =
    taskKey[Unit]("Launch VSCode in the current directory")

  // Define task to copy html files
  lazy val copyApiDocs =
    taskKey[Unit]("Copy html files from src/main/html to cross-version target directory")

  def codeTaskDefinition: Int = {
    val command = Seq("code", ".")
    val run = sys.props("os.name").toLowerCase match {
      case x if x contains "windows" => Seq("cmd", "/C") ++ command
      case _                         => command
    }
    run.!
  }

  def copyApiDocsTaskDefinition(file: File): Unit = {

    println("Copy docs from 'target/scala-3.1.0/unidoc' to 'target/scala-3.1.0/site-docs/api'")

    val src = file / "scala-3.1.0" / "unidoc"
    val dst = file / "scala-3.1.0" / "site-docs" / "api"

    IO.copyDirectory(src, dst)
  }

  lazy val customTasksAliases = Seq(
    UsefulTask("", "cleanAll", "Clean all the projects"),
    UsefulTask("", "buildAllNoClean", "Rebuild without cleaning"),
    UsefulTask("", "testAllNoClean", "Test all without cleaning"),
    UsefulTask("", "crossLocalPublishNoClean", "Locally publish the core modules"),
    UsefulTask("", "gendocs", "Rebuild the API and markdown docs"),
    UsefulTask("", "sandboxRun", "Run the sandbox game (fastOptJS + Electron)"),
    UsefulTask("", "perfRun", "Run the perf game (fastOptJS + Electron)"),
    UsefulTask("", "code", "Launch VSCode")
  )

  def presentationSettings(version: SettingKey[String]): Seq[Def.Setting[String]] = {
     val rawLogo: String =
      """
        |      //                  //  //
        |                         //
        |    //  //////      //////  //    ////      //////
        |   //  //    //  //    //  //  //    //  //    //
        |  //  //    //  ////////  //  ////////  //////
        |                                   //
        |                            //////
        |""".stripMargin

    Seq(
      logo             := rawLogo + s"version ${version.value}",
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.CYAN,
      commandColor     := scala.Console.BLUE_B,
      descriptionColor := scala.Console.WHITE
    )
  }
}
