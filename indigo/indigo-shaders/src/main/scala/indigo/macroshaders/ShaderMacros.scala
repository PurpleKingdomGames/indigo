package indigo.macroshaders

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.quoted.*

import ShaderDSL.*

object ShaderMacros:

  private def indent(amount: Int): String =
    (0 to amount).foldLeft("") {
      case (acc, 0) => acc
      case (acc, _) => acc + "  "
    }

  // Debugging

  inline def showASTExpr(inline expr: ShaderAST): String =
    ${ showASTExprImpl('expr) }

  private def showASTExprImpl(expr: Expr[ShaderAST])(using Quotes): Expr[String] = {
    import quotes.reflect.*
    Expr(Printer.TreeCode.show(expr.asTerm))
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

  // inline def toAST(inline expr: IndigoFrag): ShaderAST = ${ toASTImpl('{ expr }) }
  inline def toAST[Env, A](inline expr: ShaderContext[Env, A]): ShaderAST = ${ toASTImpl('{ expr }) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def toASTImpl[Env, A](expr: Expr[ShaderContext[Env, A]])(using Quotes): Expr[ShaderAST] = {

    import quotes.reflect.*

    val traceLog: ListBuffer[String] = new ListBuffer()

    def log(msg: String): Unit = traceLog += msg

    def makeExceptionLog(typ: String, msg: String): String =
      val toLog = traceLog.zipWithIndex
      "Failed to match " + typ + ":\n" + msg + "\n\n Trace log (last 3):\n" + toLog
        .dropInPlace(Math.max(0, toLog.length - 3))
        .map(l => s"  ${l._2}) ${l._1}")
        .mkString("\n") + "\n\n"

    def walkStatement(s: Statement): ShaderAST =
      s match
        case Import(_, _) =>
          throw new Exception("Shaders do not support imports.")

        case Export(_, _) =>
          throw new Exception("Shaders do not support exports.")

        case ClassDef(_, _, _, _, _) =>
          throw new Exception("Shaders do not support classes.")

        case TypeDef(_, _) =>
          throw new Exception("Shaders do not support fancy types.")

        case ValDef(_, _, _) =>
          log(Printer.TreeStructure.show(s))
          throw new Exception("Val support is not implemented")

        case DefDef("$anonfun", List(TermParamClause(List(ValDef(argName, _, _)))), _, Some(term)) =>
          // anonymous function
          log(Printer.TreeStructure.show(s))
          ShaderAST.Function(argName, walkTerm(term))

        case DefDef(_, _, _, _) =>
          log(Printer.TreeStructure.show(s))
          throw new Exception("Unexpected def construction")

        case t: Term =>
          walkTerm(t)

        case _ =>
          val msg: String = Printer.TreeStructure.show(s)
          throw new Exception(makeExceptionLog("statement", msg))

    def walkTree(t: Tree): ShaderAST =
      t match
        case Apply(TypeApply(Select(Ident(id), "apply"), _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Function(id, walkTerm(x))

        case Apply(Select(Ident(id), "apply"), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Function(id, walkTerm(x))

        case Apply(Select(Ident("rgba"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p)))

        case _ =>
          val msg: String = Printer.TreeStructure.show(t)
          throw new Exception(makeExceptionLog("tree", msg))

    def walkTerm(t: Term): ShaderAST =
      t match

        // Specific hooks we care about

        case Apply(TypeApply(Select(Ident(id), "apply"), _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Function(id, walkTerm(x))

        case Apply(TypeApply(Select(Ident(namespace), name), _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Function(s"$namespace.$name", walkTerm(x))

        // Data type primitives

        // case Apply(Select(Ident("rgba"), "apply"), args) =>
        //   args match
        //     case List(
        //           Literal(FloatConstant(r)),
        //           Literal(FloatConstant(g)),
        //           Literal(FloatConstant(b)),
        //           Literal(FloatConstant(a))
        //         ) =>
        //       log(Printer.TreeStructure.show(t))
        //       ShaderAST.DataTypes.rgba(r, g, b, a)

        //     case _ =>
        //       val msg: String = args.map(Printer.TreeStructure.show).mkString(", ")
        //       throw new Exception(makeExceptionLog("'rgba args'", msg))

        case Literal(FloatConstant(v)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.float(v)

        case Select(Ident(namespace), name) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.ident(s"$namespace.$name")

        case Ident(name) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.ident(name)

        // Generally walking the tree

        // case Apply(TypeApply(Select(Ident(id), "apply"), _), List(Block(statements, _))) =>
        //   log(Printer.TreeStructure.show(t))
        //   ShaderAST.Block(id, statements.map(walkStatement))

        case Inlined(None, _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Ident(_)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(tree: Tree), _, _) =>
          log(Printer.TreeStructure.show(t))
          walkTree(tree)

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
          ShaderAST.Block(statements.map(walkStatement))

        // Catch all for values we don't understand

        case _ =>
          val msg: String = Printer.TreeStructure.show(t)
          throw new Exception(makeExceptionLog("term", msg))

    Expr(walkTerm(expr.asTerm))
  }

  // ---
  // Unit => rgba

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
