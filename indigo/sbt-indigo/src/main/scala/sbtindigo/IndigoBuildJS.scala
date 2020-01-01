package sbtindigo

import org.apache.commons.io.FileUtils
import java.io.PrintWriter
import sbt.File
// import sbt.plugins.JvmPlugin
import sbt._

object IndigoBuildJS {

  def build(baseDir: String, templateOptions: TemplateOptions): Unit = {

    // create directory structure
    val directoryStructure = createDirectoryStructure(baseDir, "indigo-js")

    // copy built js file into scripts dir
    val newScriptPath = copyScript(templateOptions, directoryStructure.artefacts, "fastopt")

    // copy built js source map file into scripts dir
    copySourceMap(templateOptions, directoryStructure.artefacts)

    // copy assets into folder
    copyAssets(templateOptions.gameAssetsDirectoryPath, directoryStructure.assets)

    // Fill out html template
    val html = template(templateOptions.copy(scriptPathBase = newScriptPath))

    // Write out file
    val outputPath = writeHtml(directoryStructure, html)

    println(outputPath)
  }

  def publish(baseDir: String, templateOptions: TemplateOptions): Unit = {

    // create directory structure
    val directoryStructure = createDirectoryStructure(baseDir, "indigo-published")

    // copy built js file into scripts dir
    val newScriptPath = copyScript(templateOptions, directoryStructure.artefacts, "opt")

    // copy assets into folder
    copyAssets(templateOptions.gameAssetsDirectoryPath, directoryStructure.assets)

    // Fill out html template
    val html = template(templateOptions.copy(scriptPathBase = newScriptPath))

    // Write out file
    val outputPath = writeHtml(directoryStructure, html)

    println(outputPath)
  }

  def createDirectoryStructure(baseDir: String, outputFolderName: String): DirectoryStructure = {
    val dirPath = baseDir + "/target/" + outputFolderName

    println("dirPath: " + dirPath)

    DirectoryStructure(
      Utils.ensureDirectoryAt(dirPath),
      Utils.ensureDirectoryAt(dirPath + "/assets"),
      Utils.ensureDirectoryAt(dirPath + "/scripts")
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

  def copyScript(templateOptions: TemplateOptions, desScriptsFolder: File, jsType: String): String = {
    val path = s"${templateOptions.scriptPathBase}-$jsType.js"
    val fileName = path
      .split('/')
      .toList
      .reverse
      .headOption
      .getOrElse(throw new Exception("Could not figure out script file name from: " + path))
    val scriptPath: File = new File(path)

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
    val scriptPath: File = new File(path)

    FileUtils.copyFileToDirectory(scriptPath, desScriptsFolder)

    desScriptsFolder.getCanonicalPath + "/" + fileName
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
      |    <div id="indigo-container"></div>
      |    <script type="text/javascript" src="${options.scriptPathBase}"></script>
      |  </body>
      |</html>
    """.stripMargin

}
