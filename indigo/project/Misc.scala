import sbt.{Def, _}
import sbtwelcome.UsefulTask
import sbtwelcome.WelcomePlugin.autoImport._

import scala.sys.process._

object Misc {

  lazy val code =
    taskKey[Unit]("Launch VSCode in the current directory")

  def codeTaskDefinition: Int = {
    val command = Seq("code", ".")
    val run = sys.props("os.name").toLowerCase match {
      case x if x contains "windows" => Seq("cmd", "/C") ++ command
      case _                         => command
    }
    run.!
  }

  lazy val customTasksAliases = Seq(
    UsefulTask("cleanAll", "Clean all the projects").noAlias,
    UsefulTask("buildAllNoClean", "Rebuild without cleaning").noAlias,
    UsefulTask("testAllNoClean", "Test all without cleaning").noAlias,
    UsefulTask("crossLocalPublishNoClean", "Locally publish the core modules").noAlias,
    UsefulTask("gendocs", "Rebuild the API and markdown docs").noAlias,
    UsefulTask("sandboxRun", "Run the sandbox game (fastOptJS + Electron)").noAlias,
    UsefulTask("perfRun", "Run the perf game (fastOptJS + Electron)").noAlias,
    UsefulTask("shaderRun", "Run the shader game (fastOptJS + Electron)").noAlias,
    UsefulTask("physicsRun", "Run the physics game (fastOptJS + Electron)").noAlias,
    UsefulTask("tyrianSandboxBuild", "build the Tyrian sandbox").noAlias,
    UsefulTask("scalafmtCheckAll", "Check formatting").noAlias,
    UsefulTask("code", "Launch VSCode").noAlias
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
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )
  }
}
