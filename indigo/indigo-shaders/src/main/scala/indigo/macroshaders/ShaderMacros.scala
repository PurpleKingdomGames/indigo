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

    var varCount: Int = 0
    def nextVarName: String =
      val res = "v" + varCount.toString
      varCount = varCount + 1
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

    def findReturnType: ShaderAST => Option[ShaderAST] =
      case v: ShaderAST.Empty             => None
      case v: ShaderAST.Block             => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.NamedBlock        => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.ShaderBlock       => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.Function          => v.returnType
      case v: ShaderAST.CallFunction      => v.returnType
      case v: ShaderAST.Val               => findReturnType(v.value)
      case v: ShaderAST.DataTypes.closure => v.typeIdent
      case v: ShaderAST.DataTypes.ident   => v.typeIdent
      case v: ShaderAST.DataTypes.float   => v.typeIdent
      case v: ShaderAST.DataTypes.vec2    => v.typeIdent
      case v: ShaderAST.DataTypes.vec3    => v.typeIdent
      case v: ShaderAST.DataTypes.vec4    => v.typeIdent

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

        case DefDef("$anonfun", args, _, Some(term)) =>
          log(Printer.TreeStructure.show(s))

          val argNames =
            args
              .collect { case TermParamClause(ps) => ps }
              .flatten
              .collect { case ValDef(name, _, _) => name }

          val fn         = nextFnName
          val body       = walkTerm(term)
          val returnType = findReturnType(body)
          shaderDefs += ShaderAST.Function(fn, argNames, body, returnType)
          ShaderAST.CallFunction(fn, Nil, argNames, returnType)

        case DefDef(_, _, _, _) =>
          log(Printer.TreeStructure.show(s))
          throw new Exception("Unexpected def construction")

        case t: Term =>
          walkTerm(t)

    def walkTree(t: Tree): ShaderAST =
      t match
        case TypeIdent("Unit") =>
          ShaderAST.DataTypes.ident("void")

        case TypeIdent("Float") =>
          ShaderAST.DataTypes.ident("float")

        case TypeIdent("Int") =>
          ShaderAST.DataTypes.ident("int")

        case TypeIdent("vec2") =>
          ShaderAST.DataTypes.ident("vec2")

        case TypeIdent("vec3") =>
          ShaderAST.DataTypes.ident("vec3")

        case TypeIdent("vec4") =>
          ShaderAST.DataTypes.ident("vec4")

        case TypeIdent("rgba") =>
          ShaderAST.DataTypes.ident("vec4")

        case TypeIdent(name) =>
          throw new Exception(s"Could not identify type: $name")

        case PackageClause(_, _) =>
          throw new Exception("Shaders do not support packages.")

        case s: Statement =>
          walkStatement(s)

    def walkTerm(t: Term): ShaderAST =
      t match

        // Specific hooks we care about

        case Apply(TypeApply(Select(Ident("Shader"), "apply"), _), body) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.ShaderBlock(body.map(p => walkTerm(p)))

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

        // Extension method applies...
        case Apply(Select(Select(Inlined(_, _, _), "vec2"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p)))

        case Apply(Select(Select(Inlined(_, _, _), "vec3"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p)))

        case Apply(Select(Select(Inlined(_, _, _), "vec4"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p)))

        case Apply(Select(Select(Inlined(_, _, _), "rgba"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p)))
        //

        case Apply(Select(term, _), xs) => // Losing the operator, e.g. '+' by using _?
          log(Printer.TreeStructure.show(t))

          walkTerm(term).find {
            case ShaderAST.CallFunction(_, _, _, _) => true
            case _                                  => false
          } match
            case Some(ShaderAST.CallFunction(id, args, argNames, rt)) =>
              ShaderAST.CallFunction(id, xs.map(walkTerm), argNames, rt)

            case _ =>
              ShaderAST.Block(xs.map(walkTerm))

        case Apply(Ident(name), terms) =>
          ShaderAST.CallFunction(name, terms.map(walkTerm), Nil, None)

        case Inlined(None, _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Ident(_)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(Apply(Ident(name), args)), _, term) =>
          log(Printer.TreeStructure.show(t))
          
          val body = walkTerm(term)
          val returnType = findReturnType(body)
          val fn         = nextFnName
          shaderDefs += ShaderAST.Function(fn, Nil, body, returnType)
          ShaderAST.CallFunction(fn, Nil, Nil, returnType)

        case Inlined(Some(Select(This(_), _)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Inlined(Some(tree: Tree), _, _) =>
          log(Printer.TreeStructure.show(t))
          walkTree(tree)

        case TypeApply(term, _) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term)

        case Typed(
              Block(List(DefDef(_, args, _, Some(term))), Closure(Ident("$anonfun"), None)),
              Applied(_, types)
            ) =>
          log(Printer.TreeStructure.show(t))

          val typesRendered: List[ShaderAST] = types.map(walkTree)

          val returnType: Option[ShaderAST] =
            typesRendered.reverse.headOption

          val argNames =
            args
              .collect { case TermParamClause(ps) => ps }
              .flatten
              .collect { case ValDef(name, _, _) => name }

          val arguments = typesRendered
            .dropRight(1)
            .zip(argNames)
            .map { case (typ, nme) => s"""${typ.render} $nme""" }

          val fn = nextFnName
          shaderDefs += ShaderAST.Function(fn, arguments, walkTerm(term), returnType)
          ShaderAST.CallFunction(fn, Nil, argNames, returnType)

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

    // println(">>> Everything")
    // println(Printer.TreeStructure.show(expr.asTerm))
    // println("<<<")
    val res = walkTerm(expr.asTerm)

    Expr(ProceduralShader(shaderDefs.toList, res))
  }
