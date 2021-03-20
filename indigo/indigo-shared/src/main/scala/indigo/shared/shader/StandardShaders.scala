package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def all: Set[Shader] =
    Set(
      Bitmap,
      ImageEffects,
      NormalMinusBlue,
      ShapeBox,
      ShapeCircle,
      ShapeLine,
      ShapePolygon,
      NormalBlend,
      LightingBlend,
      BlendEffects,
      RefractionBlend
    )

  // Entity Shaders

  val Bitmap: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_blit]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlitFragment,
      light = ShaderLibrary.NoOpLight
    )

  val ImageEffects: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ImageEffectsFragment,
      light = ShaderLibrary.NoOpLight
    )

  val NormalMinusBlue: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_normal_minus_blue]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.NormalMinusBlueFragment,
      light = ShaderLibrary.NoOpLight
    )

  val ShapeBox: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_box]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeBoxFragment,
      light = ShaderLibrary.NoOpLight
    )

  val ShapeCircle: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_circle]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeCircleFragment,
      light = ShaderLibrary.NoOpLight
    )

  val ShapeLine: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_line]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapeLineFragment,
      light = ShaderLibrary.NoOpLight
    )

  val ShapePolygon: EntityShader.Source =
    EntityShader.Source(
      id = ShaderId("[indigo_engine_shape_polygon]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ShapePolygonFragment,
      light = ShaderLibrary.NoOpLight
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

  val RefractionBlend: BlendShader.Source =
    BlendShader.Source(
      id = ShaderId("[indigo_engine_blend_refraction]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.RefractionBlendFragment
    )
}
