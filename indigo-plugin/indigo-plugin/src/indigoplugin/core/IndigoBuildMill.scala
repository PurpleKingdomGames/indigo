package indigoplugin.core

import os._
import indigoplugin.datatypes.TemplateOptions
import indigoplugin.utils.AsciiLogo

object IndigoBuildMill {

  def build(baseDir: Path, templateOptions: TemplateOptions): Unit = {

    println(AsciiLogo.logo)

    IndigoBuild.build(
      templateOptions,
      IndigoBuild.createDirectoryStructure(baseDir),
      List("main.js", "out.js")
    )
  }

}
