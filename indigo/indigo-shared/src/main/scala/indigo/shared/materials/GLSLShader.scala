package indigo.shared.materials

import indigo.shared.assets.AssetName
import indigo.shared.shader.ShaderId
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive

final case class GLSLShader(
    shaderId: ShaderId,
    uniforms: List[(Uniform, ShaderPrimitive)],
    channel0: Option[AssetName],
    channel1: Option[AssetName],
    channel2: Option[AssetName],
    channel3: Option[AssetName]
) extends Material {

  def withShaderId(newShaderId: ShaderId): GLSLShader =
    this.copy(shaderId = newShaderId)

  def uniformHash: String =
    uniforms.toList.map(p => p._1.name + p._2.hash).mkString

  def withUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): GLSLShader =
    this.copy(uniforms = newUniforms)
  def withUniforms(newUniforms: (Uniform, ShaderPrimitive)*): GLSLShader =
    withUniforms(newUniforms.toList)

  def addUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): GLSLShader =
    this.copy(uniforms = uniforms ++ newUniforms)
  def addUniforms(newUniforms: (Uniform, ShaderPrimitive)*): GLSLShader =
    addUniforms(newUniforms.toList)

  def withChannel0(assetName: AssetName): GLSLShader =
    this.copy(channel0 = Some(assetName))
  def withChannel1(assetName: AssetName): GLSLShader =
    this.copy(channel1 = Some(assetName))
  def withChannel2(assetName: AssetName): GLSLShader =
    this.copy(channel2 = Some(assetName))
  def withChannel3(assetName: AssetName): GLSLShader =
    this.copy(channel3 = Some(assetName))

  lazy val hash: String =
    s"custom-${shaderId.value}" +
      s"-${uniformHash}" +
      s"-${channel0.map(_.value).getOrElse("")}" +
      s"-${channel1.map(_.value).getOrElse("")}" +
      s"-${channel2.map(_.value).getOrElse("")}" +
      s"-${channel3.map(_.value).getOrElse("")}"

  def toGLSLShader: GLSLShader =
    this

}
object GLSLShader {

  def apply(shaderId: ShaderId): GLSLShader =
    GLSLShader(shaderId, Nil, None, None, None, None)

  def apply(shaderId: ShaderId, uniforms: List[(Uniform, ShaderPrimitive)]): GLSLShader =
    GLSLShader(shaderId, uniforms, None, None, None, None)

  def apply(shaderId: ShaderId, channel0: AssetName, channel1: AssetName, channel2: AssetName, channel3: AssetName): GLSLShader =
    GLSLShader(shaderId, Nil, Option(channel0), Option(channel1), Option(channel2), Option(channel3))

}
