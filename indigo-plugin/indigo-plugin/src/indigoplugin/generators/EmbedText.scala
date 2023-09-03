package indigoplugin.generators

object EmbedText {

  val tripleQuotes: String = "\"\"\""

  def generate(
      outDir: os.Path,
      moduleName: String,
      fullyQualifiedPackage: String,
      text: String
  ): Seq[os.Path] = {
    val wd = outDir / "indigo-compile-codegen-output"

    os.makeDir.all(wd)

    val file = wd / s"$moduleName.scala"

    val contents =
      s"""package $fullyQualifiedPackage
      |
      |object $moduleName:
      |
      |  val text: String =
      |    $tripleQuotes$text$tripleQuotes
      |""".stripMargin

    os.write.over(file, contents)

    Seq(file)
  }

}
