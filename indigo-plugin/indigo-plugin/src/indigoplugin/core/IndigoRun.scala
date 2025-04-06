package indigoplugin.core

import indigoplugin.templates.ElectronTemplates

import os._
import indigoplugin.templates.SupportScriptTemplate
import indigoplugin.datatypes.FileToWrite
import indigoplugin.ElectronInstall
import indigoplugin.IndigoOptions

object IndigoRun {

  private val installingNPMDepsMessage: String =
    " > Installing NPM dependencies"

  private val usingInstalledNPMDepsMessage: String =
    " > Using already installed NPM dependencies"

  def run(
      outputDir: Path,
      buildDir: Path,
      indigoOptions: IndigoOptions
  ): Unit = {

    os.makeDir.all(outputDir)

    filesToWrite(indigoOptions).foreach { f =>
      os.write.over(outputDir / f.name, f.contents)
    }

    os.list(buildDir).foreach { file =>
      os.copy(file, outputDir / file.last, true, true, true, true, true)
    }

    // Write support js script
    val supportFile = outputDir / "scripts" / "indigo-support.js"
    val support     = SupportScriptTemplate.template()
    os.remove(supportFile)
    os.write(supportFile, support)

    println(s"Starting '${indigoOptions.metadata.title}'")

    sys.props("os.name").toLowerCase match {
      case x if x contains "windows" =>
        indigoOptions.electron.electronInstall match {
          case ElectronInstall.Global =>
            IndigoProc.Windows.npmStart(outputDir)

          case ElectronInstall.Version(_) =>
            if (!os.exists(outputDir / "node_modules" / "electron")) {
              println(installingNPMDepsMessage)
              IndigoProc.Windows.npmInstall(outputDir)
            } else {
              println(usingInstalledNPMDepsMessage)
            }
            IndigoProc.Windows.npmStart(outputDir)

          case ElectronInstall.Latest =>
            if (!os.exists(outputDir / "node_modules" / "electron")) {
              println(installingNPMDepsMessage)
              IndigoProc.Windows.installLatestElectron(outputDir)
              IndigoProc.Windows.npmInstall(outputDir)
            } else {
              println(usingInstalledNPMDepsMessage)
            }
            IndigoProc.Windows.npmStart(outputDir)

          case ElectronInstall.PathToExecutable(_) =>
            IndigoProc.Windows.npmStart(outputDir)
        }

      case _ =>
        indigoOptions.electron.electronInstall match {
          case ElectronInstall.Global =>
            IndigoProc.Nix.npmStart(outputDir)

          case ElectronInstall.Version(_) =>
            if (!os.exists(outputDir / "node_modules" / "electron")) {
              println(installingNPMDepsMessage)
              IndigoProc.Nix.npmInstall(outputDir)
            } else {
              println(usingInstalledNPMDepsMessage)
            }
            IndigoProc.Nix.npmStart(outputDir)

          case ElectronInstall.Latest =>
            if (!os.exists(outputDir / "node_modules" / "electron")) {
              println(installingNPMDepsMessage)
              IndigoProc.Nix.installLatestElectron(outputDir)
              IndigoProc.Nix.npmInstall(outputDir)
            } else {
              println(usingInstalledNPMDepsMessage)
            }
            IndigoProc.Nix.npmStart(outputDir)

          case ElectronInstall.PathToExecutable(_) =>
            IndigoProc.Nix.npmStart(outputDir)
        }
    }

    ()
  }

  def filesToWrite(indigoOptions: IndigoOptions): List[FileToWrite] =
    List(
      FileToWrite(
        "main.js",
        ElectronTemplates.mainFileTemplate(
          windowWidth = indigoOptions.metadata.width,
          windowHeight = indigoOptions.metadata.height,
          openDevTools = indigoOptions.electron.openDevTools
        )
      ),
      FileToWrite("preload.js", ElectronTemplates.preloadFileTemplate),
      FileToWrite("package.json", ElectronTemplates.packageFileTemplate(indigoOptions.electron))
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
