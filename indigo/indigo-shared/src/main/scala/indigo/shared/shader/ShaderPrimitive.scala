package indigo.shared.shader

sealed trait ShaderPrimitive {
  def hash: String
  def toArray: Array[Float]
}
object ShaderPrimitive {

  final case class float(value: Double) extends ShaderPrimitive {
    def hash: String          = s"float${value.toString()}"
    def toArray: Array[Float] = Array(value.toFloat, 0.0f, 0.0f, 0.0f)
  }

  final case class vec2(x: Double, y: Double) extends ShaderPrimitive {
    def hash: String          = s"vec2$x$y"
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, 0.0f, 0.0f)
  }
  final case class vec3(x: Double, y: Double, z: Double) extends ShaderPrimitive {
    def hash: String          = s"vec3$x$y$z"
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, 0.0f)
  }
  final case class vec4(x: Double, y: Double, z: Double, w: Double) extends ShaderPrimitive {
    def hash: String          = s"vec4$x$y$z$w"
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, w.toFloat)
  }

}
