package indigo.macroshaders

import ShaderDSL.*

final case class ShaderGroup[VertEnv, FragEnv](
    id: ShaderId,
    vertex: Shader[VertEnv, vec4],
    fragment: Shader[FragEnv, rgba]
)
