package indigo.macroshaders

import scala.annotation.tailrec
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.quoted.*

import ShaderDSL.*

object ShaderMacros:

  inline def toAST[In, Out](inline expr: Shader[In, Out]): ProceduralShader = ${ toASTImpl('{ expr }) }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
  private def toASTImpl[In, Out: Type](expr: Expr[Shader[In, Out]])(using Quotes): Expr[ProceduralShader] = {

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

    val proxyLookUp: HashMap[String, (String, Option[ShaderAST])] = new HashMap()

    val shaderDefs: ListBuffer[ShaderAST.Function] = new ListBuffer()

    var inputClassType: Option[String]  = None
    var outputClassType: Option[String] = None

    val isSwizzle     = "^([xyzw]+)$".r
    val isSwizzleable = "^(vec2|vec3|vec4)$".r

    def findReturnType: ShaderAST => Option[ShaderAST] =
      case v: ShaderAST.Empty             => None
      case v: ShaderAST.Block             => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.NamedBlock        => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.ShaderBlock       => v.statements.reverse.headOption.flatMap(findReturnType)
      case v: ShaderAST.Function          => v.returnType
      case v: ShaderAST.CallFunction      => v.returnType
      case v: ShaderAST.FunctionRef       => v.returnType
      case v: ShaderAST.Cast              => v.typeIdent
      case v: ShaderAST.Infix             => v.returnType
      case v: ShaderAST.Assign            => findReturnType(v.right)
      case v: ShaderAST.If                => findReturnType(v.thenTerm)
      case v: ShaderAST.Val               => findReturnType(v.value)
      case v: ShaderAST.DataTypes.closure => v.typeIdent
      case v: ShaderAST.DataTypes.ident   => v.typeIdent
      case v: ShaderAST.DataTypes.float   => v.typeIdent
      case v: ShaderAST.DataTypes.int     => v.typeIdent
      case v: ShaderAST.DataTypes.vec2    => v.typeIdent
      case v: ShaderAST.DataTypes.vec3    => v.typeIdent
      case v: ShaderAST.DataTypes.vec4    => v.typeIdent
      case v: ShaderAST.DataTypes.swizzle => v.typeIdent

    def extractInferredType(typ: TypeTree): Option[String] =
      typ.tpe.classSymbol
        .map(_.name)
        .map {
          case "Float" => "float"
          case "Int"   => "int"
          case n       => n
        }
        .filter {
          case "float" | "int" | "vec2" | "vec3" | "vec4" => true
          case _                                          => false
        }

    def walkStatement(s: Statement, defs: List[ShaderAST]): ShaderAST =
      s match
        case Import(_, _) =>
          throw new Exception("Shaders do not support imports.")

        case Export(_, _) =>
          throw new Exception("Shaders do not support exports.")

        case ClassDef(_, _, _, _, _) =>
          throw new Exception("Shaders do not support classes.")

        case TypeDef(_, _) =>
          throw new Exception("Shaders do not support fancy types.")

        case ValDef(name, typ, Some(term)) =>
          val typeOf = extractInferredType(typ)
          val body   = walkTerm(term, defs)

          body match
            case ShaderAST.Block(List(ShaderAST.FunctionRef(id, rt))) =>
              proxyLookUp += (name -> (id, rt))
              ShaderAST.Empty()

            case _ =>
              ShaderAST.Val(name, body, typeOf)

        case ValDef(name, _, None) =>
          throw new Exception("Shaders do not support val's with no values.")

        case DefDef(fnName, args, rt, Some(term)) =>
          val argNamesTypes =
            args
              .collect { case TermParamClause(ps) => ps }
              .flatten
              .collect { case ValDef(name, typ, _) =>
                val typeOf = extractInferredType(typ)
                (typeOf.getOrElse("void"), name)
              }

          val fn   = if fnName == "$anonfun" then nextFnName else fnName
          val body = walkTerm(term, defs)

          val returnType =
            rt match
              case rtt @ TypeIdent(_) =>
                Option(walkTree(rtt, defs))

              case _ =>
                findReturnType(body)

          body match
            case ShaderAST.Block(List(ShaderAST.FunctionRef(id, rt))) =>
              proxyLookUp += (fn -> (id, rt))
              ShaderAST.Empty()

            case _ =>
              shaderDefs += ShaderAST.Function(fn, argNamesTypes.map(p => p._1 + " " + p._2), body, returnType)
              ShaderAST.FunctionRef(fn, returnType)

        case DefDef(_, _, _, _) =>
          throw new Exception("Unexpected def construction")

        case t: Term =>
          walkTerm(t, defs)

    def walkTree(t: Tree, defs: List[ShaderAST]): ShaderAST =
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
          walkStatement(s, defs)

    def walkTerm(t: Term, defs: List[ShaderAST]): ShaderAST =
      t match

        // Specific hooks we care about

        // Entry point
        case Apply(
              TypeApply(Select(Ident("Shader"), "apply"), List(inType, outType)),
              List(
                Block(
                  Nil,
                  Block(
                    List(
                      DefDef(
                        "$anonfun",
                        List(TermParamClause(List(ValDef(envVarName, Inferred(), None)))),
                        Inferred(),
                        Some(term)
                      )
                    ),
                    Closure(Ident("$anonfun"), None)
                  )
                )
              )
            ) =>
          inputClassType = inType.tpe.classSymbol.map(_.name)
          outputClassType = outType.tpe.classSymbol.map(_.name)

          ShaderAST.ShaderBlock(envVarName, walkTerm(term, defs))

        case Apply(Select(Ident("vec2"), "apply"), args) =>
          args match
            case List(Typed(Repeated(args2, _), _)) =>
              ShaderAST.DataTypes.vec2(args2.map(p => walkTerm(p, defs)))
            case _ =>
              ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p, defs)))

        case Apply(Select(Ident("vec3"), "apply"), args) =>
          args match
            case List(Typed(Repeated(args2, _), _)) =>
              ShaderAST.DataTypes.vec3(args2.map(p => walkTerm(p, defs)))
            case _ =>
              ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p, defs)))

        case Apply(Select(Ident("vec4"), "apply"), args) =>
          args match
            case List(Typed(Repeated(args2, _), _)) =>
              ShaderAST.DataTypes.vec4(args2.map(p => walkTerm(p, defs)))
            case _ =>
              ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p, defs)))

        case Apply(Select(Ident(id), "apply"), args) =>
          val (fnName, rt) = proxyLookUp.get(id).getOrElse((id -> Option(ShaderAST.DataTypes.ident("void"))))
          ShaderAST.CallFunction(fnName, args.map(x => walkTerm(x, defs)), Nil, rt)

        // Generally walking the tree

        case Apply(TypeApply(Select(Ident(id), "apply"), _), List(x)) =>
          ShaderAST.NamedBlock("", id, walkTerm(x, defs))

        case Apply(TypeApply(Select(Ident(namespace), name), _), List(x)) =>
          ShaderAST.NamedBlock(namespace, name, walkTerm(x, defs))

        case Apply(TypeApply(term, _), List(x)) =>
          walkTerm(x, defs)

        // Extension method applies...
        case Apply(Select(Select(Inlined(_, _, _), "vec2"), "apply"), args) =>
          ShaderAST.DataTypes.vec2(args.map(p => walkTerm(p, defs)))

        case Apply(Select(Select(Inlined(_, _, _), "vec3"), "apply"), args) =>
          ShaderAST.DataTypes.vec3(args.map(p => walkTerm(p, defs)))

        case Apply(Select(Select(Inlined(_, _, _), "vec4"), "apply"), args) =>
          ShaderAST.DataTypes.vec4(args.map(p => walkTerm(p, defs)))

        // Casting

        case Select(term, "toInt") =>
          ShaderAST.Cast(walkTerm(term, defs), "int")

        case Select(term, "toFloat") =>
          ShaderAST.Cast(walkTerm(term, defs), "float")

        // Read a field

        case Select(Inlined(None, Nil, Ident(obj)), fieldName) =>
          ShaderAST.DataTypes.ident(s"$obj.$fieldName")

        case Select(Ident(namespace), name) =>
          ShaderAST.DataTypes.ident(s"$namespace.$name")

        // Native method call.
        case Apply(Ident(name), List(Inlined(None, Nil, Ident(defRef)))) =>
          val (fnName, _) = proxyLookUp.get(defRef).getOrElse((defRef -> None))
          val args        = List(ShaderAST.DataTypes.ident(fnName))
          ShaderAST.CallFunction(name, args, args.map(_.render), None)

        //

        case Apply(Select(term, "apply"), xs) =>
          walkTerm(term, defs).find {
            case ShaderAST.CallFunction(_, _, _, _) => true
            case _                                  => false
          } match
            case Some(ShaderAST.CallFunction(id, Nil, Nil, rt)) =>
              ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, defs)), Nil, rt)

            case Some(ShaderAST.CallFunction(id, args, argNames, rt)) =>
              ShaderAST.CallFunction(id, xs.map(tt => walkTerm(tt, defs)), argNames, rt)

            case _ =>
              ShaderAST.Block(xs.map(tt => walkTerm(tt, defs)))

        case Apply(Select(term, op), xs) =>
          op match
            case "+" | "-" | "*" | "/" | "<" | ">" | "==" | "<=" | ">=" =>
              val lhs = walkTerm(term, defs)
              val rhs = xs.headOption.map(tt => walkTerm(tt, defs)).getOrElse(ShaderAST.Empty())
              val rt  = findReturnType(lhs)
              ShaderAST.Infix(op, lhs, rhs, rt)

            case _ =>
              throw new Exception("Shaders do not support infix operator: " + op)

        case Apply(Ident(name), terms) =>
          ShaderAST.CallFunction(name, terms.map(tt => walkTerm(tt, defs)), Nil, None)

        case Inlined(None, _, term) =>
          walkTerm(term, defs)

        case Inlined(Some(Ident(_)), _, term) =>
          walkTerm(term, defs)

        // Swizzle
        case Inlined(Some(Apply(Ident(name), List(gt @ Apply(Select(Ident(genType), "apply"), args)))), _, _)
            if isSwizzle.matches(name) && isSwizzleable.matches(genType) =>
          ShaderAST.DataTypes.swizzle(
            walkTerm(gt, defs),
            name,
            Option(ShaderAST.DataTypes.ident(genType))
          )

        case Inlined(Some(Apply(Ident(name), List(Ident(id)))), _, _) if isSwizzle.matches(name) =>
          ShaderAST.DataTypes.swizzle(
            ShaderAST.DataTypes.ident(id),
            name,
            None
          )
        //

        case Inlined(Some(Apply(Ident(name), args)), ds, Typed(term, typeTree)) =>
          val argNames   = args.map(_ => nextVarName)
          val callArgs   = args.map(tt => walkTerm(tt, defs))
          val pairedArgs = callArgs.zip(argNames)
          val fnArgs =
            pairedArgs.map { p =>
              val typ = p._1.typeIdent.map(_.render).getOrElse("void")
              s"$typ ${p._2}"
            }
          val nextDefs = ds.map(s => walkStatement(s, defs))
          val proxies = nextDefs.flatMap {
            case ShaderAST.Val(proxy, value, _) =>
              pairedArgs.find(p => p._1 == value) match
                case None    => Nil
                case Some(v) => List(proxy -> (v._2 -> None)) // Is None good enough?

            case _ =>
              Nil
          }
          proxyLookUp ++= proxies
          val body       = walkTerm(term, nextDefs)
          val returnType = findReturnType(walkTree(typeTree, defs))

          shaderDefs += ShaderAST.Function(name, fnArgs, body, returnType)
          ShaderAST.CallFunction(name, callArgs, argNames, returnType)

        case Inlined(Some(Select(This(_), _)), _, term) =>
          walkTerm(term, defs)

        case Inlined(Some(tree: Tree), _, _) =>
          walkTree(tree, defs)

        case TypeApply(term, _) =>
          walkTerm(term, defs)

        case Typed(
              Block(List(DefDef(_, args, _, Some(term))), Closure(Ident("$anonfun"), None)),
              Applied(_, types)
            ) =>
          val typesRendered: List[ShaderAST] = types.map(p => walkTree(p, defs))

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
          shaderDefs += ShaderAST.Function(fn, arguments, walkTerm(term, defs), returnType)
          ShaderAST.CallFunction(fn, Nil, argNames, returnType)

        case Typed(term, _) =>
          walkTerm(term, defs)

        case Block(statements, Closure(Ident("$anonfun"), None)) =>
          val ss = statements
            .map(s => walkStatement(s, defs))

          ShaderAST.Block(ss)

        case Block(statements, term) =>
          val ss =
            statements.map(s => walkStatement(s, defs)) :+ walkTerm(term, defs)

          ShaderAST.Block(ss)

        // Literals

        case Literal(FloatConstant(f)) =>
          ShaderAST.DataTypes.float(f)

        case Literal(IntConstant(i)) =>
          ShaderAST.DataTypes.int(i)

        case Literal(UnitConstant()) =>
          ShaderAST.Empty()

        case Literal(constant) =>
          throw new Exception("Shaders do not support constant type: " + constant.show)

        // Refs

        case Ident(name) =>
          val resolvedName = proxyLookUp.get(name).getOrElse((name -> None))._1

          shaderDefs.toList.find(_.id == resolvedName) match
            case None => ShaderAST.DataTypes.ident(resolvedName)
            case Some(ShaderAST.Function(_, _, _, rt)) =>
              ShaderAST.CallFunction(resolvedName, Nil, Nil, rt)

        case Closure(Ident("$anonfun"), None) =>
          ShaderAST.Empty()

        case Closure(term, typeRepr) =>
          ShaderAST.DataTypes.closure(walkTerm(term, defs), typeRepr.map(_.toString))

        case Wildcard() =>
          throw new Exception("Shaders do not support wildcards.")

        case Select(term, _) => // term, name
          walkTerm(term, defs)

        // Unsupported (yet?)

        case This(_) =>
          throw new Exception("Shaders do not support 'this'.")

        case New(_) =>
          throw new Exception("Shaders do not support 'new'.")

        case NamedArg(_, _) =>
          throw new Exception("Shaders do not support named args.")

        case Super(_, _) =>
          throw new Exception("Shaders do not support super.")

        case Assign(lhs, rhs) =>
          ShaderAST.Assign(
            walkTerm(lhs, defs),
            walkTerm(rhs, defs)
          )

        case If(condTerm, thenTerm, elseTerm) =>
          ShaderAST.If(
            walkTerm(condTerm, defs),
            walkTerm(thenTerm, defs),
            walkTerm(elseTerm, defs)
          )

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

    val res           = walkTerm(expr.asTerm, Nil)
    val shaderDefList = shaderDefs.toList

    Expr(ProceduralShader(shaderDefList, res))
  }
