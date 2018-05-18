package com.purplekingdomgames.sbtindigo

import java.io.PrintWriter

import org.apache.commons.io.FileUtils
import sbt.plugins.JvmPlugin
import sbt.{File, _}

object SbtIndigo extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger   = allRequirements

  // TODO
  // Settings for game title
  // Asset directory
  // Hide / Show mouse cursor

  object autoImport {
    val indigoBuild: TaskKey[Unit] = taskKey[Unit]("Build an indigo game.")
    val gameAssetsDirectory: SettingKey[String] =
      settingKey[String]("Project relative path to a directory that contains all of the assets the game needs to load.")
    val showCursor: SettingKey[Boolean] = settingKey[Boolean]("Show the cursor? True by default.")
    val title: SettingKey[String]       = settingKey[String]("Title of your game. Defaults to 'Made with Indigo'.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuild := indigoBuildTask.value,
    showCursor := true,
    title := "Made with Indigo",
    gameAssetsDirectory := "."
  )

  lazy val indigoBuildTask: Def.Initialize[Task[Unit]] =
    Def.task {

      val baseDir: String      = Keys.baseDirectory.value.getCanonicalPath
      val scalaVersion: String = Keys.scalaVersion.value
      val projectName: String  = Keys.projectID.value.name

      val scriptPathBase = s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName"

      println(scriptPathBase)

      IndigoBuild.build(
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
}

object IndigoBuild {

  def build(baseDir: String, templateOptions: TemplateOptions): Unit = {

    // create directory structure
    val directoryStructure = createDirectoryStructure(baseDir)

    // copy built js file into scripts dir
    val newScriptPath = copyScript(templateOptions, directoryStructure.scripts)

    // copy built js source map file into scripts dir
    copySourceMap(templateOptions, directoryStructure.scripts)

    // copy assets into folder
    copyAssets(templateOptions.gameAssetsDirectoryPath, directoryStructure.assets)

    // Fill out html template
    val html = template(templateOptions.copy(scriptPathBase = newScriptPath))

    // Write out file
    val outputPath = writeHtml(directoryStructure, html)

    println(outputPath)
  }

  case class DirectoryStructure(base: File, assets: File, scripts: File)

  def createDirectoryStructure(baseDir: String): DirectoryStructure = {
    val dirPath = baseDir + "/target/indigo"

    println("dirPath: " + dirPath)

    DirectoryStructure(
      ensureDirectoryAt(dirPath),
      ensureDirectoryAt(dirPath + "/assets"),
      ensureDirectoryAt(dirPath + "/scripts")
    )
  }

  def copyAssets(gameAssetsDirectoryPath: String, destAssetsFolder: File): Unit = {
    val dirFile = new File(gameAssetsDirectoryPath)

    if (!dirFile.exists())
      throw new Exception("Supplied game assets path does not exist: " + dirFile.getPath)
    else if (!dirFile.isDirectory)
      throw new Exception("Supplied game assets path was not a directory")
    else {
      println("Copying assets...")
      FileUtils.copyDirectory(dirFile, destAssetsFolder)
    }
  }

  def copyScript(templateOptions: TemplateOptions, desScriptsFolder: File): String = {
    val path = s"${templateOptions.scriptPathBase}-fastopt.js"
    val fileName = path
      .split('/')
      .toList
      .reverse
      .headOption
      .getOrElse(throw new Exception("Could not figure out script file name from: " + path))
    val scriptPath: File = new File(path) // TODO: This only covers the fast opt JS version

    FileUtils.copyFileToDirectory(scriptPath, desScriptsFolder)

    "scripts/" + fileName
  }

  def copySourceMap(templateOptions: TemplateOptions, desScriptsFolder: File): String = {
    val path = s"${templateOptions.scriptPathBase}-fastopt.js.map"
    val fileName = path
      .split('/')
      .toList
      .reverse
      .headOption
      .getOrElse(throw new Exception("Could not figure out source map file name from: " + path))
    val scriptPath: File = new File(path) //TODO: This only covers the fast opt JS version

    FileUtils.copyFileToDirectory(scriptPath, desScriptsFolder)

    desScriptsFolder.getCanonicalPath + "/" + fileName
  }

  private def ensureDirectoryAt(path: String): File = {
    val dirFile = new File(path)

    if (!dirFile.exists())
      dirFile.mkdir()

    dirFile
  }

  def writeHtml(directoryStructure: DirectoryStructure, html: String): String = {
    val relativePath = directoryStructure.base.getCanonicalPath + "/index.html"
    val file         = new File(relativePath)

    if (file.exists())
      file.delete()

    file.createNewFile()

    new PrintWriter(relativePath) {
      write(html)
      close()
    }

    file.getCanonicalPath
  }

  val template: TemplateOptions => String = options => s"""<!DOCTYPE html>
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
      |      ${if (!options.showCursor) "canvas { cursor: none }" else ""}
      |    </style>
      |  </head>
      |  <body>
      |    <script type="text/javascript">
      |window.onload = function () {
      |    if (typeof history.pushState === "function") {
      |        history.pushState("jibberish", null, null);
      |        window.onpopstate = function () {
      |            history.pushState('newjibberish', null, null);
      |            // Handle the back (or forward) buttons here
      |            // Will NOT handle refresh, use onbeforeunload for this.
      |        };
      |    }
      |    else {
      |        var ignoreHashChange = true;
      |        window.onhashchange = function () {
      |            if (!ignoreHashChange) {
      |                ignoreHashChange = true;
      |                window.location.hash = Math.random();
      |                // Detect and redirect change here
      |                // Works in older FF and IE9
      |                // * it does mess with your hash symbol (anchor?) pound sign
      |                // delimiter on the end of the URL
      |            }
      |            else {
      |                ignoreHashChange = false;
      |            }
      |        };
      |    }
      |}
      |    </script>
      |    <script type="text/javascript" src="${options.scriptPathBase}"></script>
      |  </body>
      |</html>
    """.stripMargin

}

case class TemplateOptions(title: String, showCursor: Boolean, scriptPathBase: String, gameAssetsDirectoryPath: String)
