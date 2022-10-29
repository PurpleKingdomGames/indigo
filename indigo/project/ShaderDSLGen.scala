import sbt._
import scala.sys.process._

object ShaderDSLGen {

  val tripleQuotes: String = "\"\"\""

  def template(
      name: String,
      contents: String
  ): String =
    s"""package indigo.macroshaders
    |
    |import indigo.macroshaders.ShaderDSL.*
    |
    |trait ${name} extends ShaderDSLTypes:
    |$contents
    |""".stripMargin

  def makeShaderDSL(sourceManagedDir: File): Seq[File] = {
    println("Generating Indigo Shader DSL...")

    val name = "ShaderDSLTypeExtensions"

    val file: File =
      sourceManagedDir / "indigo" / "macroshaders" / (name + ".scala")

    if (!file.exists()) {
      val newContents: String =
        template(name, makeContents())

      IO.write(file, newContents)

      println("Written: " + file.getCanonicalPath)
    } else {
      println("Found, skipping: " + file.getCanonicalPath)
    }

    Seq(file)
  }

  def makeContents(): String = {

    val vec2 = List("x", "y")
    val vec3 = List("x", "y", "z")
    val vec4 = List("x", "y", "z", "w")

    def swizzles1(input: List[String]): List[List[String]] =
      input.map(c => List(c))

    def swizzles2(input: List[String]): List[List[String]] = {
      val res =
        for {
          a <- input
          r <- List.fill(input.length)(a).zip(input).map(p => List(p._1, p._2))
        } yield r

      res
    }

    def swizzles3(input: List[String]): List[List[String]] = {
      val res =
        for {
          a <- input
          b <- List.fill(input.length)(a).zip(input).map(p => List(p._1, p._2))
          r <- List.fill(input.length)(b).zip(input).flatMap(p => List(p._1 :+ p._2))
        } yield r

      res
    }

    def swizzles4(input: List[String]): List[List[String]] = {
      val res =
        for {
          a <- input
          b <- List.fill(input.length)(a).zip(input).map(p => List(p._1, p._2))
          c <- List.fill(input.length)(b).zip(input).flatMap(p => List(p._1 :+ p._2))
          r <- List.fill(input.length)(c).zip(input).flatMap(p => List(p._1 :+ p._2))
        } yield r

      res
    }

    List(
      extensionContent(
        "vec2",
        swizzles1(vec2) ++
          swizzles2(vec2) ++
          swizzles3(vec2) ++
          swizzles4(vec2),
        Map(
          "x" -> "x",
          "y" -> "y",
          "z" -> "z",
          "w" -> "w"
        )
      ),
      extensionContent(
        "vec3",
        swizzles1(vec3) ++
          swizzles2(vec3) ++
          swizzles3(vec3) ++
          swizzles4(vec3),
        Map(
          "x" -> "x",
          "y" -> "y",
          "z" -> "z",
          "w" -> "w"
        )
      ),
      extensionContent(
        "vec4",
        swizzles1(vec4) ++
          swizzles2(vec4) ++
          swizzles3(vec4) ++
          swizzles4(vec4),
        Map(
          "x" -> "x",
          "y" -> "y",
          "z" -> "z",
          "w" -> "w"
        )
      )
    ).mkString("\n")
  }

  def extensionContent(typeName: String, swizzles: List[List[String]], replace: Map[String, String]): String =
    s"""  extension (inline v: $typeName)
    |${swizzlesToMethods(swizzles, replace)}
    |""".stripMargin

  def swizzlesToMethods(swizzles: List[List[String]], replace: Map[String, String]): String =
    swizzles
      .map { s =>
        s match {
          case x :: Nil =>
            s"    inline def $x: Float = v.${replace(x)}"

          case x :: y :: Nil =>
            s"    inline def $x$y: vec2 = vec2(v.${replace(x)}, v.${replace(y)})"

          case x :: y :: z :: Nil =>
            s"    inline def $x$y$z: vec3 = vec3(v.${replace(x)}, v.${replace(y)}, v.${replace(z)})"

          case x :: y :: z :: w :: Nil =>
            s"    inline def $x$y$z$w: vec4 = vec4(v.${replace(x)}, v.${replace(y)}, v.${replace(z)}, v.${replace(w)})"

          case _ =>
            ""
        }
      }
      .filterNot(_.isEmpty)
      .mkString("\n")

}
