package indigoplugin

/** Represents you game's assets processing.
  *
  * @param gameAssetsDirectory
  *   Project relative path to a directory that contains all of the assets the game needs to load. Default './assets'.
  */
final case class IndigoAssets(
    gameAssetsDirectory: os.RelPath,
    include: os.RelPath => Boolean,
    exclude: os.RelPath => Boolean
) {

  /** Sets the asset directory path */
  def withAssetDirectory(path: String): IndigoAssets =
    this.copy(
      gameAssetsDirectory =
        if (path.startsWith("/")) os.Path(path).relativeTo(os.pwd)
        else os.RelPath(path)
    )
  def withAssetDirectory(path: os.RelPath): IndigoAssets =
    this.copy(gameAssetsDirectory = path)

  def withInclude(p: os.RelPath => Boolean): IndigoAssets =
    this.copy(include = p)

  def withExclude(p: os.RelPath => Boolean): IndigoAssets =
    this.copy(exclude = p)

  def isCopyAllowed(rel: os.RelPath): Boolean =
    // val rel = toCopy.relativeTo(base)
    if (include(rel))
      // Specifically include, even if in an excluded location
      true
    else if (exclude(rel))
      // Specifically excluded, do nothing
      false
    else
      // Otherwise, no specific instruction so assume copy.
      true

  def filesToCopy(baseDirectory: os.Path): List[os.Path] =
    os.walk(baseDirectory / gameAssetsDirectory)
      .toList
      .filter(path => isCopyAllowed(path.relativeTo(baseDirectory / gameAssetsDirectory)))
  def filesToCopy: List[os.Path] =
    filesToCopy(os.pwd)

  def listAssetFiles(baseDirectory: os.Path): List[os.RelPath] =
    filesToCopy(baseDirectory)
      .filterNot(os.isDir)
      .map(_.relativeTo(baseDirectory / gameAssetsDirectory))
  def listAssetFiles: List[os.RelPath] =
    listAssetFiles(os.pwd)

}

object IndigoAssets {

  /** Default settings for an Indigo game's asset management */
  val defaults: IndigoAssets = {
    val pf: PartialFunction[os.RelPath, Boolean] = { case _ => false }

    IndigoAssets(gameAssetsDirectory = os.RelPath("assets"), pf, pf)
  }
}
