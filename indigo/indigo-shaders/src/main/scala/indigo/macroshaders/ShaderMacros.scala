package indigo.macroshaders

import scala.quoted._

import ShaderDSL._

object ShaderMacros:

  inline def debugSingle(inline expr: Any): Unit = ${ debugSingleImpl('expr) }

  private def debugSingleImpl(expr: Expr[Any])(using Quotes): Expr[Unit] =
    '{ println("Value of " + ${ Expr(expr.show) } + " is " + $expr) }

  inline def debug(inline exprs: Any*): Unit = ${ debugImpl('exprs) }

  private def debugImpl(exprs: Expr[Seq[Any]])(using Quotes): Expr[Unit] = {
    def showWithValue(e: Expr[_]): Expr[String] = '{ ${ Expr(e.show) } + " = " + $e }

    val stringExps: Seq[Expr[String]] = exprs match
      case Varargs(es) =>
        es.map(showWithValue)
      case e =>
        List(showWithValue(e))

    val concatenatedStringsExp = stringExps.reduceOption((e1, e2) => '{ $e1 + ", " + $e2 }).getOrElse('{ "" })

    '{ println($concatenatedStringsExp) }
  }

  inline def toGLSL(inline expr: Function1[Float, Float]): String = ${ toGLSLImpl('expr) }

  private def toGLSLImpl(expr: Expr[Function1[Float, Float]])(using Quotes): Expr[String] = {

    println(">>" + expr.show)

    val x: Expr[String] = {
      val fieldName =

        import quotes.reflect._

        println("term" + expr.asTerm.show)

        expr.asTerm match {
          // case Inlined(optTree, listDefinition, term) =>
          //   println("tree:")
          //   println(optTree)
          //   println("definitions:")
          //   println(listDefinition)
          //   println("term:")
          //   println(term)
          //   "fishcakes"

          case Inlined(
                _,
                List(),
                Block(
                  List(
                    DefDef(_, valueDefinitions, returnType, statementTerm)
                    // DefDef(_,List(List(ValDef(x,Ident(Float),EmptyTree))))
                  ),
                  Closure(_, _)
                )
              ) =>
            valueDefinitions.toString + " :: " + returnType.show + " :: " + statementTerm.map(_.show)
        }

      //$anonfun :: List(List(ValDef(x,Ident(Float),EmptyTree))) :: scala.Float :: Some(x.+(1.0f))

      // val s = 
      //   Block(
      //     List(
      //       DefDef(
      //         $anonfun,
      //         List(
      //           List(
      //             ValDef(x,Ident(Float),EmptyTree)
      //           )
      //         ),
      //         TypeTree[TypeRef(TermRef(ThisType(TypeRef(NoPrefix,module class <root>)),object scala),Float)],
      //         Apply(Select(Ident(x),+),List(Literal(Constant(1.0))))
      //       )
      //     ),
      //     Closure(List(),Ident($anonfun),EmptyTree)
      //   )

      Expr(fieldName)
    }

    // x

    println(x.show)

    import quotes.reflect.*

    // println(">>> " +  x.show(using Printer.TreeStructure))
    println(">>>" + Printer.TreeStructure.show(expr.asTerm))
    //>>>Inlined(None, Nil, Block(List(DefDef("$anonfun", List(TermParamClause(List(ValDef("x", TypeIdent("Float"), None)))), Inferred(), Some(Apply(Select(Ident("x"), "+"), List(Literal(FloatConstant(1.0f))))))), Closure(Ident("$anonfun"), None)))


    /*
def getNameImpl[T](f: Expr[T => Any])(using Quotes): Expr[String] = {
  import quotes.reflect._

  val fieldName = f.asTerm match {
    case Inlined(
      _,
      List(),
      Block(
        List(DefDef(
          _,
          List(),
          List(List(ValDef(_, _, _))),
          _,
          Some(Select(Ident(_), fn))
        )),
        Closure(_, _)
      )
    ) => fn
  }

  Expr(fieldName)
}
     */

    // Expr(routine("fish", Ref("z"))(float(10.0)).render)
    x
  }
//'{ println("Value of " + ${Expr(expr.show)} + " is " + $expr) }

// given ToExpr[routine] with
//  def toExpr(r: routine) =
//   '{ (r.name: Float) => r.expr }
// if b then '{ true } else '{ false }
