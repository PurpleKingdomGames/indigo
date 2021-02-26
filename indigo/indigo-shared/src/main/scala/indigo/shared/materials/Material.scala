package indigo.shared.materials

trait Material {
  def hash: String
  def toShaderData: ShaderData
}
