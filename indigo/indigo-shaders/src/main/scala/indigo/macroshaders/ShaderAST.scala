package indigo.macroshaders

import scala.quoted.*

sealed trait ShaderAST
object ShaderAST:

  given ToExpr[ShaderAST] with {
    def apply(x: ShaderAST)(using Quotes): Expr[ShaderAST] =
      x match
        case v: FragmentProgram => Expr(v)
        case v: Block           => Expr(v)
        case v: DataTypes       => Expr(v)
  }

  final case class FragmentProgram(statements: List[ShaderAST]) extends ShaderAST
  object FragmentProgram:
    given ToExpr[FragmentProgram] with {
      def apply(x: FragmentProgram)(using Quotes): Expr[FragmentProgram] =
        '{ FragmentProgram(${ Expr(x.statements) }) }
    }

    def apply(statements: ShaderAST*): FragmentProgram =
      FragmentProgram(statements.toList)

  final case class Block(statements: List[ShaderAST]) extends ShaderAST
  object Block:
    given ToExpr[Block] with {
      def apply(x: Block)(using Quotes): Expr[Block] =
        '{ Block(${ Expr(x.statements) }) }
    }

    def apply(statements: ShaderAST*): Block =
      Block(statements.toList)

  enum DataTypes extends ShaderAST:
    case float(v: Float)
    case vec2(x: Float, y: Float)
    case vec3(x: Float, y: Float, z: Float)
    case vec4(x: Float, y: Float, z: Float, w: Float)
    case rgba(r: Float, g: Float, b: Float, a: Float)

  object DataTypes:
    given ToExpr[DataTypes] with {
      def apply(x: DataTypes)(using Quotes): Expr[DataTypes] =
        x match
          case v: DataTypes.float => Expr(v)
          case v: DataTypes.vec2  => Expr(v)
          case v: DataTypes.vec3  => Expr(v)
          case v: DataTypes.vec4  => Expr(v)
          case v: DataTypes.rgba  => Expr(v)
    }
    given ToExpr[float] with {
      def apply(x: float)(using Quotes): Expr[float] =
        '{ float(${ Expr(x.v) }) }
    }
    given ToExpr[vec2] with {
      def apply(x: vec2)(using Quotes): Expr[vec2] =
        '{ vec2(${ Expr(x.x) }, ${ Expr(x.y) }) }
    }
    given ToExpr[vec3] with {
      def apply(x: vec3)(using Quotes): Expr[vec3] =
        '{ vec3(${ Expr(x.x) }, ${ Expr(x.y) }, ${ Expr(x.z) }) }
    }
    given ToExpr[vec4] with {
      def apply(x: vec4)(using Quotes): Expr[vec4] =
        '{ vec4(${ Expr(x.x) }, ${ Expr(x.y) }, ${ Expr(x.z) }, ${ Expr(x.w) }) }
    }
    given ToExpr[rgba] with {
      def apply(x: rgba)(using Quotes): Expr[rgba] =
        '{ rgba(${ Expr(x.r) }, ${ Expr(x.g) }, ${ Expr(x.b) }, ${ Expr(x.a) }) }
    }

  extension (ast: ShaderAST)
    def render: String =
      def rf(f: Float): String =
        val s = f.toString
        if s.contains(".") then s else s + ".0"

      ast match
        case FragmentProgram(statements) =>
          s"""//<indigo-fragment>
          |void fragment() {
          |  COLOR = ${statements.map(_.render).mkString}
          |}
          |//</indigo-fragment>
          |""".stripMargin

        case Block(statements) =>
          statements.map(s => s.render + ";").mkString("\n")

        case DataTypes.float(v) =>
          s"${rf(v)}"

        case DataTypes.vec2(x, y) =>
          s"vec2(${rf(x)}, ${rf(y)})"

        case DataTypes.vec3(x, y, z) =>
          s"vec3(${rf(x)}, ${rf(y)}, ${rf(z)})"

        case DataTypes.vec4(x, y, z, w) =>
          s"vec4(${rf(x)}, ${rf(y)}, ${rf(z)}, ${rf(w)})"

        case DataTypes.rgba(r, g, b, a) =>
          s"rgba(${rf(r)}, ${rf(g)}, ${rf(b)}, ${rf(a)})"
