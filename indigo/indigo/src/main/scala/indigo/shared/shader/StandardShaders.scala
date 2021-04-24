package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def all: Set[Shader] =
    Set(
      Bitmap,
      LitBitmap,
      ImageEffects,
      LitImageEffects,
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
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlitFragment,
      prepare = ShaderLibrary.NoOpPrepare,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val LitBitmap: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_bitmap]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlitFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_image_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ImageEffectsFragment,
      prepare = ShaderLibrary.NoOpPrepare,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val LitImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_image_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ImageEffectsFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_box]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeBoxFragment,
      prepare = ShaderLibrary.NoOpPrepare,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val LitShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_box]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeBoxFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_circle]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeCircleFragment,
      prepare = ShaderLibrary.NoOpPrepare,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val LitShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_circle]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeCircleFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_line]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeLineFragment,
      prepare = ShaderLibrary.NoOpPrepare,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val LitShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_line]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeLineFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  val ShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_polygon]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapePolygonFragment,
      prepare = ShaderLibrary.NoOpPrepare,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val LitShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_shape_polygon]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapePolygonFragment,
      prepare = ShaderLibrary.LightingPrepare,
      light = ShaderLibrary.LightingLight,
      composite = ShaderLibrary.LightingComposite
    )

  // Blend Shaders

  val NormalBlend: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_normal]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.NormalBlendFragment
    )

  val LightingBlend: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_lighting]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.LightingBlendFragment
    )

  val BlendEffects: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlendEffectsFragment
    )

}
