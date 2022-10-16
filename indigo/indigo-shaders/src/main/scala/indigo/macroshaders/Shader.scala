package indigo.macroshaders

import ShaderDSL.*

final case class Shader[VertEnv, FragEnv](
    id: ShaderId,
    vertex: ShaderContext[VertEnv, vec4],
    fragment: ShaderContext[FragEnv, rgba]
)
