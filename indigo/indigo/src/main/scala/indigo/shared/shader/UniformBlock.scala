package indigo.shared.shader

import indigo.shared.collections.Batch

final case class UniformBlock(blockName: String, uniforms: Batch[(Uniform, ShaderPrimitive)]) derives CanEqual:

  lazy val uniformHash: String =
    uniforms.toList.map(p => p._1.toString + p._2.hash).mkString

  def withUniformBlockName(newBlockName: String): UniformBlock =
    this.copy(blockName = newBlockName)

  def withUniforms(newUniforms: Batch[(Uniform, ShaderPrimitive)]): UniformBlock =
    this.copy(uniforms = newUniforms)
  def withUniforms(newUniforms: (Uniform, ShaderPrimitive)*): UniformBlock =
    withUniforms(Batch.fromSeq(newUniforms))

  def addUniforms(newUniforms: Batch[(Uniform, ShaderPrimitive)]): UniformBlock =
    this.copy(uniforms = uniforms ++ newUniforms)
  def addUniforms(newUniforms: (Uniform, ShaderPrimitive)*): UniformBlock =
    addUniforms(Batch.fromSeq(newUniforms))

opaque type Uniform = String
object Uniform:
  inline def apply(name: String): Uniform = name
