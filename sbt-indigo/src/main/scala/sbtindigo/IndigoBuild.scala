package sbtindigo

import org.apache.commons.io.FileUtils
import java.io.PrintWriter
import sbt.File
import sbt._
import indigoplugin.templates.HtmlTemplate

object IndigoBuild {

  def build(baseDir: String, templateOptions: TemplateOptions, outputFolderName: String, optimised: Boolean): Unit = {

    // create directory structure
    val directoryStructure = createDirectoryStructure(baseDir, outputFolderName)

    // copy built js file into scripts dir
    val newScriptName = copyScript(
      templateOptions,
      directoryStructure.artefacts,
      optimised
    )

    // copy built js source map file into scripts dir
    copySourceMap(templateOptions, directoryStructure.artefacts, optimised)

    // copy assets into folder
    copyAssets(templateOptions.gameAssetsDirectoryPath, directoryStructure.assets)

    // Fill out html template
    val html = HtmlTemplate.template(templateOptions.title, templateOptions.showCursor, newScriptName)

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

  def copyScript(templateOptions: TemplateOptions, desScriptsFolder: File, optimised: Boolean): String = {
    val suffix = if (optimised) "opt.js" else "fastopt.js"
    val path   = s"${templateOptions.scriptPathBase}-$suffix"
    val fileName = path
      .split('/')
      .toList
      .reverse
      .headOption
      .getOrElse(throw new Exception("Could not figure out script file name from: " + path))
    val scriptPath: File = new File(path)

    FileUtils.copyFileToDirectory(scriptPath, desScriptsFolder)

    fileName
  }

  def copySourceMap(templateOptions: TemplateOptions, desScriptsFolder: File, optimised: Boolean): Unit = {
    val suffix           = if (optimised) "opt.js.map" else "fastopt.js.map"
    val path             = s"${templateOptions.scriptPathBase}-$suffix"
    val scriptPath: File = new File(path)

    FileUtils.copyFileToDirectory(scriptPath, desScriptsFolder)
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

}

final case class TemplateOptions(
    title: String,
    showCursor: Boolean,
    scriptPathBase: String,
    gameAssetsDirectoryPath: String
)

object Utils {

  def ensureDirectoryAt(path: String): File = {
    val dirFile = new File(path)

    if (!dirFile.exists())
      dirFile.mkdir()

    dirFile
  }

}

final case class DirectoryStructure(base: File, assets: File, artefacts: File)
