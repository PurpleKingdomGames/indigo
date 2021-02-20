package indigo.shared.shader

sealed trait ShaderPrimitive {
  def hash: String
  def length: Int
  def toArray: Array[Float]
}
object ShaderPrimitive {

  final case class float(value: Double) extends ShaderPrimitive {
    def hash: String          = s"float${value.toString()}"
    def length: Int           = 1
    def toArray: Array[Float] = Array(value.toFloat)
  }

  final case class vec2(x: Double, y: Double) extends ShaderPrimitive {
    def hash: String          = s"vec2$x$y"
    def length: Int           = 2
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat)
  }
  object vec2 {
    def apply(fill: Double): vec2 =
      vec2(fill, fill)
  }
  final case class vec3(x: Double, y: Double, z: Double) extends ShaderPrimitive {
    def hash: String          = s"vec3$x$y$z"
    def length: Int           = 4
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, 0.0f)
  }
  object vec3 {
    def apply(fill: Double): vec3 =
      vec3(fill, fill, fill)
  }
  final case class vec4(x: Double, y: Double, z: Double, w: Double) extends ShaderPrimitive {
    def hash: String          = s"vec4$x$y$z$w"
    def length: Int           = 4
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, w.toFloat)
  }
  object vec4 {
    def apply(fill: Double): vec4 =
      vec4(fill, fill, fill, fill)
  }

}
