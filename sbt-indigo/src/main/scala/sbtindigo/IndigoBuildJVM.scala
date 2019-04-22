package sbtindigo

import org.apache.commons.io.FileUtils
import java.io.PrintWriter
import sbt.File
// import sbt.plugins.JvmPlugin
import sbt._


object IndigoBuildJVM {

  def build(baseDir: String, templateOptions: TemplateOptions): Unit = {

    // create directory structure
    val directoryStructure = createDirectoryStructure(baseDir, "indigo-jvm")

    // copy built fat jar file into scripts dir
    val newScriptPath = copyFatJar(templateOptions, directoryStructure.artefacts)

    // copy assets into folder
    copyAssets(templateOptions.gameAssetsDirectoryPath, directoryStructure.assets)

    // Fill out launch script template
    val launchScript = template(templateOptions.copy(scriptPathBase = newScriptPath))

    // Write out file
    val outputPath = writeLaunchScript(directoryStructure, launchScript)

    println(outputPath)
  }

  def createDirectoryStructure(baseDir: String, outputFolderName: String): DirectoryStructure = {
    val dirPath = baseDir + "/target/" + outputFolderName

    println("dirPath: " + dirPath)

    DirectoryStructure(
      Utils.ensureDirectoryAt(dirPath),
      Utils.ensureDirectoryAt(dirPath + "/assets"),
      Utils.ensureDirectoryAt(dirPath + "/game")
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

  def copyFatJar(templateOptions: TemplateOptions, destinationFolder: File): String = {
    val path = s"${templateOptions.scriptPathBase}.jar"
    val fileName = path
      .split('/')
      .toList
      .reverse
      .headOption
      .getOrElse(throw new Exception("Could not figure out fat jar file name from: " + path))
    val outputPath: File = new File(path)

    FileUtils.copyFileToDirectory(outputPath, destinationFolder)

    "game/" + fileName
  }

  def writeLaunchScript(directoryStructure: DirectoryStructure, html: String): String = {
    val relativePath = directoryStructure.base.getCanonicalPath + "/launch.sh"
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

  val template: TemplateOptions => String = options => s"""#/bin/bash
      |
      |java -jar ${options.scriptPathBase}
    """.stripMargin

}