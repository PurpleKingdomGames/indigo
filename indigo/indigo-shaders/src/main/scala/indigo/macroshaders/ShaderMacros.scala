package indigo.macroshaders

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.quoted.*

import ShaderDSL.*

object ShaderMacros:

  inline def toAST[Env, A](inline expr: Shader[Env, A]): ProceduralShader = ${ toASTImpl('{ expr }) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
  private def toASTImpl[Env, A](expr: Expr[Shader[Env, A]])(using Quotes): Expr[ProceduralShader] = {

    import quotes.reflect.*

    var fnCount: Int = 0

    def nextFnName: String =
      val res = "fn" + fnCount.toString
      fnCount = fnCount + 1
      res

    val shaderDefs: ListBuffer[ShaderAST] = new ListBuffer()

    val traceLog: ListBuffer[String] = new ListBuffer()

    def log(msg: String): Unit = traceLog += msg

    def makeExceptionLog(typ: String, msg: String): String =
      val count = 3
      val toLog = traceLog.zipWithIndex
      "Failed to match " + typ + ":\n" + msg + "\n\n Trace log (last " + count.toString + "):\n" + toLog
        .dropInPlace(Math.max(0, toLog.length - count))
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

        case ValDef(name, _, Some(term)) =>
          log(Printer.TreeStructure.show(s))
          ShaderAST.Val(name, walkTerm(term))

        case ValDef(name, _, None) =>
          throw new Exception("Shaders do not support val's with no values.")

        case DefDef("$anonfun", List(TermParamClause(List(ValDef(argName, _, _)))), _, Some(term)) =>
          log(Printer.TreeStructure.show(s))
          val fn = nextFnName
          shaderDefs += ShaderAST.Function(fn, List(argName), walkTerm(term))
          ShaderAST.CallFunction(fn, Nil)

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
        case PackageClause(_, _) =>
          throw new Exception("Shaders do not support packages.")

        case s: Statement =>
          walkStatement(s)

        case _ =>
          val msg: String = Printer.TreeStructure.show(t)
          throw new Exception(makeExceptionLog("tree", msg))

    def walkTerm(t: Term): ShaderAST =
      t match

        // Specific hooks we care about

        case Apply(Select(Ident("vec2"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p)))

        case Apply(Select(Ident("vec3"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p)))

        case Apply(Select(Ident("vec4"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p)))

        case Apply(Select(Ident("rgba"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          println("rgba")
          println(args.length)
          // println(Printer.TreeStructure.show(args(0)))
          println(Printer.TreeStructure.show(args(1)))
          ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p)))

        case Apply(Select(Ident(id), "apply"), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.NamedBlock(id, "", walkTerm(x))

        case Select(Ident(namespace), name) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.ident(s"$namespace.$name")

        // Generally walking the tree

        case Apply(TypeApply(Select(Ident(id), "apply"), _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.NamedBlock("", id, walkTerm(x))

        case Apply(TypeApply(Select(Ident(namespace), name), _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.NamedBlock(namespace, name, walkTerm(x))

        case Apply(TypeApply(term, _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(x)

        case Apply(Select(term, _), List(x)) =>
          log(Printer.TreeStructure.show(t))

          walkTerm(term).find {
            case ShaderAST.CallFunction(id, args) => true
            case _                                => false
          } match
            case Some(ShaderAST.CallFunction(id, args)) =>
              ShaderAST.CallFunction(id, List(walkTerm(x)))

            case _ =>
              walkTerm(x)

        case Inlined(None, _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Ident(_)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Apply(Ident(_), _)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Select(This(_), _)), _, term) =>
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

        case Block(statements, Closure(Ident("$anonfun"), None)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Block(statements.map(walkStatement))

        case Block(statements, term) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Block(statements.map(walkStatement) :+ walkTerm(term))

        // Literals

        case Literal(FloatConstant(f)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.float(f)

        case Literal(constant) =>
          throw new Exception("Shaders do not support constant type: " + constant.show)

        // Refs

        case Ident(name) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.ident(name)

        case Closure(Ident("$anonfun"), None) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Empty()

        case Closure(term, typeRepr) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.closure(walkTerm(term), typeRepr.map(_.toString))

        case Wildcard() =>
          throw new Exception("Shaders do not support wildcards.")

        case Select(term, _) => // term, name
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        // Unsupported (yet?)

        case This(_) =>
          throw new Exception("Shaders do not support 'this'.")

        case New(_) =>
          throw new Exception("Shaders do not support 'new'.")

        case NamedArg(_, _) =>
          throw new Exception("Shaders do not support named args.")

        case Super(_, _) =>
          throw new Exception("Shaders do not support super.")

        case Assign(_, _) =>
          throw new Exception("Shaders do not support assign.")

        case If(_, _, _) =>
          throw new Exception("Shaders do not support if statements.")

        case Match(_, _) =>
          throw new Exception("Shaders do not support pattern matching.")

        case SummonFrom(_) =>
          throw new Exception("Shaders do not support summoning.")

        case Try(_, _, _) =>
          throw new Exception("Shaders do not support try blocks.")

        case Return(_, _) =>
          throw new Exception("Shaders do not support return statements.")

        case Repeated(_, _) =>
          throw new Exception("Shaders do not support repeated arguments.")

        case SelectOuter(_, _, _) =>
          throw new Exception("Shaders do not support outer selectors.")

        case While(_, _) =>
          throw new Exception("Shaders do not support while loops.")

        // Catch all for values we don't understand

        case _ =>
          val msg: String = Printer.TreeStructure.show(t)
          throw new Exception(makeExceptionLog("term", msg))

    // println(">>> Everything")
    // println(Printer.TreeStructure.show(expr.asTerm))
    // println("<<<")
    val res = walkTerm(expr.asTerm)

    Expr(ProceduralShader(shaderDefs.toList, res))
  }
