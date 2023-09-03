package indigoplugin

object IndigoGenerators {

  val tripleQuotes: String = "\"\"\""

  def embedText(
      outDir: os.Path,
      moduleName: String,
      fullyQualifiedPackage: String,
      text: String
  ): Seq[os.Path] = {
    val wd = outDir / "indigo-codegen-output"

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
