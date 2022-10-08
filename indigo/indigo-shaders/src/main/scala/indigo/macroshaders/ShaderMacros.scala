package indigo.macroshaders

import scala.quoted.*

import ShaderDSL._

object ShaderMacros:

  private def indent(amount: Int): String =
    (0 to amount).foldLeft("") {
      case (acc, 0) => acc
      case (acc, _) => acc + "  "
    }

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
        s"${indent(indentAmount)}return $argName $operator $const;"

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

  inline def toFrag(inline expr: Function0[rgba]): String = ${ toFragImpl('expr) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def toFragImpl(expr: Expr[Function0[rgba]])(using Quotes): Expr[String] = {

    import quotes.reflect.*

    def printTerm(indentAmount: Int): Term => String =
      case Apply(
            Select(Ident("rgba"), "apply"),
            List(
              Literal(FloatConstant(r)),
              Literal(FloatConstant(g)),
              Literal(FloatConstant(b)),
              Literal(FloatConstant(a))
            )
          ) =>
        s"${indent(indentAmount)}COLOR = vec4($r, $g, $b, $a);"

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
                    _,
                    _,
                    Some(functionTerm)
                  )
                ),
                Closure(Ident(_), None)
              )
            ) =>
          s"""
          |void fragment() {
          |${printTerm(1)(functionTerm)}
          |}
          |""".stripMargin

        case _ =>
          val msg: String = Printer.TreeStructure.show(expr.asTerm)
          throw new Exception("Failed to match this: " + msg)
      }

    Expr(fieldName)
  }
