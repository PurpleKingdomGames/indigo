package indigoplugin

import os._
import indigoplugin.templates.HtmlTemplate
import indigoplugin.templates.SupportScriptTemplate

object IndigoBuild {

  def build(templateOptions: TemplateOptions, directoryStructure: DirectoryStructure, newScriptName: String): Unit = {

    // copy built js file into scripts dir
    IndigoBuild.copyScript(templateOptions, directoryStructure.artefacts, newScriptName)

    // copy assets into folder
    IndigoBuild.copyAssets(templateOptions.gameAssetsDirectoryPath, directoryStructure.assets)

    // copy built js source map file into scripts dir
    IndigoBuild.copyScript(
      templateOptions,
      directoryStructure.artefacts,
      newScriptName + ".map"
    )

    // Write an empty cordova.js file so the script reference is intact,
    // even though it does nothing here.
    os.write(directoryStructure.base / "cordova.js", "")

    // Write support js script
    val support = SupportScriptTemplate.template(false)
    os.write(directoryStructure.base / "scripts" / "indigo-support.js", support)

    // Fill out html template
    val html = HtmlTemplate.template(templateOptions.title, templateOptions.showCursor, newScriptName)

    // Write out file
    val outputPath = IndigoBuild.writeHtml(directoryStructure, html)

    println(outputPath.toString())
  }

  def createDirectoryStructure(baseDir: Path): DirectoryStructure = {
    println("dirPath: " + baseDir.toString())

    DirectoryStructure(
      Utils.ensureDirectoryAt(baseDir),
      Utils.ensureDirectoryAt(baseDir / "assets"),
      Utils.ensureDirectoryAt(baseDir / "scripts")
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def copyAssets(gameAssetsDirectoryPath: Path, destAssetsFolder: Path): Unit =
    if (!os.exists(gameAssetsDirectoryPath))
      throw new Exception("Supplied game assets path does not exist: " + gameAssetsDirectoryPath.toString())
    else if (!os.isDir(gameAssetsDirectoryPath))
      throw new Exception("Supplied game assets path was not a directory")
    else {
      println("Copying assets...")
      os.copy(gameAssetsDirectoryPath, destAssetsFolder, true, true, true)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def copyScript(templateOptions: TemplateOptions, destScriptsFolder: Path, fileName: String): Unit = {
    val scriptFile = templateOptions.scriptPathBase / fileName

    if (os.exists(scriptFile))
      os.copy(scriptFile, destScriptsFolder / fileName)
    else
      throw new Exception("Script file does not exist, have you compiled the JS file? Tried: " + scriptFile.toString())
  }

  def writeHtml(directoryStructure: DirectoryStructure, html: String): Path = {

    val outFile = directoryStructure.base / "index.html"

    os.write(outFile, html)

    outFile
  }

}
