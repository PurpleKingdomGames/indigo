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

    val isSwizzle     = "^([xyzw]+)$".r
    val isSwizzleable = "^(vec2|vec3|vec4)$".r

    def findReturnType: ShaderAST => Option[ShaderAST] =
      case v: ShaderAST.Empty             => None
      case v: ShaderAST.Block             => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.NamedBlock        => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.ShaderBlock       => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.Function          => v.returnType
      case v: ShaderAST.CallFunction      => v.returnType
      case v: ShaderAST.Infix             => v.returnType
      case v: ShaderAST.Val               => findReturnType(v.value)
      case v: ShaderAST.DataTypes.closure => v.typeIdent
      case v: ShaderAST.DataTypes.ident   => v.typeIdent
      case v: ShaderAST.DataTypes.float   => v.typeIdent
      case v: ShaderAST.DataTypes.vec2    => v.typeIdent
      case v: ShaderAST.DataTypes.vec3    => v.typeIdent
      case v: ShaderAST.DataTypes.vec4    => v.typeIdent
      case v: ShaderAST.DataTypes.swizzle => v.typeIdent

    def walkStatement(s: Statement, defs: List[ShaderAST], proxyLookUp: Map[String, String]): ShaderAST =
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
          ShaderAST.Val(name, walkTerm(term, defs, proxyLookUp))

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
          val body       = walkTerm(term, defs, proxyLookUp)
          val returnType = findReturnType(body)
          shaderDefs += ShaderAST.Function(fn, argNames, body, returnType)
          ShaderAST.CallFunction(fn, Nil, argNames, returnType)

        case DefDef(_, _, _, _) =>
          log(Printer.TreeStructure.show(s))
          throw new Exception("Unexpected def construction")

        case t: Term =>
          walkTerm(t, defs, proxyLookUp)

    def walkTree(t: Tree, defs: List[ShaderAST], proxyLookUp: Map[String, String]): ShaderAST =
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

        case TypeIdent(name) =>
          throw new Exception(s"Could not identify type: $name")

        case PackageClause(_, _) =>
          throw new Exception("Shaders do not support packages.")

        case s: Statement =>
          walkStatement(s, defs, proxyLookUp)

    def walkTerm(t: Term, defs: List[ShaderAST], proxyLookUp: Map[String, String]): ShaderAST =
      t match

        // Specific hooks we care about

        case Apply(TypeApply(Select(Ident("Shader"), "apply"), _), body) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.ShaderBlock(body.map(p => walkTerm(p, defs, proxyLookUp)))

        case Apply(Select(Ident("vec2"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          args match
            case List(Typed(Repeated(args2, _), _)) =>
              ShaderAST.DataTypes.vec2(args2.map(p => walkTerm(p, defs, proxyLookUp)))
            case _ =>
              ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p, defs, proxyLookUp)))

        case Apply(Select(Ident("vec3"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          args match
            case List(Typed(Repeated(args2, _), _)) =>
              ShaderAST.DataTypes.vec3(args2.map(p => walkTerm(p, defs, proxyLookUp)))
            case _ =>
              ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p, defs, proxyLookUp)))

        case Apply(Select(Ident("vec4"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          args match
            case List(Typed(Repeated(args2, _), _)) =>
              ShaderAST.DataTypes.vec4(args2.map(p => walkTerm(p, defs, proxyLookUp)))
            case _ =>
              ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p, defs, proxyLookUp)))

        case Apply(Select(Ident(id), "apply"), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.NamedBlock(id, "", walkTerm(x, defs, proxyLookUp))

        case Select(Ident(namespace), name) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.ident(s"$namespace.$name")

        // Generally walking the tree

        case Apply(TypeApply(Select(Ident(id), "apply"), _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.NamedBlock("", id, walkTerm(x, defs, proxyLookUp))

        case Apply(TypeApply(Select(Ident(namespace), name), _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.NamedBlock(namespace, name, walkTerm(x, defs, proxyLookUp))

        case Apply(TypeApply(term, _), List(x)) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(x, defs, proxyLookUp)

        // Extension method applies...
        case Apply(Select(Select(Inlined(_, _, _), "vec2"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p, defs, proxyLookUp)))

        case Apply(Select(Select(Inlined(_, _, _), "vec3"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p, defs, proxyLookUp)))

        case Apply(Select(Select(Inlined(_, _, _), "vec4"), "apply"), args) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p, defs, proxyLookUp)))

        // Read a field

        case Select(Inlined(None, Nil, Ident(obj)), fieldName) =>
          ShaderAST.DataTypes.ident(s"$obj.$fieldName")

        // Native method call.
        case Apply(Ident(name), List(Inlined(None, Nil, Ident(defRef)))) =>
          val args = List(ShaderAST.DataTypes.ident(proxyLookUp.get(defRef).getOrElse(defRef)))
          ShaderAST.CallFunction(name, args, args.map(_.render), None)

        //

        case Apply(Select(term, op), xs) =>
          log(Printer.TreeStructure.show(t))

          walkTerm(term, defs, proxyLookUp).find {
            case ShaderAST.CallFunction(_, _, _, _) => true
            case _                                  => false
          } match
            case Some(ShaderAST.CallFunction(id, Nil, Nil, rt)) =>
              ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, defs, proxyLookUp)), Nil, rt)

            case Some(ShaderAST.CallFunction(id, args, argNames, rt)) =>
              op match
                case "+" | "-" | "*" | "/" =>
                  ShaderAST.Infix(
                    op,
                    ShaderAST.CallFunction(id, args, argNames, rt),
                    xs.headOption.map(tt => walkTerm(tt, defs, proxyLookUp)).getOrElse(ShaderAST.Empty()),
                    rt
                  )

                case _ =>
                  ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, defs, proxyLookUp)), argNames, rt)

            case _ =>
              ShaderAST.Block(xs.map(tt => walkTerm(tt, defs, proxyLookUp)))

        case Apply(Ident(name), terms) =>
          ShaderAST.CallFunction(name, terms.map(tt => walkTerm(tt, defs, proxyLookUp)), Nil, None)

        case Inlined(None, _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term, defs, proxyLookUp)

        case Inlined(Some(Ident(_)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term, defs, proxyLookUp)

        // Swizzle
        case Inlined(Some(Apply(Ident(name), List(gt @ Apply(Select(Ident(genType), "apply"), args)))), _, _)
            if isSwizzle.matches(name) && isSwizzleable.matches(genType) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.swizzle(walkTerm(gt, defs, proxyLookUp), name)

        case Inlined(Some(Apply(Ident(name), List(Ident(id)))), _, _) if isSwizzle.matches(name) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.swizzle(ShaderAST.DataTypes.ident(id), name)
        //

        case Inlined(Some(Apply(Ident(name), args)), ds, Typed(term, typeTree)) =>
          log(Printer.TreeStructure.show(t))

          val argNames   = args.map(_ => nextVarName)
          val callArgs   = args.map(tt => walkTerm(tt, defs, proxyLookUp))
          val pairedArgs = callArgs.zip(argNames)
          val fnArgs =
            pairedArgs.map { p =>
              val typ = p._1.typeIdent.map(_.render).getOrElse("void")
              s"$typ ${p._2}"
            }
          val nextDefs = ds.map(s => walkStatement(s, defs, proxyLookUp))
          val proxies = nextDefs.flatMap {
            case ShaderAST.Val(proxy, value) =>
              pairedArgs.find(p => p._1 == value) match
                case None    => Nil
                case Some(v) => List(proxy -> v._2)

            case _ =>
              Nil
          }
          val body       = walkTerm(term, nextDefs, proxies.toMap ++ proxyLookUp)
          val returnType = findReturnType(walkTree(typeTree, defs, proxyLookUp))

          shaderDefs += ShaderAST.Function(name, fnArgs, body, returnType)
          ShaderAST.CallFunction(name, callArgs, argNames, returnType)

        case Inlined(Some(Select(This(_), _)), _, term) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term, defs, proxyLookUp)

        case Inlined(Some(tree: Tree), _, _) =>
          log(Printer.TreeStructure.show(t))
          walkTree(tree, defs, proxyLookUp)

        case TypeApply(term, _) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term, defs, proxyLookUp)

        case Typed(
              Block(List(DefDef(_, args, _, Some(term))), Closure(Ident("$anonfun"), None)),
              Applied(_, types)
            ) =>
          log(Printer.TreeStructure.show(t))

          val typesRendered: List[ShaderAST] = types.map(p => walkTree(p, defs, proxyLookUp))

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
          shaderDefs += ShaderAST.Function(fn, arguments, walkTerm(term, defs, proxyLookUp), returnType)
          ShaderAST.CallFunction(fn, Nil, argNames, returnType)

        case Typed(term, _) =>
          log(Printer.TreeStructure.show(t))
          walkTerm(term, defs, proxyLookUp)

        case Block(statements, Closure(Ident("$anonfun"), None)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Block(statements.map(s => walkStatement(s, defs, proxyLookUp)))

        case Block(statements, term) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Block(statements.map(s => walkStatement(s, defs, proxyLookUp)) :+ walkTerm(term, defs, proxyLookUp))

        // Literals

        case Literal(FloatConstant(f)) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.float(f)

        case Literal(constant) =>
          throw new Exception("Shaders do not support constant type: " + constant.show)

        // Refs

        case Ident(name) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.ident(proxyLookUp.get(name).getOrElse(name))

        case Closure(Ident("$anonfun"), None) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.Empty()

        case Closure(term, typeRepr) =>
          log(Printer.TreeStructure.show(t))
          ShaderAST.DataTypes.closure(walkTerm(term, defs, proxyLookUp), typeRepr.map(_.toString))

        case Wildcard() =>
          throw new Exception("Shaders do not support wildcards.")

        case Select(term, _) => // term, name
          log(Printer.TreeStructure.show(t))
          walkTerm(term, defs, proxyLookUp)

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
    val res = walkTerm(expr.asTerm, Nil, Map())

    Expr(ProceduralShader(shaderDefs.toList, res))
  }
