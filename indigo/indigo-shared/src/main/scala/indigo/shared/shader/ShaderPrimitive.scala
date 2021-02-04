package indigo.shared.shader

sealed trait ShaderPrimitive
object ShaderPrimitive {

  final case class float(value: Double) extends ShaderPrimitive

  final case class vec2(x: Double, y: Double) extends ShaderPrimitive
  final case class vec3(x: Double, y: Double, z: Double) extends ShaderPrimitive
  final case class vec4(x: Double, y: Double, z: Double, w: Double) extends ShaderPrimitive

}
