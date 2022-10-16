package indigo.macroshaders

import scala.quoted.*

sealed trait ShaderAST
object ShaderAST:

  given ToExpr[ShaderAST] with {
    def apply(x: ShaderAST)(using Quotes): Expr[ShaderAST] =
      x match
        case v: FragmentProgram => Expr(v)
        case v: Block           => Expr(v)
        case v: Function        => Expr(v)
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

  final case class Function(id: String, body: ShaderAST) extends ShaderAST
  object Function:
    given ToExpr[Function] with {
      def apply(x: Function)(using Quotes): Expr[Function] =
        '{ Function(${ Expr(x.id) }, ${ Expr(x.body) }) }
    }

  enum DataTypes extends ShaderAST:
    case ident(id: String)
    case float(v: Float)
    case vec2(args: List[ShaderAST])
    case vec3(args: List[ShaderAST])
    case vec4(args: List[ShaderAST])

  object DataTypes:
    given ToExpr[DataTypes] with {
      def apply(x: DataTypes)(using Quotes): Expr[DataTypes] =
        x match
          case v: DataTypes.ident => Expr(v)
          case v: DataTypes.float => Expr(v)
          case v: DataTypes.vec2  => Expr(v)
          case v: DataTypes.vec3  => Expr(v)
          case v: DataTypes.vec4  => Expr(v)
    }
    given ToExpr[ident] with {
      def apply(x: ident)(using Quotes): Expr[ident] =
        '{ ident(${ Expr(x.id) }) }
    }
    given ToExpr[float] with {
      def apply(x: float)(using Quotes): Expr[float] =
        '{ float(${ Expr(x.v) }) }
    }
    given ToExpr[vec2] with {
      def apply(x: vec2)(using Quotes): Expr[vec2] =
        '{ vec2(${ Expr(x.args) }) }
    }
    given ToExpr[vec3] with {
      def apply(x: vec3)(using Quotes): Expr[vec3] =
        '{ vec3(${ Expr(x.args) }) }
    }
    given ToExpr[vec4] with {
      def apply(x: vec4)(using Quotes): Expr[vec4] =
        '{ vec4(${ Expr(x.args) }) }
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

        case Function(name, body) =>
          s"""void $name(){${body.render}}"""

        case DataTypes.ident(id) =>
          s"$id"

        case DataTypes.float(v) =>
          s"${rf(v)}"

        case DataTypes.vec2(args) =>
          s"vec2(${args.map(_.render).mkString(", ")})"

        case DataTypes.vec3(args) =>
          s"vec3(${args.map(_.render).mkString(", ")})"

        case DataTypes.vec4(args) =>
          s"vec4(${args.map(_.render).mkString(", ")})"
