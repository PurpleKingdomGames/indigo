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

object IndigoBuild {

  def build(
      scriptPathBase: Path,
      options: IndigoOptions,
      directoryStructure: DirectoryStructure,
      scriptNames: List[String]
  ): Unit = {

    val scriptName = findScriptName(scriptNames, scriptPathBase)

    // copy built js file into scripts dir
    IndigoBuild.copyScript(scriptPathBase, directoryStructure.artefacts, scriptName)

    // copy assets into folder
    IndigoBuild.copyAssets(options.assets, directoryStructure.assets)

    // copy built js source map file into scripts dir
    IndigoBuild.copyScript(
      scriptPathBase,
      directoryStructure.artefacts,
      scriptName + ".map"
    )

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
  def copyAssets(indigoAssets: IndigoAssets, destAssetsFolder: Path): Unit = {
    val absPath = indigoAssets.gameAssetsDirectory.resolveFrom(os.pwd)

    if (!os.exists(absPath))
      throw new Exception("Supplied game assets path does not exist: " + indigoAssets.gameAssetsDirectory.toString())
    else if (!os.isDir(absPath))
      throw new Exception("Supplied game assets path was not a directory")
    else {
      println("Copying assets...")

      copyAllWithFilters(
        absPath,
        destAssetsFolder,
        indigoAssets.include,
        indigoAssets.exclude
      )
    }
  }

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

  def isCopyAllowed(
      base: Path,
      toCopy: Path,
      include: RelPath => Boolean,
      exclude: RelPath => Boolean
  ): Boolean = {
    val rel = toCopy.relativeTo(base)

    if (include(rel))
      // Specifically include, even if in an excluded location
      true
    else if (exclude(rel))
      // Specifically excluded, do nothing
      false
    else
      // Otherwise, no specific instruction so assume copy.
      true
  }

  def copyAllWithFilters(
      from: Path,
      to: Path,
      include: RelPath => Boolean,
      exclude: RelPath => Boolean
  ): Unit = {

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

    walk(from).foreach { path =>
      if (isCopyAllowed(from, path, include, exclude)) {
        copyOne(path)
      }
    }
  }

}
