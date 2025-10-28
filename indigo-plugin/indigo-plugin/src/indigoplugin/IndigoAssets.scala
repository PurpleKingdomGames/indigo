package indigoplugin

import indigoplugin.utils.Utils

/** Represents you game's assets processing. All assets and details are based around a single asset directory and it's
  * sub-tree.
  *
  * @param gameAssetsDirectory
  *   Project relative path to a directory that contains all of the assets the game needs to load. Default './assets'.
  */
final case class IndigoAssets(
    include: os.RelPath => Boolean,
    exclude: os.RelPath => Boolean,
    rename: PartialFunction[(String, String), String]
) {

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

}

object IndigoAssets {

  val noRename: PartialFunction[(String, String), String] = { case (name, _) =>
    name
  }

  /** Default settings for an Indigo game's asset management */
  val defaults: IndigoAssets = {
    val pf: PartialFunction[os.RelPath, Boolean] = { case _ => false }

    IndigoAssets(
      pf,
      pf,
      noRename
    )
  }

  /** List which absolute paths will be copied from the source asset directory. */
  def filesToCopy(indigoAssets: IndigoAssets, assetsDirectory: os.Path): List[os.Path] =
    os.walk(assetsDirectory)
      .toList
      .filter(path => indigoAssets.isCopyAllowed(path.relativeTo(assetsDirectory)))

  /** List all relative paths that will be available to the game. */
  def listAssetFiles(
      indigoAssets: IndigoAssets,
      assetsDirectory: os.Path
  ): List[os.RelPath] =
    filesToCopy(indigoAssets, assetsDirectory)
      .filterNot(os.isDir)
      .map(_.relativeTo(assetsDirectory / os.RelPath.up))
}
