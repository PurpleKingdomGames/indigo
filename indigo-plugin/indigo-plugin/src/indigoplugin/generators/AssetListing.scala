package indigoplugin.generators

import indigoplugin.IndigoAssets

object AssetListing {

  def generate(
      outDir: os.Path,
      moduleName: String,
      fullyQualifiedPackage: String,
      indigoAssets: IndigoAssets
  ): Seq[os.Path] = {

    val fileContents: String =
      renderContent(indigoAssets.listAssetFiles)

    val wd = outDir / Generators.OutputDirName

    os.makeDir.all(wd)

    val file = wd / s"$moduleName.scala"

    val contents =
      s"""package $fullyQualifiedPackage
      |
      |import indigo.*
      |
      |object $moduleName:
      |
      |${fileContents}
      |
      |""".stripMargin

    os.write.over(file, contents)

    Seq(file)
  }

  def renderContent(paths: List[os.RelPath]): String =
    (convertPathsToTree _ andThen renderTree(0))(paths)

  def convertPathsToTree(paths: List[os.RelPath]): PathTree =
    PathTree
      .combineAll(
        paths.map { p =>
          PathTree.pathToPathTree(p) match {
            case None        => throw new Exception(s"Could not parse given path: $p")
            case Some(value) => value
          }
        }
      )
      .sorted

  def renderTree(indent: Int)(pathTree: PathTree): String = {
    /*

    What are we doing?

    For each root / folder, we make a object. (Root is top level)

    For all the files in the children, we make essentially this:

    val snakeTexture: AssetName  = AssetName("snakeTexture")
    val snakeMaterial: Material.Bitmap = Material.Bitmap(snakeTexture)

    |def assets(baseUrl: String): Set[AssetType] =
    |  Set(
    |    ...
    |    AssetType.Image(snakeTexture, AssetPath(baseUrl + "assets/snake.png")),
    |    ...
    |  )

    With tags. Or audio descriptions for audio files. Nothing special for text / unknown.

    and for each sub-folder, we call render again.

    Until we end up with one great big string or list of strings or something.

     */

    val indentSpaces     = List.fill(indent)("  ").mkString
    val indentSpacesNext = indentSpaces + "  "

    val res = 
      pathTree match {
        case PathTree.File(_, _, _) =>
          ""

        case PathTree.Folder(name, children) =>
          val files: List[PathTree.File] = children.collect { case f: PathTree.File => f }

          val renderedFiles: String =
            files
              .map { case PathTree.File(name, ext, path) =>
                val safeName = toSafeName(name)
                s"${indentSpacesNext}file: " + toSafeName(name) + " at: " + path.toString() + "\n"
                s"""
                |${indentSpacesNext}val ${safeName}Name: AssetName  = AssetName("${name}.${ext}")
                |${indentSpacesNext}val ${safeName}Material: Material.Bitmap = Material.Bitmap(${safeName}Name)
                |""".stripMargin
              }
              .mkString("\n")

          val assetSeq: String =
            if (files.isEmpty) ""
            else
              s"""${renderedFiles}
              |
              |${indentSpacesNext}def assets(baseUrl: String): Set[AssetType] =
              |${indentSpacesNext}  Set(
              |${indentSpacesNext}    AssetType.Image(snakeTexture, AssetPath(baseUrl + "assets/snake.png")),
              |${indentSpacesNext}  )
              |
              |""".stripMargin

          s"""${indentSpaces}object ${toSafeName(name)}:
          |${children.map(renderTree(indent + 1)).mkString}
          |""".stripMargin + assetSeq

        case PathTree.Root(children) =>
          children.map(renderTree(indent + 1)).mkString
      }
    
    res
  }

  def toSafeName(name: String): String = {
    val res = name.replaceAll("[^a-zA-Z0-9]", "-").split("-").map(_.capitalize).mkString
    if (res.take(1).matches("[0-9]")) "_" + res else res
  }

}
