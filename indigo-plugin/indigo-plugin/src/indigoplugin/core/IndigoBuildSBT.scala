package indigoplugin.core

import os._
import indigoplugin.utils.AsciiLogo
import indigoplugin.IndigoOptions

object IndigoBuildSBT {

  def build(
      scriptPathBase: Path,
      baseDir: String,
      options: IndigoOptions,
      outputFolderName: String,
      scriptNames: List[String]
  ): Unit = {

    println(AsciiLogo.logo)

    IndigoBuild.build(
      scriptPathBase,
      options,
      IndigoBuild.createDirectoryStructure(Path(baseDir) / "target" / outputFolderName),
      scriptNames
    )
  }

}
