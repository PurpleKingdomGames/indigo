package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def shaderList: List[CustomShader.Source] =
    List(
      Bitmap,
      ImageEffects
    )

  val Bitmap: CustomShader.Source =
    CustomShader.Source(
      id = ShaderId("[indigo_engine_blit]"),
      vertex = ShaderLibrary.NoOpVertex,
      postVertex = ShaderLibrary.NoOpPostVertex,
      fragment = ShaderLibrary.BlitFragment,
      postFragment = ShaderLibrary.NoOpPostFragment,
      light = ShaderLibrary.NoOpLight,
      postLight = ShaderLibrary.NoOpPostLight
    )

  val ImageEffects: CustomShader.Source =
    CustomShader.Source(
      id = ShaderId("[indigo_engine_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      postVertex = ShaderLibrary.NoOpPostVertex,
      fragment = ShaderLibrary.ImageEffectsFragment,
      postFragment = ShaderLibrary.NoOpPostFragment,
      light = ShaderLibrary.NoOpLight,
      postLight = ShaderLibrary.NoOpPostLight
    )

}
