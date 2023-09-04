package sbtindigo

import sbt.plugins.JvmPlugin
import sbt._

import indigoplugin.core.IndigoBuildSBT
import indigoplugin.core.IndigoCordova
import indigoplugin.core.IndigoRun
import indigoplugin.generators.EmbedText
import indigoplugin.generators.EmbedGLSLShaderPair

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

    // Config options
    val indigoOptions: SettingKey[IndigoOptions] =
      settingKey[IndigoOptions]("Config options for your Indigo game.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuild            := indigoBuildTask.value,
    indigoBuildFull        := indigoBuildFullTask.value,
    indigoRun              := indigoRunTask.value,
    indigoRunFull          := indigoRunFullTask.value,
    indigoCordovaBuild     := indigoCordovaBuildTask.value,
    indigoCordovaBuildFull := indigoCordovaBuildFullTask.value,
    indigoOptions          := IndigoOptions.defaults
  )

  object IndigoGenerators {

    def embedText(
        sourceManagedDir: File,
        moduleName: String,
        fullyQualifiedPackage: String,
        text: String
    ): Seq[File] =
      EmbedText.generate(os.Path(sourceManagedDir), moduleName, fullyQualifiedPackage, text).map(_.toIO)

    def embedGLSLShaderPair(
        sourceManagedDir: File,
        moduleName: String,
        fullyQualifiedPackage: String,
        vertexShaderPath: String,
        fragmentShaderPath: String,
        validateGLSL: Boolean
    ): Seq[File] =
      EmbedGLSLShaderPair
        .generate(
          os.Path(sourceManagedDir),
          moduleName,
          fullyQualifiedPackage,
          os.RelPath(vertexShaderPath).resolveFrom(os.pwd),
          os.RelPath(fragmentShaderPath).resolveFrom(os.pwd),
          validateGLSL
        )
        .map(_.toIO)

  }

  private def giveScriptBasePath(baseDir: String, scalaVersion: String, projectName: String): String = {
    val base =
      if (scalaVersion.startsWith("2"))
        s"$baseDir/target/scala-${scalaVersion.split('.').init.mkString(".")}"
      else
        s"$baseDir/target/scala-${scalaVersion}"

    val subDir = "/" + projectName + "-fastopt"

    if (new sbt.File(base + subDir).isDirectory) {
      base + subDir
    } else {
      base
    }
  }

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

      IndigoBuildSBT.build(
        os.Path(scriptPathBase),
        baseDir,
        indigoOptions.value,
        outputDir,
        List(
          "main.js",
          Keys.projectID.value.name + "-fastopt.js"
        )
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

      IndigoBuildSBT.build(
        os.Path(scriptPathBase),
        baseDir,
        indigoOptions.value,
        outputDir,
        List(
          "main.js",
          Keys.projectID.value.name + "-opt.js"
        )
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
        indigoOptions = indigoOptions.value
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
        indigoOptions = indigoOptions.value
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
        metadata = indigoOptions.value.metadata
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
        metadata = indigoOptions.value.metadata
      )
    }

}
