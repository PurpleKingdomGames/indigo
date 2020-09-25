package indigoplugin

import indigoplugin.templates.ElectronTemplates

import os._
import indigoplugin.templates.SupportScriptTemplate

object IndigoRun {

  def run(outputDir: Path, buildDir: Path, title: String, windowWidth: Int, windowHeight: Int): Unit = {

    os.remove.all(outputDir)
    os.makeDir.all(outputDir)

    filesToWrite(windowWidth, windowHeight).foreach { f =>
      os.write.over(outputDir / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      os.copy.into(file, outputDir, true, true, true, true)
    }

    // Write support js script
    val supportFile = outputDir / "scripts" / "indigo-support.js"
    val support     = SupportScriptTemplate.template(true)
    os.remove(supportFile)
    os.write(supportFile, support)

    println(s"Starting '$title'")

    os.proc("npm", "start")
      .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)

    ()
  }

  def filesToWrite(windowWidth: Int, windowHeight: Int): List[FileToWrite] =
    List(
      FileToWrite("main.js", ElectronTemplates.mainFileTemplate(windowWidth, windowHeight)),
      FileToWrite("preload.js", ElectronTemplates.preloadFileTemplate),
      FileToWrite("package.json", ElectronTemplates.packageFileTemplate)
    )

}
