package com.purplekingdomgames.sbtindigo

import java.io.PrintWriter

import sbt.plugins.JvmPlugin
import sbt.{File, _}

object SbtIndigo extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  // TODO
  // Settings for game title
  // Asset directory
  // Hide / Show mouse cursor

  object autoImport {
    val indigoBuild: TaskKey[Unit] = taskKey[Unit]("Build an indigo game")
    val entryPoint: SettingKey[String] = settingKey[String]("The fully qualified path to the Game class")
    val showCursor: SettingKey[Boolean] = settingKey[Boolean]("Show the cursor? True by default.")
    val title: SettingKey[String] = settingKey[String]("Title of your game. Defaults to 'Made with Indigo'")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuild := indigoBuildTask.value,
    showCursor := true,
    entryPoint := "",
    title := "Made with Indigo"
  )

  lazy val indigoBuildTask: Def.Initialize[Task[Unit]] =
    Def.task {

      if(entryPoint.value.isEmpty) println("The entryKey must be set")
      else {

        val baseDir: String = Keys.baseDirectory.value.getCanonicalPath
        val scalaVersion: String = Keys.scalaVersion.value
        val projectName: String = Keys.projectID.value.name

        val scriptPathBase = s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName"

        println(scriptPathBase)

        IndigoBuild.build(
          baseDir,
          TemplateOptions(
            title = title.value,
            showCursor = showCursor.value,
            scriptPathBase = scriptPathBase,
            entryPoint = entryPoint.value + "().main();"
          )
        )

      }
    }
}

object IndigoBuild {

  def build(baseDir: String, templateOptions: TemplateOptions): Unit = {

    // create directory structure
    val dirPath = createDirectoryStructure(baseDir)

    // copy built js file into scripts dir
    //<base>/target/scala-<version>/<project-name>-fastopt.js
    //<base>/target/scala-<version>/<project-name>-fastopt.js.map
    //<base>/target/scala-<version>/<project-name>-jsdeps.js
//    println(Keys.baseDirectory.value)
//    println(Keys.scalaVersion.value)
//    println(Keys.projectID.value)

    // copy assets into folder

    // Fill out html template
    val html = template(templateOptions)

    // Write out file
    val outputPath = writeHtml(dirPath, html)

    println(outputPath)
  }

  def createDirectoryStructure(baseDir: String): String = {
    //TODO: where to get the sub project path from?
    val dirPath = baseDir + "/target/indigo"

    println("dirPath: " + dirPath)

    ensureDirectoryAt(dirPath)
    ensureDirectoryAt(dirPath + "/scripts")
    ensureDirectoryAt(dirPath + "/assets")

    dirPath
  }

  private def ensureDirectoryAt(path: String): Unit = {
    val dirFile = new File(path)

    if (!dirFile.exists()) {
      dirFile.mkdir()
    }
  }

  def writeHtml(dirPath: String, html: String): String = {
    val relativePath = dirPath + "/index.html"
    val file = new File(relativePath)

    if (file.exists()) {
      file.delete()
    }

    file.createNewFile()

    new PrintWriter(relativePath) {
      write(html)
      close()
    }

    file.getCanonicalPath
  }

  val template: TemplateOptions => String = options =>
    s"""<!DOCTYPE html>
      |<html>
      |  <head>
      |    <meta charset="UTF-8">
      |    <title>${options.title}</title>
      |    <style>
      |      body {
      |        padding:0px;
      |        margin:0px;
      |      }
      |
      |      ${if(!options.showCursor) "canvas { cursor: none }" else "" }
      |    </style>
      |  </head>
      |  <body>
      |    <script type="text/javascript" src="${options.scriptPathBase}-fastopt.js"></script>
      |    <script type="text/javascript">
      |      ${options.entryPoint}
      |    </script>
      |  </body>
      |</html>
    """.stripMargin

}

case class TemplateOptions(title: String, showCursor: Boolean, scriptPathBase: String, entryPoint: String)
