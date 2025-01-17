package indigoplugin.core

import os._
import indigoplugin.templates.HtmlTemplate
import indigoplugin.templates.SupportScriptTemplate
import indigoplugin.datatypes.DirectoryStructure
import indigoplugin.utils.Utils
import indigoplugin.IndigoOptions
import java.nio.file.LinkOption
import java.nio.file.StandardCopyOption
import indigoplugin.IndigoAssets
import indigoplugin.IndigoTemplate.Custom
import indigoplugin.IndigoTemplate.Default

object IndigoBuild {

  private val workspaceDir = Utils.findWorkspace

  def build(
      scriptPathBase: Path,
      options: IndigoOptions,
      baseDir: Path,
      scriptNames: List[String]
  ): Unit =
    options.template match {
      case Custom(inputs, outputs) =>
        println("Building using custom template.")

        if (!os.isDir(inputs.templateSource)) {
          throw new Exception(
            s"The supplied path to the template source directory is not a directory: ${inputs.templateSource.toString}"
          )
        } else if (!os.exists(inputs.templateSource)) {
          throw new Exception(
            s"The supplied path to the template source directory does not exist: ${inputs.templateSource.toString}"
          )
        } else {
          println("Copying template files...")
          os.copy.over(inputs.templateSource, baseDir)
        }

        val scriptName = findScriptName(scriptNames, scriptPathBase)

        // copy the game files
        val gameScriptsDest = outputs.gameScripts.resolveFrom(baseDir)
        if (!os.isDir(gameScriptsDest)) {
          throw new Exception(
            s"The supplied path to the game scripts destination is not a directory: ${gameScriptsDest.toString}"
          )
        } else if (!os.exists(gameScriptsDest)) {
          throw new Exception(
            s"The supplied path to the assets game scripts destination does not exist: ${gameScriptsDest.toString}"
          )
        } else {
          // copy built js file into scripts dir
          IndigoBuild.copyScript(scriptPathBase, gameScriptsDest, scriptName)

          // copy built js source map file into scripts dir
          IndigoBuild.copyScript(
            scriptPathBase,
            gameScriptsDest,
            scriptName + ".map"
          )
        }

        // copy assets into folder
        val assetsDest = outputs.assets.resolveFrom(baseDir)
        if (!os.isDir(assetsDest)) {
          throw new Exception(s"The supplied path to the assets destination is not a directory: ${assetsDest.toString}")
        } else if (!os.exists(assetsDest)) {
          throw new Exception(
            s"The supplied path to the assets destination does not exist: ${assetsDest.toString}"
          )
        } else {
          IndigoBuild.copyAssets(options.assets, assetsDest)
        }

        println(s"Built to: ${baseDir.toString}")

      case Default =>
        val directoryStructure = createDirectoryStructure(baseDir)

        val scriptName = findScriptName(scriptNames, scriptPathBase)

        // copy built js file into scripts dir
        IndigoBuild.copyScript(scriptPathBase, directoryStructure.artefacts, scriptName)

        // copy built js source map file into scripts dir
        IndigoBuild.copyScript(
          scriptPathBase,
          directoryStructure.artefacts,
          scriptName + ".map"
        )

        // copy assets into folder
        IndigoBuild.copyAssets(options.assets, directoryStructure.assets)

        // Write an empty cordova.js file so the script reference is intact,
        // even though it does nothing here.
        os.write(directoryStructure.base / "cordova.js", "")

        // Write support js script
        val support = SupportScriptTemplate.template()
        os.write(directoryStructure.base / "scripts" / "indigo-support.js", support)

        // Fill out html template
        val html = HtmlTemplate.template(
          options.metadata.title,
          options.metadata.showCursor,
          scriptName,
          options.metadata.backgroundColor
        )

        // Write out file
        val outputPath = IndigoBuild.writeHtml(directoryStructure, html)

        println(outputPath.toString())
    }

  def findScriptName(names: List[String], scriptDirPath: Path): String =
    names
      .find(name => os.exists(scriptDirPath / name))
      .getOrElse(
        throw new Exception(
          "Could not find a script file with any of the following names " +
            names.mkString("[", ", ", "]") +
            s" in '${scriptDirPath.toString}'"
        )
      )

  def createDirectoryStructure(baseDir: Path): DirectoryStructure = {
    println("dirPath: " + baseDir.toString())

    DirectoryStructure(
      Utils.ensureDirectoryAt(baseDir),
      Utils.ensureDirectoryAt(baseDir / "assets"),
      Utils.ensureDirectoryAt(baseDir / "scripts")
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def copyAssets(baseDirectory: os.Path, indigoAssets: IndigoAssets, destAssetsFolder: Path): Unit = {
    val from = baseDirectory / indigoAssets.gameAssetsDirectory
    val to   = destAssetsFolder

    if (!os.exists(from))
      throw new Exception("Supplied game assets path does not exist: " + indigoAssets.gameAssetsDirectory.toString())
    else if (!os.isDir(from))
      throw new Exception("Supplied game assets path was not a directory")
    else {
      println("Copying assets...")
    }

    def copyOne(p: Path): java.nio.file.Path = {
      val target = to / p.relativeTo(from)

      os.makeDir.all(target)

      java.nio.file.Files.copy(
        p.wrapped,
        target.wrapped,
        LinkOption.NOFOLLOW_LINKS,
        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.COPY_ATTRIBUTES
      )
    }

    // Sanity check src and destination aren't the same
    require(
      !to.startsWith(from),
      s"Can't copy a directory into itself: $to is inside $from"
    )

    require(
      stat(from, followLinks = true).isDir,
      s"Asset source location is must be a directory and isn't: $from"
    )

    // Ensure destination directories are in place
    makeDir.all(to)

    indigoAssets
      .filesToCopy(baseDirectory)
      .foreach { path =>
        copyOne(path)
      }
  }
  def copyAssets(indigoAssets: IndigoAssets, destAssetsFolder: Path): Unit =
    copyAssets(workspaceDir, indigoAssets, destAssetsFolder)

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def copyScript(scriptPathBase: Path, destScriptsFolder: Path, fileName: String): Unit = {
    val scriptFile = scriptPathBase / fileName

    if (os.exists(scriptFile))
      os.copy(scriptFile, destScriptsFolder / fileName, true, false, false, false, false)
    else
      throw new Exception("Script file does not exist, have you compiled the JS file? Tried: " + scriptFile.toString())
  }

  def writeHtml(directoryStructure: DirectoryStructure, html: String): Path = {

    val outFile = directoryStructure.base / "index.html"

    os.write(outFile, html)

    outFile
  }

}
