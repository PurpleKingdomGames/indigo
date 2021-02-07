package indigo.shared.shader

import indigo.shaders.ShaderLibrary

object StandardShaders {

  val Basic: ShaderId =
    ShaderId("[indigo_engine_basic]")

  val BasicShader: CustomShader.Source =
    CustomShader.Source(
      id = Basic,
      vertex = ShaderLibrary.BasicVertex,
      fragment = ShaderLibrary.BasicFragment,
      light = ShaderLibrary.BasicLight
    )

}
