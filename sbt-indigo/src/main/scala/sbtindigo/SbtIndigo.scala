package sbtindigo

import sbt.plugins.JvmPlugin
import sbt._

object SbtIndigo extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger   = allRequirements

  object autoImport {
    val indigoBuildJS: TaskKey[Unit]   = taskKey[Unit]("Build an Indigo JS game.")
    val indigoPublishJS: TaskKey[Unit] = taskKey[Unit]("Publish an Indigo game.")
    val indigoBuildJVM: TaskKey[Unit]  = taskKey[Unit]("Build an Indigo JVM game.")
    val gameAssetsDirectory: SettingKey[String] =
      settingKey[String]("Project relative path to a directory that contains all of the assets the game needs to load.")
    val showCursor: SettingKey[Boolean] = settingKey[Boolean]("Show the cursor? True by default.")
    val title: SettingKey[String]       = settingKey[String]("Title of your game. Defaults to 'Made with Indigo'.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuildJS := indigoBuildJSTask.value,
    indigoPublishJS := indigoPublishJSTask.value,
    indigoBuildJVM := indigoBuildJVMTask.value,
    showCursor := true,
    title := "Made with Indigo",
    gameAssetsDirectory := "."
  )

  lazy val indigoBuildJSTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String      = Keys.baseDirectory.value.getCanonicalPath
      val scalaVersion: String = Keys.scalaVersion.value
      val projectName: String  = Keys.projectID.value.name

      val scriptPathBase =
        s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName"

      println(scriptPathBase)

      IndigoBuildJS.build(
        baseDir,
        TemplateOptions(
          title = title.value,
          showCursor = showCursor.value,
          scriptPathBase = scriptPathBase,
          gameAssetsDirectoryPath =
            if (gameAssetsDirectory.value.startsWith("/")) gameAssetsDirectory.value
            else baseDir.replace("/.js", "") + "/" + gameAssetsDirectory.value
        )
      )
    }

  lazy val indigoPublishJSTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String      = Keys.baseDirectory.value.getCanonicalPath
      val scalaVersion: String = Keys.scalaVersion.value
      val projectName: String  = Keys.projectID.value.name

      val scriptPathBase =
        s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName"

      println(scriptPathBase)

      IndigoBuildJS.publish(
        baseDir,
        TemplateOptions(
          title = title.value,
          showCursor = showCursor.value,
          scriptPathBase = scriptPathBase,
          gameAssetsDirectoryPath =
            if (gameAssetsDirectory.value.startsWith("/")) gameAssetsDirectory.value
            else baseDir + "/" + gameAssetsDirectory.value
        )
      )
    }

  lazy val indigoBuildJVMTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String      = Keys.baseDirectory.value.getCanonicalPath
      val scalaVersion: String = Keys.scalaVersion.value
      val projectName: String  = Keys.projectID.value.name
      val projectVersion: String = Keys.version.value

      val scriptPathBase =
        s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName-assembly-$projectVersion"

      println(scriptPathBase)

      IndigoBuildJVM.build(
        baseDir,
        TemplateOptions(
          title = title.value,
          showCursor = showCursor.value,
          scriptPathBase = scriptPathBase,
          gameAssetsDirectoryPath =
            if (gameAssetsDirectory.value.startsWith("/")) gameAssetsDirectory.value
            else baseDir.replace("/.jvm", "") + "/" + gameAssetsDirectory.value
        )
      )
    }
}
