import sbt._

object ShaderGen {

  val extensions: List[String] =
    List(".glsl", ".shader", ".vert", ".frag")

  val fileFilter: String => Boolean =
    name => extensions.exists(e => name.endsWith(e))

  val tripleQuotes: String = "\"\"\""

  def template(name: String, contents: String): String =
    s"""package indigo.platform.shaders
    |object $name {
    |  val shader: String =
    |    ${tripleQuotes}${contents}${tripleQuotes}
    |}
    """.stripMargin

  def stripSuffix(remaining: List[String], name: String): String =
    remaining match {
      case Nil =>
        name

      case s :: ss if name.endsWith(s) =>
        stripSuffix(ss, name.split('.').map(_.capitalize).mkString)

      case _ :: ss =>
        stripSuffix(ss, name)

    }

  def makeShader(files: Set[File], sourceManagedDir: File): Seq[File] = {
    println("Generating Indigo Shader Classes...")

    val shaderFiles: Seq[File] =
      files.filter(f => fileFilter(f.name)).toSeq

    val details: Seq[(String, String)] =
      shaderFiles.map(f => (f.name, IO.read(f)))

    details.map {
      case (name, contents) =>
        val newName = stripSuffix(extensions, name)

        println("> " + name + " --> " + newName + ".scala")

        val file: File =
          sourceManagedDir / "indigo" / "platform" / "shaders" / (newName + ".scala")

        val newContents: String =
          template(newName, contents)

        IO.write(file, newContents)

        file
    }
  }

}
