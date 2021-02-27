package indigo.shared.shader

import scala.reflect.ClassTag

sealed trait ShaderPrimitive {
  def hash: String
  def length: Int
  def toArray: Array[Float]
  def isArray: Boolean
}

sealed trait IsShaderValue[T] {
  def giveLength: Int
  def toArray(p: ShaderPrimitive): Array[Float] = p.toArray
}
object IsShaderValue {
  def create[T](length: Int): IsShaderValue[T] =
    new IsShaderValue[T] {
      val giveLength: Int = length
    }
}

object ShaderPrimitive {

  final case class float(value: Double) extends ShaderPrimitive {
    val hash: String          = s"float${value.toString()}"
    val length: Int           = float.length
    def toArray: Array[Float] = Array(value.toFloat)
    val isArray: Boolean      = false
  }
  object float {
    val length: Int = 1

    implicit val isShaderValue: IsShaderValue[float] =
      IsShaderValue.create[float](length)
  }

  final case class vec2(x: Double, y: Double) extends ShaderPrimitive {
    val hash: String          = s"vec2$x$y"
    val length: Int           = vec2.length
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat)
    val isArray: Boolean      = false
  }
  object vec2 {
    val length: Int = 2

    def apply(fill: Double): vec2 =
      vec2(fill, fill)

    implicit val isShaderValue: IsShaderValue[vec2] =
      IsShaderValue.create[vec2](length)
  }

  final case class vec3(x: Double, y: Double, z: Double) extends ShaderPrimitive {
    val hash: String          = s"vec3$x$y$z"
    val length: Int           = vec3.length
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, 0.0f)
    val isArray: Boolean      = false
  }
  object vec3 {
    val length: Int = 4

    def apply(fill: Double): vec3 =
      vec3(fill, fill, fill)

    implicit val isShaderValue: IsShaderValue[vec3] =
      IsShaderValue.create[vec3](length)
  }

  final case class vec4(x: Double, y: Double, z: Double, w: Double) extends ShaderPrimitive {
    val hash: String          = s"vec4$x$y$z$w"
    val length: Int           = vec4.length
    def toArray: Array[Float] = Array(x.toFloat, y.toFloat, z.toFloat, w.toFloat)
    val isArray: Boolean      = false
  }
  object vec4 {
    val length: Int = 4

    def apply(fill: Double): vec4 =
      vec4(fill, fill, fill, fill)

    implicit val isShaderValue: IsShaderValue[vec4] =
      IsShaderValue.create[vec4](length)
  }

  /**
    * array data to send to the fragment shader
    *
    * @param size Size != Length! Size is the memory allocated, the max possible number of entries, e.g. you are sending 3 x vec2 but the size is 16, meaning the max you _could_ send is 16 x vec2 but no more than that.
    * @param values The values to send
    * @param ev Implicit proof that T is a Shader value (float, vec2, vec3, vec4)
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  final case class array[T](size: Int, values: Array[T])(implicit ev: IsShaderValue[T]) extends ShaderPrimitive {
    val hash: String     = s"array${values.mkString}"
    val length: Int      = values.map(_ => ev.giveLength).sum
    val isArray: Boolean = true

    def toArray: Array[Float] = {
      val data =
        values.map(p => ev.toArray(p.asInstanceOf[ShaderPrimitive])).flatten.toArray

      val len = data.length
      val allocatedSize = size * ev.giveLength

      if (len == allocatedSize)
        data
      else if (len > allocatedSize)
        data.take(allocatedSize)
      else
        data ++ Array.fill[Float](allocatedSize - data.length)(0)
    }
  }
  object array {
    def apply[T: ClassTag](size: Int)(values: T*)(implicit ev: IsShaderValue[T]): array[T] =
      array(size, values.toArray[T])
  }

}
