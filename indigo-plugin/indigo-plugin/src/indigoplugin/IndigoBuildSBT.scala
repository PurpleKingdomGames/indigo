package indigoplugin

import os._

object IndigoBuildSBT {

  def build(baseDir: String, templateOptions: TemplateOptions, outputFolderName: String, scriptName: String): Unit =
    IndigoBuild.build(
      templateOptions,
      IndigoBuild.createDirectoryStructure(Path(baseDir) / "target" / outputFolderName),
      scriptName
    )

}
