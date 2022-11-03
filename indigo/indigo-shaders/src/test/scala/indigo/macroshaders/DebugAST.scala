package indigo.macroshaders

import scala.quoted.*

object DebugAST:

  inline def toAST[In, Out](inline expr: Shader[In, Out]): String = ${ toASTImpl('{ expr }) }

  private def toASTImpl[In, Out: Type](expr: Expr[Shader[In, Out]])(using Quotes): Expr[String] = {

    import quotes.reflect.*

    println(">>> Everything")
    println(Printer.TreeStructure.show(expr.asTerm))
    println("<<<")

    Expr("Done.")
  }
