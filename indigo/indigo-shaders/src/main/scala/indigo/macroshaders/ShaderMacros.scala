package indigo.macroshaders

import scala.quoted.*

object ShaderMacros:

  inline def toGLSL(name: String, inline expr: Function1[Float, Float]): String = ${ toGLSLImpl('name, 'expr) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def toGLSLImpl(name: Expr[String], expr: Expr[Function1[Float, Float]])(using Quotes): Expr[String] = {

    import quotes.reflect.*

    def grabName: Term => String =
      case Inlined(None, Nil, Literal(StringConstant(theName))) =>
        theName

      case name =>
        val msg: String = Printer.TreeStructure.show(name)
        throw new Exception("Failed to extract the name provided: " + msg)

    def printTerm(indentAmount: Int): Term => String =
      case Apply(Select(Ident(argName), operator), List(Literal(FloatConstant(const)))) =>
        val indent = (0 to indentAmount).foldLeft("") {
          case (acc, 0) => acc
          case (acc, _) => acc + "  "
        }
        s"${indent}return $argName $operator $const;"

      case term =>
        val msg: String = Printer.TreeStructure.show(term)
        throw new Exception("Failed to match term of this: " + msg)

    val fieldName =
      expr.asTerm match {
        case Inlined(
              None,
              List(),
              Block(
                List(
                  DefDef(
                    _,
                    List(TermParamClause(List(ValDef(paramName, TypeIdent("Float"), None)))),
                    Inferred(),
                    Some(functionTerm)
                  )
                ),
                Closure(Ident(_), None)
              )
            ) =>
          s"""
          |float ${grabName(name.asTerm)}(float $paramName) {
          |${printTerm(1)(functionTerm)}
          |}
          |""".stripMargin

        case _ =>
          val msg: String = Printer.TreeStructure.show(expr.asTerm)
          throw new Exception("Failed to match this: " + msg)
      }

    Expr(fieldName)
  }
