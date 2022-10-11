package indigo.macroshaders

import scala.quoted.*

sealed trait ShaderAST
object ShaderAST:

  given ToExpr[ShaderAST] with {
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(x: ShaderAST)(using Quotes): Expr[ShaderAST] =
      x match
        case f: FragmentProgram =>
          Expr(f)

        case v =>
          throw new Exception("Fragment programs must be a 'FragmentProgram', got: " + v)
  }

  final case class FragmentProgram(statements: List[ShaderAST]) extends ShaderAST
  object FragmentProgram:
    given ToExpr[FragmentProgram] with {
      def apply(x: FragmentProgram)(using Quotes): Expr[FragmentProgram] =
        '{ FragmentProgram(${ Expr(x.statements) }) }
    }

  final case class Block(statements: List[ShaderAST]) extends ShaderAST

  enum DataTypes extends ShaderAST:
    case float(v: Float)
    case vec2(x: Float, y: Float)
    case vec3(x: Float, y: Float, z: Float)
    case vec4(x: Float, y: Float, z: Float, w: Float)
    case rgba(r: Float, g: Float, b: Float, a: Float)

object ShaderASTSample:

  import ShaderAST.*

  inline def sample: ShaderAST =
    FragmentProgram(
      List(
        Block(
          List(
            DataTypes.rgba(1.0, 0.0, 0.0, 1.0)
          )
        )
      )
    )
