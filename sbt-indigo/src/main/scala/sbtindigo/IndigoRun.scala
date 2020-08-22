package sbtindigo

import os._
import indigoplugin._

object IndigoRun {

  def run(outputDir: Path, buildDir: Path, title: String, windowWidth: Int, windowHeight: Int): Unit = {

    os.makeDir.all(outputDir)

    ElectronRequirements.filesToWrite(windowWidth, windowHeight).foreach { f =>
      os.write.over(outputDir / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      os.copy.into(file, outputDir, true, true, true, true)
    }

    println(s"Starting '$title'")

    os.proc("npm", "start")
      .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)

    ()
  }

}
