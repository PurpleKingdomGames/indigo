package sbtindigo

import sbt.plugins.JvmPlugin
import sbt._

object SbtIndigo extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger   = allRequirements

  object autoImport {
    val indigoBuild: TaskKey[Unit]     = taskKey[Unit]("Build an Indigo game.")
    val indigoBuildFull: TaskKey[Unit] = taskKey[Unit]("Build an Indigo game using full compression.")
    val indigoRun: TaskKey[Unit]       = taskKey[Unit]("Run an Indigo game.")
    val indigoRunFull: TaskKey[Unit]   = taskKey[Unit]("Run an Indigo game that has been compressed.")
    val gameAssetsDirectory: SettingKey[String] =
      settingKey[String]("Project relative path to a directory that contains all of the assets the game needs to load.")
    val showCursor: SettingKey[Boolean]    = settingKey[Boolean]("Show the cursor? True by default.")
    val title: SettingKey[String]          = settingKey[String]("Title of your game. Defaults to 'Made with Indigo'.")
    val windowStartWidth: SettingKey[Int]  = settingKey[Int]("Initial window width. Defaults to 550 pixels.")
    val windowStartHeight: SettingKey[Int] = settingKey[Int]("Initial window height. Defaults to 400 pixels.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuild := { indigoBuildTask.value; () },
    indigoBuildFull := { indigoBuildFullTask.value; () },
    indigoRun := indigoRunTask.value,
    indigoRunFull := indigoRunFullTask.value,
    showCursor := true,
    title := "Made with Indigo",
    gameAssetsDirectory := ".",
    windowStartWidth := 550,
    windowStartHeight := 400
  )

  def giveScriptBasePath(baseDir: String, scalaVersion: String, projectName: String): String =
    s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName"

  lazy val indigoBuildTask: Def.Initialize[Task[String]] =
    Def.task {
      val baseDir: String   = Keys.baseDirectory.value.getCanonicalPath
      val outputDir: String = "indigoBuild"

      val scriptPathBase =
        giveScriptBasePath(
          baseDir = baseDir,
          scalaVersion = Keys.scalaVersion.value,
          projectName = Keys.projectID.value.name
        )

      println(scriptPathBase)

      IndigoBuild.build(
        baseDir,
        TemplateOptions(
          title = title.value,
          showCursor = showCursor.value,
          scriptPathBase = scriptPathBase,
          gameAssetsDirectoryPath =
            if (gameAssetsDirectory.value.startsWith("/"))
              gameAssetsDirectory.value
            else baseDir.replace("/.js", "") + "/" + gameAssetsDirectory.value
        ),
        outputDir,
        false
      )

      baseDir + "/target/" + outputDir
    }

  lazy val indigoBuildFullTask: Def.Initialize[Task[String]] =
    Def.task {
      val baseDir: String   = Keys.baseDirectory.value.getCanonicalPath
      val outputDir: String = "indigoBuildFull"

      val scriptPathBase =
        giveScriptBasePath(
          baseDir = baseDir,
          scalaVersion = Keys.scalaVersion.value,
          projectName = Keys.projectID.value.name
        )

      println(scriptPathBase)

      IndigoBuild.build(
        baseDir,
        TemplateOptions(
          title = title.value,
          showCursor = showCursor.value,
          scriptPathBase = scriptPathBase,
          gameAssetsDirectoryPath =
            if (gameAssetsDirectory.value.startsWith("/"))
              gameAssetsDirectory.value
            else baseDir + "/" + gameAssetsDirectory.value
        ),
        outputDir,
        true
      )

      baseDir + "/target/" + outputDir
    }

  lazy val indigoRunTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String    = Keys.baseDirectory.value.getCanonicalPath
      val outputDir: os.Path = os.Path(baseDir) / "target" / "indigoRun"
      val buildDir: os.Path  = os.Path(indigoBuildTask.value)

      IndigoRun.run(
        outputDir = outputDir,
        buildDir = buildDir,
        title = title.value,
        windowWidth = windowStartWidth.value,
        windowHeight = windowStartHeight.value
      )
    }

  lazy val indigoRunFullTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String    = Keys.baseDirectory.value.getCanonicalPath
      val outputDir: os.Path = os.Path(baseDir) / "target" / "indigoRunFull"
      val buildDir: os.Path  = os.Path(indigoBuildFullTask.value)

      IndigoRun.run(
        outputDir = outputDir,
        buildDir = buildDir,
        title = title.value,
        windowWidth = windowStartWidth.value,
        windowHeight = windowStartHeight.value
      )
    }

}
