package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def shaderList: List[CustomShader.Source] =
    List(
      Blit,
      ImageEffects
    )

  val Blit: CustomShader.Source =
    CustomShader.Source(
      id = ShaderId("[indigo_engine_blit]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlitFragment,
      light = ShaderLibrary.NoOpLight
    )

  val ImageEffects: CustomShader.Source =
    CustomShader.Source(
      id = ShaderId("[indigo_engine_effects]"),
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.ImageEffectsFragment,
      light = ShaderLibrary.NoOpLight
    )

}
