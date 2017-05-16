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
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuild := indigoBuildTask.value
  )

  lazy val indigoBuildTask: Def.Initialize[Task[Unit]] =
    Def.task {
      IndigoBuild.build(
        TemplateOptions(
          title = "Made with Indigo",
          showCursor = true,
          scriptPath = "./target/scala-2.12/indigo-sandbox-fastopt.js",
          entryPoint = "com.example.sandbox.MyGame().main();"
        )
      )
    }
}

object IndigoBuild {

  def build(templateOptions: TemplateOptions): Unit = {

    // create directory structure
    val dirPath = createDirectoryStructure()

    // copy built js file into scripts dir

    // copy assets into folder

    // Fill out html template
    val html = template(templateOptions)

    // Write out file
    writeHtml(dirPath, html)

  }

  def createDirectoryStructure(): String = {
    //TODO: where to get the sub project path from?
    val dirPath = "target/indigo"

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

  def writeHtml(dirPath: String, html: String): Unit = {
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

    ()
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
      |      ${if(options.showCursor) "canvas { cursor: none }" }
      |    </style>
      |  </head>
      |  <body>
      |    <script type="text/javascript" src="${options.scriptPath}"></script>
      |    <script type="text/javascript">
      |      ${options.entryPoint}
      |    </script>
      |  </body>
      |</html>
    """.stripMargin

}

case class TemplateOptions(title: String, showCursor: Boolean, scriptPath: String, entryPoint: String)
