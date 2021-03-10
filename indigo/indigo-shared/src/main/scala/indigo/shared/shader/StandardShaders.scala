package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def shaderList: List[EntityShader.Source] =
    List(
      Bitmap,
      ImageEffects,
      ShapeBox,
      ShapeCircle,
      ShapeLine,
      ShapePolygon
    )

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

}
