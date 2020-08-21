package millindigo

import indigoplugin.templates._

object ElectronRequirements {

  def filesToWrite(windowWidth: Int, windowHeight: Int): List[FileToWrite] =
    List(
      FileToWrite("main.js", MainTemplate.template(windowWidth, windowHeight)),
      FileToWrite("preload.js", PreloadTemplate.template),
      FileToWrite("package.json", PackageTemplate.template)
    )

}

final case class FileToWrite(name: String, contents: String)
