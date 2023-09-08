package indigoplugin.generators

object EmbedText {

  def generate(
      outDir: os.Path,
      moduleName: String,
      fullyQualifiedPackage: String,
      filePath: os.Path
  ): Seq[os.Path] = {

    val text =
      if (!os.exists(filePath)) throw new Exception("Text file to embed not found: " + filePath.toString())
      else {
        os.read(filePath)
      }

    val wd = outDir / Generators.OutputDirName

    os.makeDir.all(wd)

    val file = wd / s"$moduleName.scala"

    val contents =
      s"""package $fullyQualifiedPackage
      |
      |object $moduleName:
      |
      |  val text: String =
      |    ${Generators.TripleQuotes}$text${Generators.TripleQuotes}
      |""".stripMargin

    os.write.over(file, contents)

    Seq(file)
  }

}
