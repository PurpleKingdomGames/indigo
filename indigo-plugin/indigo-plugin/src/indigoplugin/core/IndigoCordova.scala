package indigoplugin.core

import indigoplugin.templates.CordovaTemplates

import os._
import indigoplugin.templates.SupportScriptTemplate
import indigoplugin.datatypes.FileToWrite
import indigoplugin.IndigoGameMetadata

object IndigoCordova {

  def run(outputDir: Path, buildDir: Path, metadata: IndigoGameMetadata): Unit = {

    os.remove.all(outputDir)
    os.makeDir.all(outputDir)

    filesToWrite(metadata.title, metadata.width, metadata.height).foreach { f =>
      os.makeDir.all(outputDir / f.folderPath)
      os.write.over(outputDir / f.folderPath / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      os.copy(file, outputDir / "www" / file.last, true, true, true, true, false)
    }

    // Write support js script
    val supportFile = outputDir / "www" / "scripts" / "indigo-support.js"
    val support     = SupportScriptTemplate.template()
    os.remove(supportFile)
    os.write(supportFile, support)

    // This will be replaced by cordova itself.
    os.remove(outputDir / "www" / "cordova.js")

    ()
  }

  def filesToWrite(title: String, windowWidth: Int, windowHeight: Int): List[FileToWrite] =
    List(
      FileToWrite("config.xml", CordovaTemplates.configFileTemplate(title)),
      FileToWrite("package.json", CordovaTemplates.packageFileTemplate),
      FileToWrite(
        "settings.json",
        CordovaTemplates.electronSettingsFileTemplate(windowWidth, windowHeight),
        RelPath("res") / "electron"
      )
    )

}
