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

  final case class float(value: Float) extends ShaderPrimitive {
    val hash: String          = s"float${value.toString()}"
    val length: Int           = float.length
    def toArray: Array[Float] = Array(value)
    val isArray: Boolean      = false
  }
  object float {
    val length: Int = 1

    def apply(fill: Double): float =
      float(fill.toFloat)

    implicit val isShaderValue: IsShaderValue[float] =
      IsShaderValue.create[float](length)
  }

  final case class vec2(x: Float, y: Float) extends ShaderPrimitive {
    val hash: String          = s"vec2$x$y"
    val length: Int           = vec2.length
    def toArray: Array[Float] = Array(x, y)
    val isArray: Boolean      = false
  }
  object vec2 {
    val length: Int = 2

    def apply(fill: Float): vec2 =
      vec2(fill, fill)

    def apply(fill: Double): vec2 =
      vec2(fill.toFloat, fill.toFloat)

    def apply(x: Double, y: Double): vec2 =
      vec2(x.toFloat, y.toFloat)

    implicit val isShaderValue: IsShaderValue[vec2] =
      IsShaderValue.create[vec2](length)
  }

  final case class vec3(x: Float, y: Float, z: Float) extends ShaderPrimitive {
    val hash: String          = s"vec3$x$y$z"
    val length: Int           = vec3.length
    def toArray: Array[Float] = Array(x, y, z, 0.0f)
    val isArray: Boolean      = false
  }
  object vec3 {
    val length: Int = 4

    def apply(fill: Float): vec3 =
      vec3(fill, fill, fill)

    def apply(fill: Double): vec3 =
      vec3(fill.toFloat, fill.toFloat, fill.toFloat)

    def apply(x: Double, y: Double, z: Double): vec3 =
      vec3(x.toFloat, y.toFloat, z.toFloat)

    implicit val isShaderValue: IsShaderValue[vec3] =
      IsShaderValue.create[vec3](length)
  }

  final case class vec4(x: Float, y: Float, z: Float, w: Float) extends ShaderPrimitive {
    val hash: String          = s"vec4$x$y$z$w"
    val length: Int           = vec4.length
    def toArray: Array[Float] = Array(x, y, z, w)
    val isArray: Boolean      = false
  }
  object vec4 {
    val length: Int = 4

    def apply(fill: Float): vec4 =
      vec4(fill, fill, fill, fill)

    def apply(fill: Double): vec4 =
      vec4(fill.toFloat, fill.toFloat, fill.toFloat, fill.toFloat)

    def apply(x: Double, y: Double, z: Double, w: Double): vec4 =
      vec4(x.toFloat, y.toFloat, z.toFloat, w.toFloat)

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
    val length: Int      = values.length * 4
    val isArray: Boolean = true

    def toArray: Array[Float] = {
      val data =
        values
          .map(p => expandTo4(ev.toArray(p.asInstanceOf[ShaderPrimitive])))
          .flatten
          .toArray

      val len           = data.length
      val allocatedSize = size * 4

      if (len == allocatedSize)
        data
      else if (len > allocatedSize)
        data.take(allocatedSize)
      else
        data ++ Array.fill[Float](allocatedSize - data.length)(0)
    }

    private val empty1: Array[Float] = Array[Float](0.0f)
    private val empty2: Array[Float] = Array[Float](0.0f, 0.0f)
    private val empty3: Array[Float] = Array[Float](0.0f, 0.0f, 0.0f)

    private def expandTo4(arr: Array[Float]): Array[Float] =
      arr.length match {
        case 0 => arr
        case 1 => arr ++ empty3
        case 2 => arr ++ empty2
        case 3 => arr ++ empty1
        case 4 => arr
        case _ => arr
      }
  }
  object array {
    def apply[T: ClassTag](size: Int)(values: T*)(implicit ev: IsShaderValue[T]): array[T] =
      array(size, values.toArray[T])
  }

}
