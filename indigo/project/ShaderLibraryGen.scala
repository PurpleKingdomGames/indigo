import sbt._
import scala.sys.process._

object ShaderLibraryGen {

  val extensions: List[String] =
    List(".vert", ".frag")

  val fileFilter: String => Boolean =
    name => extensions.exists(e => name.endsWith(e))

  def extractDetails(remaining: Seq[String], name: String, file: File): Option[ShaderDetails] =
    remaining match {
      case Nil =>
        None

      case ext :: exts if name.endsWith(ext) =>
        Some(ShaderDetails(name.substring(0, name.indexOf(ext)).capitalize, name, ext, IO.read(file)))

      case _ :: exts =>
        extractDetails(exts, name, file)
    }

  val tripleQuotes: String = "\"\"\""

  def template(contents: String): String =
    s"""package indigo.shaders
    |
    |object ShaderLibrary {
    |
    |$contents
    |
    |}
    """.stripMargin

  def extractShaderCode(text: String, tag: String, assetName: String, newName: String): Seq[ShaderSnippet] =
    s"""//<indigo-$tag>\n((.|\n|\r)*)//</indigo-$tag>""".r
      .findAllIn(text)
      .toSeq
      .map(_.toString)
      .map(_.split('\n').drop(1).dropRight(1).mkString("\n"))
      .map(program => ShaderSnippet(newName + tag.split("-").map(_.capitalize).mkString, program))

  def makeShaderLibrary(files: Set[File], sourceManagedDir: File): Seq[File] = {
    println("Generating Indigo Shader Library...")

    val shaderFiles: Seq[File] =
      files.filter(f => fileFilter(f.name)).toSeq

    val glslValidatorExitCode = "glslangValidator -v" !

    println("***************")
    println("GLSL Validation")
    println("***************")

    if (glslValidatorExitCode == 0)
      shaderFiles.foreach { f =>
        val exit = ("glslangValidator " + f.getCanonicalPath) !

        if (exit != 0)
          throw new Exception("GLSL Validation Error in: " + f.getName)
        else
          println(f.getName + " [valid]")
      }
    else
      println("**WARNING**: GLSL Validator not installed, shader code not checked.")

    val shaderDetails: Seq[ShaderDetails] =
      shaderFiles
        .map(f => extractDetails(extensions, f.name, f))
        .collect { case Some(s) => s }

    val contents: String =
      shaderDetails
        .flatMap { d =>
          extractShaderCode(d.shaderCode, "vertex", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "fragment", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "light", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "post-vertex", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "post-fragment", d.originalName + d.ext, d.newName) ++
            extractShaderCode(d.shaderCode, "post-light", d.originalName + d.ext, d.newName)
        }
        .map { snippet =>
          s"""  val ${snippet.variableName}: String =
             |    ${tripleQuotes}${snippet.snippet}${tripleQuotes}
             |
          """.stripMargin
        }
        .mkString("\n")

    val file: File =
      sourceManagedDir / "indigo" / "shaders" / "ShaderLibrary.scala"

    val newContents: String =
      template(contents)

    IO.write(file, newContents)

    println("Written: " + file.getCanonicalPath)

    Seq(file)
  }

  case class ShaderDetails(newName: String, originalName: String, ext: String, shaderCode: String)
  case class ShaderSnippet(variableName: String, snippet: String)
}
