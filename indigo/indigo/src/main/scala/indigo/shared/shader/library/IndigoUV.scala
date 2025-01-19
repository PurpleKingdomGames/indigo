package indigo.shared.shader.library

import ultraviolet.datatypes.ShaderAST
import ultraviolet.datatypes.ShaderPrinter
import ultraviolet.datatypes.ShaderValid
import ultraviolet.syntax.*

object IndigoUV:

  private val PI: Float    = 3.141592653589793f
  private val PI_2: Float  = PI * 0.5f
  private val PI_4: Float  = PI * 0.25f
  private val TAU: Float   = 2.0f * PI
  private val TAU_2: Float = PI
  private val TAU_4: Float = PI_2
  private val TAU_8: Float = PI_4

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final case class VertexEnv(
      TIME: Float,         // Running time
      VIEWPORT_SIZE: vec2, // Size of the viewport in pixels

      // Variables
      ATLAS_SIZE: vec2,
      var VERTEX: vec4,
      TEXTURE_SIZE: vec2,
      var UV: vec2,
      SIZE: vec2,
      FRAME_SIZE: vec2,
      CHANNEL_0_ATLAS_OFFSET: vec2,
      CHANNEL_1_ATLAS_OFFSET: vec2,
      CHANNEL_2_ATLAS_OFFSET: vec2,
      CHANNEL_3_ATLAS_OFFSET: vec2,
      CHANNEL_0_TEXTURE_COORDS: vec2,
      CHANNEL_1_TEXTURE_COORDS: vec2,
      CHANNEL_2_TEXTURE_COORDS: vec2,
      CHANNEL_3_TEXTURE_COORDS: vec2,
      CHANNEL_0_POSITION: vec2,
      CHANNEL_1_POSITION: vec2,
      CHANNEL_2_POSITION: vec2,
      CHANNEL_3_POSITION: vec2,
      CHANNEL_0_SIZE: vec2,
      POSITION: vec2,
      SCALE: vec2,
      REF: vec2,
      FLIP: vec2,
      ROTATION: Float,
      TEXTURE_COORDS: vec2, // Redundant, equal to UV
      INSTANCE_ID: Int,

      // Constants
      PI: Float,
      PI_2: Float,
      PI_4: Float,
      TAU: Float,
      TAU_2: Float,
      TAU_4: Float,
      TAU_8: Float
  )
  object VertexEnv:
    def reference: VertexEnv =
      VertexEnv(
        TIME = 0.0f,
        VIEWPORT_SIZE = vec2(0.0f),
        ATLAS_SIZE = vec2(0.0f),
        VERTEX = vec4(0.0f),
        TEXTURE_SIZE = vec2(0.0f),
        UV = vec2(0.0f),
        SIZE = vec2(0.0f),
        FRAME_SIZE = vec2(0.0f),
        CHANNEL_0_ATLAS_OFFSET = vec2(0.0f),
        CHANNEL_1_ATLAS_OFFSET = vec2(0.0f),
        CHANNEL_2_ATLAS_OFFSET = vec2(0.0f),
        CHANNEL_3_ATLAS_OFFSET = vec2(0.0f),
        CHANNEL_0_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_1_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_2_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_3_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_0_POSITION = vec2(0.0f),
        CHANNEL_1_POSITION = vec2(0.0f),
        CHANNEL_2_POSITION = vec2(0.0f),
        CHANNEL_3_POSITION = vec2(0.0f),
        CHANNEL_0_SIZE = vec2(0.0f),
        POSITION = vec2(0.0f),
        SCALE = vec2(0.0f),
        REF = vec2(0.0f),
        FLIP = vec2(0.0f),
        ROTATION = 0.0f,
        TEXTURE_COORDS = vec2(0.0f),
        INSTANCE_ID = 0,
        PI = PI,
        PI_2 = PI_2,
        PI_4 = PI_4,
        TAU = TAU,
        TAU_2 = TAU_2,
        TAU_4 = TAU_4,
        TAU_8 = TAU_8
      )
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  trait VertexEnvReference:
    val TIME: Float                    = 0.0f
    val VIEWPORT_SIZE: vec2            = vec2(0.0f)
    val ATLAS_SIZE: vec2               = vec2(0.0f)
    var VERTEX: vec4                   = vec4(0.0f)
    val TEXTURE_SIZE: vec2             = vec2(0.0f)
    var UV: vec2                       = vec2(0.0f)
    val SIZE: vec2                     = vec2(0.0f)
    val FRAME_SIZE: vec2               = vec2(0.0f)
    val CHANNEL_0_ATLAS_OFFSET: vec2   = vec2(0.0f)
    val CHANNEL_1_ATLAS_OFFSET: vec2   = vec2(0.0f)
    val CHANNEL_2_ATLAS_OFFSET: vec2   = vec2(0.0f)
    val CHANNEL_3_ATLAS_OFFSET: vec2   = vec2(0.0f)
    val CHANNEL_0_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_1_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_2_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_3_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_0_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_1_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_2_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_3_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_0_SIZE: vec2           = vec2(0.0f)
    val POSITION: vec2                 = vec2(0.0f)
    val SCALE: vec2                    = vec2(0.0f)
    val REF: vec2                      = vec2(0.0f)
    val FLIP: vec2                     = vec2(0.0f)
    val ROTATION: Float                = 0.0f
    val TEXTURE_COORDS: vec2           = vec2(0.0f)
    val INSTANCE_ID: Int               = 0
    val PI: Float                      = 0.0f
    val PI_2: Float                    = 0.0f
    val PI_4: Float                    = 0.0f
    val TAU: Float                     = 0.0f
    val TAU_2: Float                   = 0.0f
    val TAU_4: Float                   = 0.0f
    val TAU_8: Float                   = 0.0f

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final case class FragmentEnv(
      SRC_CHANNEL: sampler2D.type,
      TIME: Float,         // Running time
      VIEWPORT_SIZE: vec2, // Size of the viewport in pixels

      // Variables
      UV: vec2,                       // Unscaled texture coordinates
      SIZE: vec2,                     // Width / height of the objects
      var CHANNEL_0: vec4,            // Pixel value from texture channel 0
      var CHANNEL_1: vec4,            // Pixel value from texture channel 1
      var CHANNEL_2: vec4,            // Pixel value from texture channel 2
      var CHANNEL_3: vec4,            // Pixel value from texture channel 3
      CHANNEL_0_TEXTURE_COORDS: vec2, // Scaled texture coordinates
      CHANNEL_1_TEXTURE_COORDS: vec2, // Scaled texture coordinates
      CHANNEL_2_TEXTURE_COORDS: vec2, // Scaled texture coordinates
      CHANNEL_3_TEXTURE_COORDS: vec2, // Scaled texture coordinates
      CHANNEL_0_POSITION: vec2,       // top left position of this texture on the atlas in UV coords
      CHANNEL_1_POSITION: vec2,       // top left position of this texture on the atlas in UV coords
      CHANNEL_2_POSITION: vec2,       // top left position of this texture on the atlas in UV coords
      CHANNEL_3_POSITION: vec2,       // top left position of this texture on the atlas in UV coords
      CHANNEL_0_SIZE: vec2,           // size of this texture on the atlas in UV coords
      SCREEN_COORDS: vec2,
      ROTATION: Float,
      TEXTURE_SIZE: vec2, // Size of the texture in pixels
      ATLAS_SIZE: vec2,   // Size of the atlas this texture is on, in pixels
      INSTANCE_ID: Int,   // The current instance id
      var COLOR: vec4,    // The fragment color accumulated so far

      // Light information
      LIGHT_INDEX: Int,
      LIGHT_COUNT: Int,
      LIGHT_ACTIVE: Int,
      LIGHT_TYPE: Int,
      LIGHT_FAR_CUT_OFF: Int,
      LIGHT_FALLOFF_TYPE: Int,
      LIGHT_COLOR: vec4,
      LIGHT_SPECULAR: vec4,
      LIGHT_POSITION: vec2,
      LIGHT_ROTATION: Float,
      LIGHT_NEAR: Float,
      LIGHT_FAR: Float,
      LIGHT_ANGLE: Float,
      LIGHT_INTENSITY: Float,

      // Constants
      PI: Float,
      PI_2: Float,
      PI_4: Float,
      TAU: Float,
      TAU_2: Float,
      TAU_4: Float,
      TAU_8: Float
  )
  object FragmentEnv:
    def reference: FragmentEnv =
      FragmentEnv(
        SRC_CHANNEL = sampler2D,
        TIME = 0.0f,
        VIEWPORT_SIZE = vec2(0.0f),
        UV = vec2(0.0f),
        SIZE = vec2(0.0f),
        CHANNEL_0 = vec4(0.0f),
        CHANNEL_1 = vec4(0.0f),
        CHANNEL_2 = vec4(0.0f),
        CHANNEL_3 = vec4(0.0f),
        CHANNEL_0_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_1_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_2_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_3_TEXTURE_COORDS = vec2(0.0f),
        CHANNEL_0_POSITION = vec2(0.0f),
        CHANNEL_1_POSITION = vec2(0.0f),
        CHANNEL_2_POSITION = vec2(0.0f),
        CHANNEL_3_POSITION = vec2(0.0f),
        CHANNEL_0_SIZE = vec2(0.0f),
        SCREEN_COORDS = vec2(0.0f),
        ROTATION = 0.0f,
        TEXTURE_SIZE = vec2(0.0f),
        ATLAS_SIZE = vec2(0.0f),
        INSTANCE_ID = 0,
        COLOR = vec4(0.0f),
        LIGHT_INDEX = 0,
        LIGHT_COUNT = 0,
        LIGHT_ACTIVE = 0,
        LIGHT_TYPE = 0,
        LIGHT_FAR_CUT_OFF = 0,
        LIGHT_FALLOFF_TYPE = 0,
        LIGHT_COLOR = vec4(0.0f),
        LIGHT_SPECULAR = vec4(0.0f),
        LIGHT_POSITION = vec2(0.0f),
        LIGHT_ROTATION = 0.0f,
        LIGHT_NEAR = 0.0f,
        LIGHT_FAR = 0.0f,
        LIGHT_ANGLE = 0.0f,
        LIGHT_INTENSITY = 0.0f,
        PI = PI,
        PI_2 = PI_2,
        PI_4 = PI_4,
        TAU = TAU,
        TAU_2 = TAU_2,
        TAU_4 = TAU_4,
        TAU_8 = TAU_8
      )
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  trait FragmentEnvReference:
    val SRC_CHANNEL: sampler2D.type    = sampler2D
    val TIME: Float                    = 0.0f
    val VIEWPORT_SIZE: vec2            = vec2(0.0f)
    val UV: vec2                       = vec2(0.0f)
    val SIZE: vec2                     = vec2(0.0f)
    var CHANNEL_0: vec4                = vec4(0.0f)
    var CHANNEL_1: vec4                = vec4(0.0f)
    var CHANNEL_2: vec4                = vec4(0.0f)
    var CHANNEL_3: vec4                = vec4(0.0f)
    val CHANNEL_0_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_1_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_2_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_3_TEXTURE_COORDS: vec2 = vec2(0.0f)
    val CHANNEL_0_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_1_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_2_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_3_POSITION: vec2       = vec2(0.0f)
    val CHANNEL_0_SIZE: vec2           = vec2(0.0f)
    val SCREEN_COORDS: vec2            = vec2(0.0f)
    val ROTATION: Float                = 0.0f
    val TEXTURE_SIZE: vec2             = vec2(0.0f)
    val ATLAS_SIZE: vec2               = vec2(0.0f)
    val INSTANCE_ID: Int               = 0
    var COLOR: vec4                    = vec4(0.0f)
    val LIGHT_INDEX: Int               = 0
    val LIGHT_COUNT: Int               = 0
    val LIGHT_ACTIVE: Int              = 0
    val LIGHT_TYPE: Int                = 0
    val LIGHT_FAR_CUT_OFF: Int         = 0
    val LIGHT_FALLOFF_TYPE: Int        = 0
    val LIGHT_COLOR: vec4              = vec4(0.0f)
    val LIGHT_SPECULAR: vec4           = vec4(0.0f)
    val LIGHT_POSITION: vec2           = vec2(0.0f)
    val LIGHT_ROTATION: Float          = 0.0f
    val LIGHT_NEAR: Float              = 0.0f
    val LIGHT_FAR: Float               = 0.0f
    val LIGHT_ANGLE: Float             = 0.0f
    val LIGHT_INTENSITY: Float         = 0.0f
    val PI: Float                      = 0.0f
    val PI_2: Float                    = 0.0f
    val PI_4: Float                    = 0.0f
    val TAU: Float                     = 0.0f
    val TAU_2: Float                   = 0.0f
    val TAU_4: Float                   = 0.0f
    val TAU_8: Float                   = 0.0f

  final case class BlendFragmentEnv(
      SRC_CHANNEL: sampler2D.type,
      DST_CHANNEL: sampler2D.type,
      TIME: Float,         // Running time
      VIEWPORT_SIZE: vec2, // Size of the viewport in pixels

      // Variables
      UV: vec2,   // Unscaled texture coordinates
      SIZE: vec2, // Width / height of the objects
      SRC: vec4,  // Pixel value from SRC texture
      DST: vec4,  // Pixel value from DST texture

      // Constants
      PI: Float,
      PI_2: Float,
      PI_4: Float,
      TAU: Float,
      TAU_2: Float,
      TAU_4: Float,
      TAU_8: Float
  )
  object BlendFragmentEnv:
    def reference: BlendFragmentEnv =
      BlendFragmentEnv(
        SRC_CHANNEL = sampler2D,
        DST_CHANNEL = sampler2D,
        TIME = 0.0f,
        VIEWPORT_SIZE = vec2(0.0f),
        UV = vec2(0.0f),
        SIZE = vec2(0.0f),
        SRC = vec4(0.0f),
        DST = vec4(0.0f),
        PI = PI,
        PI_2 = PI_2,
        PI_4 = PI_4,
        TAU = TAU,
        TAU_2 = TAU_2,
        TAU_4 = TAU_4,
        TAU_8 = TAU_8
      )
  trait BlendFragmentEnvReference:
    val SRC_CHANNEL: sampler2D.type = sampler2D
    val DST_CHANNEL: sampler2D.type = sampler2D
    val TIME: Float                 = 0.0f
    val VIEWPORT_SIZE: vec2         = vec2(0.0f)
    val UV: vec2                    = vec2(0.0f)
    val SIZE: vec2                  = vec2(0.0f)
    val SRC: vec4                   = vec4(0.0f)
    val DST: vec4                   = vec4(0.0f)
    val PI: Float                   = 0.0f
    val PI_2: Float                 = 0.0f
    val PI_4: Float                 = 0.0f
    val TAU: Float                  = 0.0f
    val TAU_2: Float                = 0.0f
    val TAU_4: Float                = 0.0f
    val TAU_8: Float                = 0.0f

  sealed trait IndigoVertexPrinter
  sealed trait IndigoFragmentPrinter
  sealed trait IndigoBlendFragmentPrinter

  given ShaderPrinter[IndigoVertexPrinter] = new ShaderPrinter {
    val webGL2Printer = summon[ShaderPrinter[WebGL2]]

    def isValid(
        inType: Option[String],
        outType: Option[String],
        functions: List[ShaderAST],
        body: ShaderAST
    ): ShaderValid =
      val hasVertexFunction: ShaderValid =
        body.find {
          case ShaderAST.Function(
                "vertex",
                List(
                  ShaderAST.DataTypes.ident("vec4") -> _
                ),
                _,
                ShaderAST.DataTypes.ident("vec4")
              ) =>
            true

          case _ => false
        } match
          case Some(_) =>
            ShaderValid.Valid

          case None =>
            ShaderValid.Invalid(
              List(
                "Indigo vertex shaders must declare a 'vertex' function, e.g. `def vertex(v: vec4): vec4 = v`"
              )
            )

      webGL2Printer.isValid(inType, outType, functions, body) |+|
        hasVertexFunction

    def transformer: PartialFunction[ShaderAST, ShaderAST] =
      webGL2Printer.transformer

    def printer: PartialFunction[ShaderAST, List[String]] =
      webGL2Printer.printer
  }

  given ShaderPrinter[IndigoFragmentPrinter] = new ShaderPrinter {
    val webGL2Printer = summon[ShaderPrinter[WebGL2]]

    def isValid(
        inType: Option[String],
        outType: Option[String],
        functions: List[ShaderAST],
        body: ShaderAST
    ): ShaderValid =
      def hasFragmentFunction: ShaderValid =
        body
          .find {
            case ShaderAST.Function(
                  "fragment",
                  List(
                    ShaderAST.DataTypes.ident("vec4") -> _
                  ),
                  _,
                  ShaderAST.DataTypes.ident("vec4")
                ) =>
              true

            case _ => false
          }
          .map(_ => ShaderValid.Valid)
          .getOrElse {
            ShaderValid.Invalid(
              List(
                "Indigo fragment shaders must declare a 'fragment' function, e.g. `def fragment(color: vec4): vec4 = color`"
              )
            )
          }

      def hasUnitFunction(name: String): ShaderValid =
        body
          .find {
            case ShaderAST.Function(
                  fnName,
                  Nil,
                  _,
                  ShaderAST.DataTypes.ident("void")
                ) if fnName == name =>
              true

            case _ => false
          }
          .map(_ => ShaderValid.Valid)
          .getOrElse {
            ShaderValid.Invalid(
              List(
                s"Indigo fragment shaders must declare a '$name' function, e.g. `def $name(): Unit = ()`"
              )
            )
          }

      webGL2Printer.isValid(inType, outType, functions, body) |+|
        hasFragmentFunction |+|
        hasUnitFunction("prepare") |+|
        hasUnitFunction("light") |+|
        hasUnitFunction("composite")

    def transformer: PartialFunction[ShaderAST, ShaderAST] =
      webGL2Printer.transformer

    def printer: PartialFunction[ShaderAST, List[String]] =
      webGL2Printer.printer
  }

  given ShaderPrinter[IndigoBlendFragmentPrinter] = new ShaderPrinter {
    val webGL2Printer = summon[ShaderPrinter[WebGL2]]

    def isValid(
        inType: Option[String],
        outType: Option[String],
        functions: List[ShaderAST],
        body: ShaderAST
    ): ShaderValid =
      def hasFragmentFunction: ShaderValid =
        body.find {
          case ShaderAST.Function(
                "fragment",
                List(
                  ShaderAST.DataTypes.ident("vec4") -> _
                ),
                _,
                ShaderAST.DataTypes.ident("vec4")
              ) =>
            true

          case _ => false
        } match
          case Some(_) =>
            ShaderValid.Valid

          case None =>
            ShaderValid.Invalid(
              List(
                "Indigo fragment shaders must declare a 'fragment' function, e.g. `def fragment(color: vec4): vec4 = color`"
              )
            )

      webGL2Printer.isValid(inType, outType, functions, body) |+|
        hasFragmentFunction

    def transformer: PartialFunction[ShaderAST, ShaderAST] =
      webGL2Printer.transformer

    def printer: PartialFunction[ShaderAST, List[String]] =
      webGL2Printer.printer
  }
