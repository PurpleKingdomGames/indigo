package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  def shaderList: List[CustomShader.Source] =
    List(
      BlitShader
    )

  val Blit: ShaderId =
    ShaderId("[indigo_engine_blit]")

  private val BlitShader: CustomShader.Source =
    CustomShader.Source(
      id = Blit,
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlitFragment,
      light = ShaderLibrary.NoOpLight
    )

}
