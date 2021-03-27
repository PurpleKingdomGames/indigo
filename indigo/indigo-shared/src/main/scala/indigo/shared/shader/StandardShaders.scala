package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def all: Set[Shader] =
    Set(
      Bitmap,
      LitBitmap,
      ImageEffects,
      ShapeBox,
      ShapeCircle,
      ShapeLine,
      ShapePolygon,
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
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val LitBitmap: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_lit_bitmap]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlitFragment,
      light = ShaderLibrary.BlitLight,
      composite = ShaderLibrary.BlitComposite
    )

  val ImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_image_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ImageEffectsFragment,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val ShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_box]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeBoxFragment,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val ShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_circle]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeCircleFragment,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val ShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_line]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeLineFragment,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
    )

  val ShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_polygon]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapePolygonFragment,
      light = ShaderLibrary.NoOpLight,
      composite = ShaderLibrary.NoOpComposite
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
