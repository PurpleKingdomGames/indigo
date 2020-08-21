package millindigo

import os._
import indigoplugin.templates.HtmlTemplate

object IndigoBuild {

  def build(baseDir: Path, templateOptions: TemplateOptions): Unit = {

    // create directory structure
    val directoryStructure = createDirectoryStructure(baseDir)

    // copy built js file into scripts dir
    copyScript(directoryStructure.base, templateOptions, directoryStructure.artefacts)

    // copy assets into folder
    copyAssets(templateOptions.gameAssetsDirectoryPath, directoryStructure.assets)

    // Fill out html template
    val html = HtmlTemplate.template(templateOptions.title, templateOptions.showCursor, "out.js")

    // Write out file
    val outputPath = writeHtml(directoryStructure, html)

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
  def copyScript(base: Path, templateOptions: TemplateOptions, destScriptsFolder: Path): Unit = {
    val scriptFile = templateOptions.scriptPathBase / "out.js"

    if (os.exists(scriptFile)) {
      os.copy(scriptFile, destScriptsFolder / "out.js")
    } else {
      throw new Exception("Script file does not exist, have you compiled the JS file? Tried: " + scriptFile.toString())
    }
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

  def writeHtml(directoryStructure: DirectoryStructure, html: String): Path = {

    val outFile = directoryStructure.base / "index.html"

    os.write(outFile, html)

    outFile
  }

}

final case class TemplateOptions(
    title: String,
    showCursor: Boolean,
    scriptPathBase: Path,
    gameAssetsDirectoryPath: Path
)

object Utils {

  def ensureDirectoryAt(path: Path): Path = {
    val outDir = os.pwd / "out" / "test"
    os.remove.all(path)
    os.makeDir.all(path)

    path
  }

}

final case class DirectoryStructure(base: Path, assets: Path, artefacts: Path)
