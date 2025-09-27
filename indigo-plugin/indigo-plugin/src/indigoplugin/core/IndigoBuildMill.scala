package indigoplugin.core

import os._
import indigoplugin.utils.AsciiLogo
import indigoplugin.IndigoOptions

object IndigoBuildMill {

  def build(scriptPathBase: Path, options: IndigoOptions, assetsDirectory: os.Path, baseDirectory: Path): Unit = {

    println(AsciiLogo.logo)

    IndigoBuild.build(
      scriptPathBase,
      options,
      assetsDirectory,
      baseDirectory,
      List("main.js", "out.js")
    )
  }

}
