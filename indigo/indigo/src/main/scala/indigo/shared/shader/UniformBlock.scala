package indigo.shared.shader

import indigo.shared.collections.Batch

import scala.annotation.targetName

final case class UniformBlock(blockName: UniformBlockName, uniforms: Batch[(Uniform, ShaderPrimitive)])
    derives CanEqual:

  lazy val uniformHash: String =
    blockName.toString + uniforms.map(_._2.hash).mkString

  def withUniformBlockName(newBlockName: UniformBlockName): UniformBlock =
    this.copy(blockName = newBlockName)

  def withUniforms(newUniforms: Batch[(Uniform, ShaderPrimitive)]): UniformBlock =
    this.copy(uniforms = newUniforms)
  def withUniforms(newUniforms: (Uniform, ShaderPrimitive)*): UniformBlock =
    withUniforms(Batch.fromSeq(newUniforms))

  def addUniforms(newUniforms: Batch[(Uniform, ShaderPrimitive)]): UniformBlock =
    this.copy(uniforms = uniforms ++ newUniforms)
  def addUniforms(newUniforms: (Uniform, ShaderPrimitive)*): UniformBlock =
    addUniforms(Batch.fromSeq(newUniforms))

object UniformBlock:

  def apply(blockName: UniformBlockName, uniforms: (Uniform, ShaderPrimitive)*): UniformBlock =
    UniformBlock(blockName, Batch.fromSeq(uniforms))

  @targetName("UniformBlock_ValueOnly_apply")
  def apply(blockName: UniformBlockName, uniformValues: ShaderPrimitive*): UniformBlock =
    UniformBlock(blockName, Batch.fromSeq(uniformValues).map(v => Uniform("") -> v))

opaque type UniformBlockName = String
object UniformBlockName:
  inline def apply(name: String): UniformBlockName = name

  extension (ubn: UniformBlockName) def toString: String = ubn

opaque type Uniform = String
object Uniform:
  inline def apply(name: String): Uniform = name
