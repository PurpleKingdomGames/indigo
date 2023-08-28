package indigoplugin.datatypes

import os._

final case class TemplateOptions(
    title: String,
    showCursor: Boolean,
    scriptPathBase: Path,
    gameAssetsDirectoryPath: Path,
    backgroundColor: String = "white"
)
