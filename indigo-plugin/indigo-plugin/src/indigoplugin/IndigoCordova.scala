package indigoplugin

import indigoplugin.templates.CordovaTemplates

import os._
import indigoplugin.templates.SupportScriptTemplate

object IndigoCordova {

  def run(outputDir: Path, buildDir: Path, title: String, showMouse: Boolean, windowWidth: Int, windowHeight: Int): Unit = {

    os.remove.all(outputDir)
    os.makeDir.all(outputDir)

    filesToWrite(title, showMouse, windowWidth, windowHeight).foreach { f =>
      os.makeDir.all(outputDir / f.folderPath)
      os.write.over(outputDir / f.folderPath / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      os.copy.into(file, outputDir / "www", true, true, true, true)
    }

    // Write support js script
    val supportFile = outputDir / "www" / "scripts" / "indigo-support.js"
    val support     = SupportScriptTemplate.template(true)
    os.remove(supportFile)
    os.write(supportFile, support)
    
    ()
  }

  def filesToWrite(title: String, showMouse: Boolean, windowWidth: Int, windowHeight: Int): List[FileToWrite] =
    List(
      FileToWrite("config.xml", CordovaTemplates.configFileTemplate(title, showMouse, windowWidth, windowHeight)),
      FileToWrite("package.json", CordovaTemplates.packageFileTemplate),
      FileToWrite(
        "settings.json",
        CordovaTemplates.electronSettingsFileTemplate(windowWidth, windowHeight),
        RelPath("res") / "electron"
      )
    )

}
