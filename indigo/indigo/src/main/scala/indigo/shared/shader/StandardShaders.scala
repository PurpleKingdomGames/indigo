package indigo.shared.shader

import indigo.shared.shader.library
import indigo.shared.shader.library.Blit
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
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.Blit.fragment.output.toOutput.code,
      prepare = library.NoOp.prepare.output.toOutput.code,
      light = library.NoOp.light.output.toOutput.code,
      composite = library.NoOp.composite.output.toOutput.code
    )

  val LitBitmap: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_bitmap]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.Blit.fragment.output.toOutput.code,
      prepare = library.Lighting.prepare.output.toOutput.code,
      light = library.Lighting.light.output.toOutput.code,
      composite = library.Lighting.composite.output.toOutput.code
    )

  val ImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_image_effects]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ImageEffects.fragment.output.toOutput.code,
      prepare = library.NoOp.prepare.output.toOutput.code,
      light = library.NoOp.light.output.toOutput.code,
      composite = library.NoOp.composite.output.toOutput.code
    )

  val LitImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_image_effects]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ImageEffects.fragment.output.toOutput.code,
      prepare = library.Lighting.prepare.output.toOutput.code,
      light = library.Lighting.light.output.toOutput.code,
      composite = library.Lighting.composite.output.toOutput.code
    )

  // Clips

  def shaderIdToClipShaderId(id: ShaderId): ShaderId =
    ShaderId(id.toString + "[clip]")

  def makeClipShader(shader: EntityShader.Source): EntityShader.Source =
    shader.copy(
      id = shaderIdToClipShaderId(shader.id),
      vertex = library.Clip.vertex.output.toOutput.code
    )

  val BitmapClip: EntityShader.Source          = makeClipShader(Bitmap)
  val LitBitmapClip: EntityShader.Source       = makeClipShader(LitBitmap)
  val ImageEffectsClip: EntityShader.Source    = makeClipShader(ImageEffects)
  val LitImageEffectsClip: EntityShader.Source = makeClipShader(LitImageEffects)

  // Shapes

  val ShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_box]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapeBox.fragment.output.toOutput.code,
      prepare = library.NoOp.prepare.output.toOutput.code,
      light = library.NoOp.light.output.toOutput.code,
      composite = library.NoOp.composite.output.toOutput.code
    )

  val LitShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_box]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapeBox.fragment.output.toOutput.code,
      prepare = library.Lighting.prepare.output.toOutput.code,
      light = library.Lighting.light.output.toOutput.code,
      composite = library.Lighting.composite.output.toOutput.code
    )

  val ShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_circle]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapeCircle.fragment.output.toOutput.code,
      prepare = library.NoOp.prepare.output.toOutput.code,
      light = library.NoOp.light.output.toOutput.code,
      composite = library.NoOp.composite.output.toOutput.code
    )

  val LitShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_circle]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapeCircle.fragment.output.toOutput.code,
      prepare = library.Lighting.prepare.output.toOutput.code,
      light = library.Lighting.light.output.toOutput.code,
      composite = library.Lighting.composite.output.toOutput.code
    )

  val ShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_line]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapeLine.fragment.output.toOutput.code,
      prepare = library.NoOp.prepare.output.toOutput.code,
      light = library.NoOp.light.output.toOutput.code,
      composite = library.NoOp.composite.output.toOutput.code
    )

  val LitShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_line]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapeLine.fragment.output.toOutput.code,
      prepare = library.Lighting.prepare.output.toOutput.code,
      light = library.Lighting.light.output.toOutput.code,
      composite = library.Lighting.composite.output.toOutput.code
    )

  val ShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_polygon]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapePolygon.fragment.output.toOutput.code,
      prepare = library.NoOp.prepare.output.toOutput.code,
      light = library.NoOp.light.output.toOutput.code,
      composite = library.NoOp.composite.output.toOutput.code
    )

  val LitShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_polygon]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.ShapePolygon.fragment.output.toOutput.code,
      prepare = library.Lighting.prepare.output.toOutput.code,
      light = library.Lighting.light.output.toOutput.code,
      composite = library.Lighting.composite.output.toOutput.code
    )

  // Blend Shaders

  val NormalBlend: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_normal]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.NormalBlend.fragment.output.toOutput.code
    )

  val LightingBlend: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_lighting]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.LightingBlend.fragment.output.toOutput.code
    )

  val BlendEffects: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_effects]"),
      vertex = library.NoOp.vertex.output.toOutput.code,
      fragment = library.BlendEffects.fragment.output.toOutput.code
    )

}
