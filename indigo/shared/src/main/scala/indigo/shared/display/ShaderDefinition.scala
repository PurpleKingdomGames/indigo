package indigo.shared.display

final case class ShaderDefinition(
    name: String,
    webgl1: ShaderComponents,
    webgl2: ShaderComponents
)

final case class ShaderComponents(
    uniformValues: List[ShaderUniform],
    varyingValues: List[ShaderVarying],
    vertex: String,
    fragment: String
)
final case class ShaderVarying(name: String)
final case class ShaderUniform(name: String)
