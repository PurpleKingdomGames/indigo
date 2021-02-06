package indigo.shaders

import scala.quoted._

object ShaderMacros:

  inline def debugSingle(inline expr: Any): Unit = ${debugSingleImpl('expr)} 
  
  private def debugSingleImpl(expr: Expr[Any])(using Quotes): Expr[Unit] = 
    '{ println("Value of " + ${Expr(expr.show)} + " is " + $expr) }
  
  inline def debug(inline exprs: Any*): Unit = ${debugImpl('exprs)}

  private def debugImpl(exprs: Expr[Seq[Any]])(using Quotes): Expr[Unit] = {
    def showWithValue(e: Expr[_]): Expr[String] = '{${Expr(e.show)} + " = " + $e}
  
    val stringExps: Seq[Expr[String]] = exprs match 
      case Varargs(es) => 
        es.map(showWithValue)
      case e =>
        List(showWithValue(e))
  
    val concatenatedStringsExp = stringExps.reduceOption((e1, e2) => '{$e1 + ", " + $e2}).getOrElse('{""})

    '{println($concatenatedStringsExp)}
  }

  inline def toGLSL(inline expr: Function1[Float, Float]): String = ${toGLSLImpl('expr)} 

  private def toGLSLImpl(expr: Expr[Function1[Float, Float]])(using Quotes): Expr[String] = {
    
    expr match {
      // case Expr()

      case x =>
        println(expr.show)
    }

    Expr(routine("fish", Ref("z"))(float(10.0)).render)
  }
    //'{ println("Value of " + ${Expr(expr.show)} + " is " + $expr) }

  // given ToExpr[routine] with
  //  def toExpr(r: routine) =
  //   '{ (r.name: Float) => r.expr }
      // if b then '{ true } else '{ false }
