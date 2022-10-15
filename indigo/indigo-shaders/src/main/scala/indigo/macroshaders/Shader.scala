package indigo.macroshaders

import ShaderDSL.*

final case class Shader[VertEnv, FragEnv](
    id: ShaderId,
    vertex: ShaderContext[VertEnv, Program[vec4]],
    fragment: ShaderContext[FragEnv, Program[rgba]]
)
