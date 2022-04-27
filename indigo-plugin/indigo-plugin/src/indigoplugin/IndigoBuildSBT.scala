package indigoplugin

import os._

object IndigoBuildSBT {

  def build(baseDir: String, templateOptions: TemplateOptions, outputFolderName: String, scriptName: String): Unit = {

    println(AsciiLogo.logo)
    
    IndigoBuild.build(
      templateOptions,
      IndigoBuild.createDirectoryStructure(Path(baseDir) / "target" / outputFolderName),
      List(scriptName)
    )
  }

}
