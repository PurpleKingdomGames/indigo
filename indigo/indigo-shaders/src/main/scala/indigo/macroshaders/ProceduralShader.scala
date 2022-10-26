package indigo.macroshaders

import scala.annotation.tailrec
import scala.quoted.*

final case class ProceduralShader(defs: List[ShaderAST], main: ShaderAST)

object ProceduralShader:
  given ToExpr[ProceduralShader] with {
    def apply(x: ProceduralShader)(using Quotes): Expr[ProceduralShader] =
      '{ ProceduralShader(${ Expr(x.defs) }, ${ Expr(x.main) }) }
  }

  extension (p: ProceduralShader)
    def render: String =
      import ShaderAST.*
      def envName(ast: ShaderAST): Option[String] =
        for {
          b <- ast.find {
            case ShaderBlock(_) => true
            case _              => false
          }
          f <- b.find {
            case CallFunction(_, _, _) => true
            case _                     => false
          }
          n <- f match {
            case CallFunction(_, _, name :: _) => Option(name)
            case _                             => None
          }
        } yield n.toString()

      val res = (p.defs ++ List(p.main)).map(_.render).mkString("\n")

      val out =
        envName(p.main) match
          case None       => res
          case Some(name) => res.replace(name + ".", "").replace(name, "")

      s"""//<indigo-fragment>
      |$out
      |//</indigo-fragment>""".stripMargin

    def exists(q: ShaderAST => Boolean): Boolean =
      p.main.exists(q) || p.defs.exists(_.exists(q))
