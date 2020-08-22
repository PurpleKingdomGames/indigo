package indigoplugin

import os._

object IndigoBuildMill {

  def build(baseDir: Path, templateOptions: TemplateOptions): Unit =
    IndigoBuild.build(
      templateOptions,
      IndigoBuild.createDirectoryStructure(baseDir),
      "out.js"
    )

}
