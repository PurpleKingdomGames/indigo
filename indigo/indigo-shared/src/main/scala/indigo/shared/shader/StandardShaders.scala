package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  val Blit: ShaderId =
    ShaderId("[indigo_engine_blit]")

  val BlitShader: CustomShader.Source =
    CustomShader.Source(
      id = Blit,
      vertex = ShaderLibrary.NoOpVertex,
      fragment = ShaderLibrary.BlitFragment,
      light = ShaderLibrary.NoOpLight
    )

}
