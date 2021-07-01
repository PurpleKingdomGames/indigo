package indigo.shared.shader

final case class UniformBlock(blockName: String, uniforms: List[(Uniform, ShaderPrimitive)]) derives CanEqual:

  lazy val uniformHash: String =
    uniforms.toList.map(p => p._1.toString + p._2.hashCode().toString).mkString

  def withUniformBlockName(newBlockName: String): UniformBlock =
    this.copy(blockName = newBlockName)

  def withUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): UniformBlock =
    this.copy(uniforms = newUniforms)
  def withUniforms(newUniforms: (Uniform, ShaderPrimitive)*): UniformBlock =
    withUniforms(newUniforms.toList)

  def addUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): UniformBlock =
    this.copy(uniforms = uniforms ++ newUniforms)
  def addUniforms(newUniforms: (Uniform, ShaderPrimitive)*): UniformBlock =
    addUniforms(newUniforms.toList)

opaque type Uniform = String
object Uniform:
  def apply(name: String): Uniform = name
