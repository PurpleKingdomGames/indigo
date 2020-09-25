package indigoplugin

import os._

final case class TemplateOptions(
    title: String,
    showCursor: Boolean,
    scriptPathBase: Path,
    gameAssetsDirectoryPath: Path
)

object Utils {

  def ensureDirectoryAt(path: Path): Path = {
    os.remove.all(path)
    os.makeDir.all(path)

    path
  }

}

final case class DirectoryStructure(base: Path, assets: Path, artefacts: Path)

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
final case class FileToWrite(name: String, contents: String, folderPath: RelPath = RelPath.rel)
