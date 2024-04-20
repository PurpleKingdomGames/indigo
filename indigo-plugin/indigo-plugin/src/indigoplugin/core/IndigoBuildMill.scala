package indigoplugin.core

import os._
import indigoplugin.utils.AsciiLogo
import indigoplugin.IndigoOptions

object IndigoBuildMill {

  def build(scriptPathBase: Path, baseDir: Path, options: IndigoOptions): Unit = {

    println(AsciiLogo.logo)

    IndigoBuild.build(
      scriptPathBase,
      options,
      baseDir,
      List("main.js", "out.js")
    )
  }

}
