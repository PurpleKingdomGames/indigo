package indigoplugin

import indigoplugin.templates.ElectronTemplates

import scala.sys.process._
import os._
import indigoplugin.templates.SupportScriptTemplate

object IndigoRun {

  def run(
      outputDir: Path,
      buildDir: Path,
      title: String,
      windowWidth: Int,
      windowHeight: Int,
      disableFrameRateLimit: Boolean,
      electronInstall: ElectronInstall
  ): Unit = {

    os.remove.all(outputDir)
    os.makeDir.all(outputDir)

    filesToWrite(windowWidth, windowHeight, disableFrameRateLimit, electronInstall).foreach { f =>
      os.write.over(outputDir / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      CustomOSLibCopy.copyInto(file, outputDir, true, true, true, true, false)
    }

    // Write support js script
    val supportFile = outputDir / "scripts" / "indigo-support.js"
    val support     = SupportScriptTemplate.template(true)
    os.remove(supportFile)
    os.write(supportFile, support)

    println(s"Starting '$title'")

    sys.props("os.name").toLowerCase match {
      case x if x contains "windows" =>
        electronInstall match {
          case ElectronInstall.Global =>
            IndigoProc.Windows.npmStart(outputDir)

          case ElectronInstall.Version(_) =>
            IndigoProc.Windows.npmInstall(outputDir)
            IndigoProc.Windows.npmStart(outputDir)

          case ElectronInstall.Latest =>
            IndigoProc.Windows.installLatestElectron(outputDir)
            IndigoProc.Windows.npmInstall(outputDir)
            IndigoProc.Windows.npmStart(outputDir)

          case ElectronInstall.PathToExecutable(path) =>
            IndigoProc.Windows.npmStart(outputDir)
        }

      case _ =>
        electronInstall match {
          case ElectronInstall.Global =>
            IndigoProc.Nix.npmStart(outputDir)

          case ElectronInstall.Version(_) =>
            IndigoProc.Nix.npmInstall(outputDir)
            IndigoProc.Nix.npmStart(outputDir)

          case ElectronInstall.Latest =>
            IndigoProc.Nix.installLatestElectron(outputDir)
            IndigoProc.Nix.npmInstall(outputDir)
            IndigoProc.Nix.npmStart(outputDir)

          case ElectronInstall.PathToExecutable(path) =>
            IndigoProc.Nix.npmStart(outputDir)
        }
    }

    ()
  }

  def filesToWrite(
      windowWidth: Int,
      windowHeight: Int,
      disableFrameRateLimit: Boolean,
      electronInstall: ElectronInstall
  ): List[FileToWrite] =
    List(
      FileToWrite("main.js", ElectronTemplates.mainFileTemplate(windowWidth, windowHeight)),
      FileToWrite("preload.js", ElectronTemplates.preloadFileTemplate),
      FileToWrite("package.json", ElectronTemplates.packageFileTemplate(disableFrameRateLimit, electronInstall))
    )

}

object IndigoProc {

  object Windows {
    def npmStart(outputDir: Path) =
      os.proc("cmd", "/C", "npm", "start")
        .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)

    def installLatestElectron(outputDir: Path) =
      os.proc("cmd", "/C", "npm", "install", "electron", "--save-dev")
        .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)

    def npmInstall(outputDir: Path) =
      os.proc("cmd", "/C", "npm", "install")
        .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)
  }

  object Nix {
    def npmStart(outputDir: Path) =
      os.proc("npm", "start")
        .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)

    def installLatestElectron(outputDir: Path) =
      os.proc("npm", "install", "electron", "--save-dev")
        .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)

    def npmInstall(outputDir: Path) =
      os.proc("npm", "install")
        .call(cwd = outputDir, stdin = os.Inherit, stdout = os.Inherit, stderr = os.Inherit)
  }

}
