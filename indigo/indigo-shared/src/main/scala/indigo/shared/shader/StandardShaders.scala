package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def shaderList: List[Shader.Source] =
    List(
      Bitmap,
      ImageEffects,
      ShapeBox,
      ShapeCircle,
      ShapeLine
    )

  val Bitmap: Shader.Source =
    Shader.Source(
      id = ShaderId("[indigo_engine_blit]"),
      vertex = ShaderLibrary.NoOpVertex,
      postVertex = ShaderLibrary.NoOpPostVertex,
      fragment = ShaderLibrary.BlitFragment,
      postFragment = ShaderLibrary.NoOpPostFragment,
      light = ShaderLibrary.NoOpLight,
      postLight = ShaderLibrary.NoOpPostLight
    )

  val ImageEffects: Shader.Source =
    Shader.Source(
      id = ShaderId("[indigo_engine_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      postVertex = ShaderLibrary.NoOpPostVertex,
      fragment = ShaderLibrary.ImageEffectsFragment,
      postFragment = ShaderLibrary.NoOpPostFragment,
      light = ShaderLibrary.NoOpLight,
      postLight = ShaderLibrary.NoOpPostLight
    )

  val ShapeBox: Shader.Source =
    Shader.Source(
      id = ShaderId("[indigo_engine_shape_box]"),
      vertex = ShaderLibrary.NoOpVertex,
      postVertex = ShaderLibrary.NoOpPostVertex,
      fragment = ShaderLibrary.ShapeBoxFragment,
      postFragment = ShaderLibrary.NoOpPostFragment,
      light = ShaderLibrary.NoOpLight,
      postLight = ShaderLibrary.NoOpPostLight
    )

  val ShapeCircle: Shader.Source =
    Shader.Source(
      id = ShaderId("[indigo_engine_shape_circle]"),
      vertex = ShaderLibrary.NoOpVertex,
      postVertex = ShaderLibrary.NoOpPostVertex,
      fragment = ShaderLibrary.ShapeCircleFragment,
      postFragment = ShaderLibrary.NoOpPostFragment,
      light = ShaderLibrary.NoOpLight,
      postLight = ShaderLibrary.NoOpPostLight
    )

  val ShapeLine: Shader.Source =
    Shader.Source(
      id = ShaderId("[indigo_engine_shape_line]"),
      vertex = ShaderLibrary.NoOpVertex,
      postVertex = ShaderLibrary.NoOpPostVertex,
      fragment = ShaderLibrary.ShapeLineFragment,
      postFragment = ShaderLibrary.NoOpPostFragment,
      light = ShaderLibrary.NoOpLight,
      postLight = ShaderLibrary.NoOpPostLight
    )

}
