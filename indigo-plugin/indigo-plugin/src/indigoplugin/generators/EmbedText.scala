package indigoplugin.generators

object EmbedText {

  def generate(
      outDir: os.Path,
      moduleName: String,
      fullyQualifiedPackage: String,
      text: String
  ): Seq[os.Path] = {
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
