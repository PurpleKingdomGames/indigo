package indigoplugin

import indigoplugin.utils.Utils

/** Represents you game's assets processing. All assets and details are based around a single asset directory and it's
  * sub-tree.
  *
  * @param gameAssetsDirectory
  *   Project relative path to a directory that contains all of the assets the game needs to load. Default './assets'.
  */
final case class IndigoAssets(
    gameAssetsDirectory: os.RelPath,
    include: os.RelPath => Boolean,
    exclude: os.RelPath => Boolean,
    rename: PartialFunction[(String, String), String]
) {

  val workspaceDir = Utils.findWorkspace

  /** Sets the asset directory path */
  def withAssetDirectory(path: String): IndigoAssets =
    this.copy(
      gameAssetsDirectory =
        if (path.startsWith("/")) os.Path(path).relativeTo(workspaceDir)
        else os.RelPath(path)
    )

  /** Sets the asset directory path */
  def withAssetDirectory(path: os.RelPath): IndigoAssets =
    this.copy(gameAssetsDirectory = path)

  /** Function that decides if a path in the assets folder should specifically be included. Useful for including a file
    * inside a folder that has been excluded.
    */
  def withInclude(p: os.RelPath => Boolean): IndigoAssets =
    this.copy(include = p)

  /** Function that decides if a path in the assets folder should specifically be excluded. Useful for excluding source
    * files or folders used during asset development.
    */
  def withExclude(p: os.RelPath => Boolean): IndigoAssets =
    this.copy(exclude = p)

  /** Provide a custom renaming function (arguments are (name, ext) => ???) used during asset listing generation to
    * produce safe names for generated code. Original file names will not be affected. For example: A file called
    * `some_text-file!.txt` by default will copied as is with it's path preserved, but in the generated Scala code it's
    * name will be `someTextFile`, i.e. `val someTextFile: AssetName = ???`
    *
    * @param f
    *   Function that takes a tuple of Strings, file name and extension, and returns a new 'safe for Scala' name.
    */
  def withRenameFunction(f: PartialFunction[(String, String), String]): IndigoAssets =
    this.copy(rename = f)

  /** Decides if a relative path will be included in the assets or not. */
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

  /** List which absolute paths will be copied from the source asset directory. */
  def filesToCopy(baseDirectory: os.Path): List[os.Path] =
    os.walk(baseDirectory / gameAssetsDirectory)
      .toList
      .filter(path => isCopyAllowed(path.relativeTo(baseDirectory / gameAssetsDirectory)))
  def filesToCopy: List[os.Path] =
    filesToCopy(workspaceDir)

  /** List all relative paths that will be available to the game. */
  def listAssetFiles(baseDirectory: os.Path): List[os.RelPath] =
    filesToCopy(baseDirectory)
      .filterNot(os.isDir)
      .map(_.relativeTo(baseDirectory / gameAssetsDirectory / os.RelPath.up))
  def listAssetFiles: List[os.RelPath] =
    listAssetFiles(workspaceDir)

}

object IndigoAssets {

  val noRename: PartialFunction[(String, String), String] = { case (name, _) =>
    name
  }

  /** Default settings for an Indigo game's asset management */
  val defaults: IndigoAssets = {
    val pf: PartialFunction[os.RelPath, Boolean] = { case _ => false }

    IndigoAssets(
      gameAssetsDirectory = os.RelPath("assets"),
      pf,
      pf,
      noRename
    )
  }
}
