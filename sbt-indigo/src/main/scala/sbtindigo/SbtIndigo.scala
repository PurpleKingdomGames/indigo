package indigoplugin

import sbt.plugins.JvmPlugin
import sbt._
import sbt.Keys._

import indigoplugin.core.IndigoBuildSBT
import indigoplugin.core.IndigoCordova
import indigoplugin.core.IndigoRun

object SbtIndigo extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger   = allRequirements

  object autoImport {

    // Build and Run tasks
    val indigoBuild: TaskKey[String] =
      taskKey[String]("Build an Indigo game. Returns output directory.")
    val indigoBuildFull: TaskKey[String] =
      taskKey[String]("Build an Indigo game using full compression. Returns output directory.")
    val indigoRun: TaskKey[Unit]          = taskKey[Unit]("Run an Indigo game.")
    val indigoRunFull: TaskKey[Unit]      = taskKey[Unit]("Run an Indigo game that has been compressed.")
    val indigoCordovaBuild: TaskKey[Unit] = taskKey[Unit]("Build an Indigo game Cordova template.")
    val indigoCordovaBuildFull: TaskKey[Unit] =
      taskKey[Unit]("Build an Indigo game Cordova template that has been compressed.")

    /** Location of your Indigo game's assets folder. */
    val indigoAssets: SettingKey[sbt.File] =
      settingKey[sbt.File]("Location of your Indigo game's assets folder.")

    /** Configuration options for your Indigo game. */
    val indigoOptions: SettingKey[sbt.File => IndigoOptions] =
      settingKey[sbt.File => IndigoOptions]("Configuration options for your Indigo game.")

    /** Indigo source code generators. */
    val indigoGenerators: SettingKey[sbt.File => IndigoGenerators] =
      settingKey[sbt.File => IndigoGenerators]("Indigo source code generators.")

  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuild            := indigoBuildTask.value,
    indigoBuildFull        := indigoBuildFullTask.value,
    indigoRun              := indigoRunTask.value,
    indigoRunFull          := indigoRunFullTask.value,
    indigoCordovaBuild     := indigoCordovaBuildTask.value,
    indigoCordovaBuildFull := indigoCordovaBuildFullTask.value,
    indigoAssets           := new sbt.File(Keys.baseDirectory.value.getCanonicalPath + "/assets"),
    indigoOptions          := ((_: sbt.File) => IndigoOptions.defaults),
    indigoGenerators       := ((_: sbt.File) => IndigoGenerators.None),
    Compile / sourceGenerators += Def.task {
      val cacheDir     = (Compile / sourceManaged).value / s"indigo-generated-${Keys.projectID.value.name}"
      val assetsFolder = indigoAssets.value

      indigoGenerators
        .value(assetsFolder)
        .toSourceFiles(indigoOptions.value(assetsFolder), assetsFolder, cacheDir)
    }
  )

  private def giveScriptBasePath(
      baseDir: String,
      scalaVersion: String,
      projectName: String,
      suffix: String
  ): String = {
    val base =
      if (scalaVersion.startsWith("2"))
        s"$baseDir/target/scala-${scalaVersion.split('.').init.mkString(".")}"
      else
        s"$baseDir/target/scala-${scalaVersion}"

    val subDir = "/" + projectName + suffix

    if (new sbt.File(base + subDir).isDirectory) {
      base + subDir
    } else {
      base
    }
  }

  lazy val indigoBuildTask: Def.Initialize[Task[String]] =
    Def.task {
      val baseDirectory: os.Path   = os.Path(Keys.baseDirectory.value.getCanonicalPath)
      val outputDirectory: String  = "indigoBuild"
      val assetsDirectory: os.Path = os.Path(indigoAssets.value)

      val scriptPathBase =
        giveScriptBasePath(
          baseDir = baseDirectory.toString(),
          scalaVersion = Keys.scalaVersion.value,
          projectName = Keys.projectID.value.name,
          suffix = "-fastopt"
        )

      println(scriptPathBase)

      IndigoBuildSBT.build(
        os.Path(scriptPathBase),
        indigoOptions.value(indigoAssets.value),
        assetsDirectory,
        baseDirectory,
        outputDirectory,
        List(
          "main.js",
          Keys.projectID.value.name + "-fastopt.js"
        )
      )

      baseDirectory + "/target/" + outputDirectory
    }

  lazy val indigoBuildFullTask: Def.Initialize[Task[String]] =
    Def.task {
      val baseDirectory: os.Path   = os.Path(Keys.baseDirectory.value.getCanonicalPath)
      val outputDirectory: String  = "indigoBuildFull"
      val assetsDirectory: os.Path = os.Path(indigoAssets.value)

      val scriptPathBase =
        giveScriptBasePath(
          baseDir = baseDirectory.toString(),
          scalaVersion = Keys.scalaVersion.value,
          projectName = Keys.projectID.value.name,
          suffix = "-opt"
        )

      println(scriptPathBase)

      IndigoBuildSBT.build(
        os.Path(scriptPathBase),
        indigoOptions.value(indigoAssets.value),
        assetsDirectory,
        baseDirectory,
        outputDirectory,
        List(
          "main.js",
          Keys.projectID.value.name + "-opt.js"
        )
      )

      baseDirectory + "/target/" + outputDirectory
    }

  lazy val indigoRunTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String    = Keys.baseDirectory.value.getCanonicalPath
      val outputDir: os.Path = os.Path(baseDir) / "target" / "indigoRun"
      val buildDir: os.Path  = os.Path(indigoBuildTask.value)

      IndigoRun.run(
        outputDir = outputDir,
        buildDir = buildDir,
        indigoOptions = indigoOptions.value(indigoAssets.value)
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
        indigoOptions = indigoOptions.value(indigoAssets.value)
      )
    }

  lazy val indigoCordovaBuildTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String    = Keys.baseDirectory.value.getCanonicalPath
      val outputDir: os.Path = os.Path(baseDir) / "target" / "indigoCordovaBuild"
      val buildDir: os.Path  = os.Path(indigoBuildTask.value)

      IndigoCordova.run(
        outputDir = outputDir,
        buildDir = buildDir,
        metadata = indigoOptions.value(indigoAssets.value).metadata
      )
    }

  lazy val indigoCordovaBuildFullTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String    = Keys.baseDirectory.value.getCanonicalPath
      val outputDir: os.Path = os.Path(baseDir) / "target" / "indigoCordovaBuildFull"
      val buildDir: os.Path  = os.Path(indigoBuildFullTask.value)

      IndigoCordova.run(
        outputDir = outputDir,
        buildDir = buildDir,
        metadata = indigoOptions.value(indigoAssets.value).metadata
      )
    }

}
