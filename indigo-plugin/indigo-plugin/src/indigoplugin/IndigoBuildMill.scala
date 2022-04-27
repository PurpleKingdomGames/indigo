package indigoplugin

import os._

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
