package indigo.shared.shader

import scala.reflect.ClassTag

sealed trait ShaderPrimitive {
  def hash: String
  def length: Int
  def toArray: Array[Float]
  def isArray: Boolean
}

sealed trait IsShaderValue[T] {
  def giveLength(p: ShaderPrimitive): Int       = p.length
  def toArray(p: ShaderPrimitive): Array[Float] = p.toArray
}
object IsShaderValue {
  def create[T](): IsShaderValue[T] = new IsShaderValue[T] {}
}

object ShaderPrimitive {

  final case class float(value: Double) extends ShaderPrimitive {
    def hash: String          = s"float${value.toString()}"
    def length: Int           = 1
    def toArray: Array[Float] = Array(value.toFloat)
    def isArray: Boolean      = false
  }
  object float {
    implicit val isShaderValue: IsShaderValue[float] =
      IsShaderValue.create[float]()
  }

  final case class vec2(x: Double, y: Double) extends ShaderPrimitive {
    def hash: String          = s"vec2$x$y"
    def length: Int           = 2
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat)
    def isArray: Boolean      = false
  }
  object vec2 {
    def apply(fill: Double): vec2 =
      vec2(fill, fill)

    implicit val isShaderValue: IsShaderValue[vec2] =
      IsShaderValue.create[vec2]()
  }

  final case class vec3(x: Double, y: Double, z: Double) extends ShaderPrimitive {
    def hash: String          = s"vec3$x$y$z"
    def length: Int           = 4
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, 0.0f)
    def isArray: Boolean      = false
  }
  object vec3 {
    def apply(fill: Double): vec3 =
      vec3(fill, fill, fill)

    implicit val isShaderValue: IsShaderValue[vec3] =
      IsShaderValue.create[vec3]()
  }

  final case class vec4(x: Double, y: Double, z: Double, w: Double) extends ShaderPrimitive {
    def hash: String          = s"vec4$x$y$z$w"
    def length: Int           = 4
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, w.toFloat)
    def isArray: Boolean      = false
  }
  object vec4 {
    def apply(fill: Double): vec4 =
      vec4(fill, fill, fill, fill)

    implicit val isShaderValue: IsShaderValue[vec4] =
      IsShaderValue.create[vec4]()
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  final case class array[T](values: Array[T])(implicit ev: IsShaderValue[T]) extends ShaderPrimitive {
    def hash: String          = s"array${values.mkString}"
    def length: Int           = values.map(p => ev.giveLength(p.asInstanceOf[ShaderPrimitive])).sum
    def toArray: Array[Float] = values.map(p => ev.toArray(p.asInstanceOf[ShaderPrimitive])).flatten.toArray
    def isArray: Boolean      = true
  }
  object array {
    def apply[T: ClassTag](values: T*)(implicit ev: IsShaderValue[T]): array[T] =
      array(values.toArray[T])
  }

}
