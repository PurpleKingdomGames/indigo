package indigoplugin.core

import os._
import indigoplugin.datatypes.TemplateOptions
import indigoplugin.utils.AsciiLogo

object IndigoBuildSBT {

  def build(
      baseDir: String,
      templateOptions: TemplateOptions,
      outputFolderName: String,
      scriptNames: List[String]
  ): Unit = {

    println(AsciiLogo.logo)

    IndigoBuild.build(
      templateOptions,
      IndigoBuild.createDirectoryStructure(Path(baseDir) / "target" / outputFolderName),
      scriptNames
    )
  }

}
