package indigo.shared.shader

import indigo.shaders.ShaderLibrary
import indigo.shared.shader.library.Blit
import indigo.shared.shader.library.NoOp

object StandardShaders {

  def all: Set[Shader] =
    Set(
      Bitmap,
      LitBitmap,
      ImageEffects,
      LitImageEffects,
      BitmapClip,
      LitBitmapClip,
      ImageEffectsClip,
      LitImageEffectsClip,
      ShapeBox,
      LitShapeBox,
      ShapeCircle,
      LitShapeCircle,
      ShapeLine,
      LitShapeLine,
      ShapePolygon,
      LitShapePolygon,
      NormalBlend,
      LightingBlend,
      BlendEffects
    )

  // Entity Shaders

  val Bitmap: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_bitmap]"),
      vertex = NoOp.vertex.output.code,
      fragment = Blit.fragment.output.code,
      prepare = NoOp.prepare.output.code,
      light = NoOp.light.output.code,
      composite = NoOp.composite.output.code
    )

  val LitBitmap: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_bitmap]"),
      vertex = NoOp.vertex.output.code,
      fragment = Blit.fragment.output.code,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_image_effects]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ImageEffectsFragment,
      prepare = NoOp.prepare.output.code,
      light = NoOp.light.output.code,
      composite = NoOp.composite.output.code
    )

  val LitImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_image_effects]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ImageEffectsFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  // Clips

  def shaderIdToClipShaderId(id: ShaderId): ShaderId =
    ShaderId(id.toString + "[clip]")

  def makeClipShader(shader: EntityShader.Source): EntityShader.Source =
    shader.copy(
      id = shaderIdToClipShaderId(shader.id),
      vertex = ShaderLibrary.ClipVertex
    )

  val BitmapClip: EntityShader.Source          = makeClipShader(Bitmap)
  val LitBitmapClip: EntityShader.Source       = makeClipShader(LitBitmap)
  val ImageEffectsClip: EntityShader.Source    = makeClipShader(ImageEffects)
  val LitImageEffectsClip: EntityShader.Source = makeClipShader(LitImageEffects)

  // Shapes

  val ShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_box]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapeBoxFragment,
      prepare = NoOp.prepare.output.code,
      light = NoOp.light.output.code,
      composite = NoOp.composite.output.code
    )

  val LitShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_box]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapeBoxFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_circle]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapeCircleFragment,
      prepare = NoOp.prepare.output.code,
      light = NoOp.light.output.code,
      composite = NoOp.composite.output.code
    )

  val LitShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_circle]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapeCircleFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_line]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapeLineFragment,
      prepare = NoOp.prepare.output.code,
      light = NoOp.light.output.code,
      composite = NoOp.composite.output.code
    )

  val LitShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_line]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapeLineFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_polygon]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapePolygonFragment,
      prepare = NoOp.prepare.output.code,
      light = NoOp.light.output.code,
      composite = NoOp.composite.output.code
    )

  val LitShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_polygon]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.ShapePolygonFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  // Blend Shaders

  val NormalBlend: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_normal]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.NormalBlendFragment
    )

  val LightingBlend: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_lighting]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.LightingBlendFragment
    )

  val BlendEffects: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_effects]"),
      vertex = NoOp.vertex.output.code,
      fragment = ShaderLibrary.BlendEffectsFragment
    )

}
