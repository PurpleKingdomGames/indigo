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
      val res = (p.defs ++ List(p.main)).map(_.render).mkString("\n")
      s"""//<indigo-fragment>
      |$res
      |//</indigo-fragment>""".stripMargin
