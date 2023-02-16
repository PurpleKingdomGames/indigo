package indigoplugin

import indigoplugin.templates.CordovaTemplates

import os._
import indigoplugin.templates.SupportScriptTemplate

object IndigoCordova {

  def run(outputDir: Path, buildDir: Path, title: String, windowWidth: Int, windowHeight: Int): Unit = {

    os.remove.all(outputDir)
    os.makeDir.all(outputDir)

    filesToWrite(title, windowWidth, windowHeight).foreach { f =>
      os.makeDir.all(outputDir / f.folderPath)
      os.write.over(outputDir / f.folderPath / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      os.copy(file, outputDir / "www" / file.last, true, true, true, true, false)
    }

    // Write support js script
    val supportFile = outputDir / "www" / "scripts" / "indigo-support.js"
    val support     = SupportScriptTemplate.template(true)
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
