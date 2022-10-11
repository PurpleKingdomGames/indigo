package indigo.macroshaders

import scala.quoted.*

sealed trait ShaderAST
object ShaderAST:

  given ToExpr[ShaderAST] with {
    def apply(x: ShaderAST)(using Quotes): Expr[ShaderAST] =
      '{ ShaderASTSample.sample } // TODO: How do I make a real instance?
  }

  given ToExpr[Boolean] with {
    def apply(x: Boolean)(using Quotes) =
      if x then '{ true }
      else '{ false }
  }

  final case class FragmentProgram(statements: List[ShaderAST]) extends ShaderAST

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
