package indigo.shared.materials

trait Material {
  def toShaderData: ShaderData
}
