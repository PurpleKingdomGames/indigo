package indigo.macroshaders

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.quoted.*

import ShaderDSL.*
import ShaderDSL.glsl

object ShaderMacros:

  private def indent(amount: Int): String =
    (0 to amount).foldLeft("") {
      case (acc, 0) => acc
      case (acc, _) => acc + "  "
    }

  // ---
  // Float => Float

  inline def toGLSLString(name: String, inline expr: Function1[Float, Float]): String = ${
    toGLSLStringImpl('name, 'expr)
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def toGLSLStringImpl(name: Expr[String], expr: Expr[Function1[Float, Float]])(using Quotes): Expr[String] = {

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
          val msg: String = Printer.TreeCode.show(expr.asTerm)
          throw new Exception("Failed to match this: " + msg)
      }

    Expr(fieldName)
  }

  // ---
  // IndigoFrag

  inline def toGLSL(inline expr: IndigoFrag): String = ${ toGLSLImpl('{ expr }) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def toGLSLImpl(expr: Expr[IndigoFrag])(using Quotes): Expr[String] = {

    val traceLog: ListBuffer[String] = new ListBuffer()

    def log(msg: String): Unit = traceLog += msg

    def makeExceptionLog(typ: String, msg: String): String =
      "Failed to match " + typ + ":\n" + msg + "\n\n Trace log:\n" + traceLog.zipWithIndex
        .map(l => s"  ${l._2}) ${l._1}")
        .mkString("\n") + "\n\n"

    import quotes.reflect.*

    def walkStatement(s: Statement): String =
      s match
        case DefDef("$anonfun", List(TermParamClause(List(ValDef("_$1", _, _)))), _, Some(term)) =>
          // anonymous function
          log(Printer.TreeStructure.show(s))
          walkTerm(term)

        case _ =>
          val msg: String = Printer.TreeStructure.show(s)
          throw new Exception(makeExceptionLog("statement", msg))

    def walkTerm(t: Term): String =
      t match
        case Inlined(None, _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Ident(_)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Apply(Select(Ident("IndigoFrag"), "apply"), terms)), _, _) =>
          log(Printer.TreeStructure.show(t))
          s"""//<indigo-fragment>
          |void fragment() {
          |  ${terms.map(walkTerm).mkString("\n")}
          |}
          |//</indigo-fragment>
          |""".stripMargin

        case TypeApply(term, _) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Typed(term, _) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Select(term, _) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Block(statements, _) =>
          log(Printer.TreeStructure.show(t))
          statements.map(walkStatement).mkString("\n")

        case Apply(Select(Ident("rgba"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          s"""vec4(${args.map(walkTerm).mkString(", ")})"""

        case Literal(FloatConstant(v)) =>
          log(Printer.TreeStructure.show(t))
          s"""${v.toString()}f"""

        case _ =>
          val msg: String = Printer.TreeStructure.show(t)
          throw new Exception(makeExceptionLog("term", msg))

    Expr(walkTerm(expr.asTerm))
  }

  // ---
  // Unit => rgba

  inline def toFrag(inline expr: Function0[glsl.rgba]): String = ${ toFragImpl('expr) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def toFragImpl(expr: Expr[Function0[glsl.rgba]])(using Quotes): Expr[String] = {

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
              _,
              _,
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
