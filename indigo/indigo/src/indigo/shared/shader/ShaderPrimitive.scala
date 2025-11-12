package indigo.shared.shader

import indigo.shared.datatypes.Matrix4
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Vector3
import indigo.shared.datatypes.Vector4
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.geometry.Vertex
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

import scala.collection.immutable.ArraySeq
import scala.reflect.ClassTag
import scala.scalajs.js.JSConverters.*

sealed trait ShaderPrimitive derives CanEqual:
  def length: Int
  def toArray: Array[Float]
  def toJSArray: scala.scalajs.js.Array[Float]
  def isArray: Boolean
  def hash: String

sealed trait IsShaderValue[T]:
  def giveLength: Int
  def toArray: T => Array[Float]

object IsShaderValue:
  def create[T](length: Int, valueToArray: T => Array[Float]): IsShaderValue[T] =
    new IsShaderValue[T]:
      def giveLength: Int            = length
      def toArray: T => Array[Float] = t => valueToArray(t)

  given IsShaderValue[Float] =
    create(ShaderPrimitive.float.length, f => Array(f))
  given IsShaderValue[RGB] =
    create(ShaderPrimitive.vec3.length, rgb => ShaderPrimitive.vec3.fromRGB(rgb).toArray)
  given IsShaderValue[RGBA] =
    create(ShaderPrimitive.vec4.length, rgba => ShaderPrimitive.vec4.fromRGBA(rgba).toArray)
  given IsShaderValue[Matrix4] =
    create(ShaderPrimitive.vec4.length, mat => ShaderPrimitive.mat4.fromMatrix4(mat).toArray)
  given IsShaderValue[CheapMatrix4] =
    create(ShaderPrimitive.vec4.length, mat => ShaderPrimitive.mat4.fromCheapMatrix4(mat).toArray)

object ShaderPrimitive:

  final case class float(value: Float) extends ShaderPrimitive:
    val length: Int                              = float.length
    def toArray: Array[Float]                    = Array(value)
    def toJSArray: scala.scalajs.js.Array[Float] = scala.scalajs.js.Array(value)
    val isArray: Boolean                         = false
    val hash: String                             = value.toString
  object float:
    val length: Int = 1

    def apply(fill: Double): float =
      float(fill.toFloat)

    def apply(fill: Int): float =
      float(fill.toFloat)

    def apply(fill: Long): float =
      float(fill.toFloat)

    def fromMillis(millis: Millis): float =
      float(millis.toDouble)

    def fromRadians(radians: Radians): float =
      float(radians.toDouble)

    def fromSeconds(seconds: Seconds): float =
      float(seconds.toDouble)

    given IsShaderValue[float] =
      IsShaderValue.create[float](length, _.toArray)

  final case class vec2(x: Float, y: Float) extends ShaderPrimitive:
    val length: Int                              = vec2.length
    def toArray: Array[Float]                    = Array(x, y)
    def toJSArray: scala.scalajs.js.Array[Float] = scala.scalajs.js.Array(x, y)
    val isArray: Boolean                         = false
    val hash: String                             = x.toString + y.toString
  object vec2:
    val length: Int = 2

    def apply(fill: Float): vec2 =
      vec2(fill, fill)

    def apply(fill: Double): vec2 =
      vec2(fill.toFloat, fill.toFloat)

    def apply(x: Double, y: Double): vec2 =
      vec2(x.toFloat, y.toFloat)

    def fromPoint(pt: Point): vec2    = vec2(pt.x.toFloat, pt.y.toFloat)
    def fromSize(s: Size): vec2       = vec2(s.width.toFloat, s.height.toFloat)
    def fromVector2(v: Vector2): vec2 = vec2(v.x, v.y)
    def fromVertex(v: Vertex): vec2   = vec2(v.x, v.y)

    given IsShaderValue[vec2] =
      IsShaderValue.create[vec2](length, _.toArray)

  final case class vec3(x: Float, y: Float, z: Float) extends ShaderPrimitive:
    val length: Int                              = vec3.length
    def toArray: Array[Float]                    = Array(x, y, z, 0.0f)
    def toJSArray: scala.scalajs.js.Array[Float] = scala.scalajs.js.Array(x, y, z, 0.0f)
    val isArray: Boolean                         = false
    val hash: String                             = x.toString + y.toString + z.toString
  object vec3:
    val length: Int = 4

    def apply(fill: Float): vec3 =
      vec3(fill, fill, fill)

    def apply(fill: Double): vec3 =
      vec3(fill.toFloat, fill.toFloat, fill.toFloat)

    def apply(x: Double, y: Double, z: Double): vec3 =
      vec3(x.toFloat, y.toFloat, z.toFloat)

    def fromRGB(rgb: RGB): vec3       = vec3(rgb.r, rgb.g, rgb.b)
    def fromVector3(v: Vector3): vec3 = vec3(v.x, v.y, v.z)

    given IsShaderValue[vec3] =
      IsShaderValue.create[vec3](length, _.toArray)

  final case class vec4(x: Float, y: Float, z: Float, w: Float) extends ShaderPrimitive:
    val length: Int                              = vec4.length
    def toArray: Array[Float]                    = Array(x, y, z, w)
    def toJSArray: scala.scalajs.js.Array[Float] = scala.scalajs.js.Array(x, y, z, w)
    val isArray: Boolean                         = false
    val hash: String                             = x.toString + y.toString + z.toString + w.toString
  object vec4:
    val length: Int = 4

    def apply(fill: Float): vec4 =
      vec4(fill, fill, fill, fill)

    def apply(fill: Double): vec4 =
      vec4(fill.toFloat, fill.toFloat, fill.toFloat, fill.toFloat)

    def apply(x: Double, y: Double, z: Double, w: Double): vec4 =
      vec4(x.toFloat, y.toFloat, z.toFloat, w.toFloat)

    def fromRGB(rgb: RGB): vec4           = vec4(rgb.r, rgb.g, rgb.b, 1.0)
    def fromRGBA(rgba: RGBA): vec4        = vec4(rgba.r, rgba.g, rgba.b, rgba.a)
    def fromVector4(v: Vector4): vec4     = vec4(v.x, v.y, v.z, v.w)
    def fromRectangle(r: Rectangle): vec4 = vec4(r.x.toFloat, r.y.toFloat, r.width.toFloat, r.height.toFloat)

    given IsShaderValue[vec4] =
      IsShaderValue.create[vec4](length, _.toArray)

  final case class mat4(mat: Array[Float]) extends ShaderPrimitive:
    val length: Int      = mat4.length
    val isArray: Boolean = false
    val hash: String     = mat.mkString

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def toArray: Array[Float] =
      if mat.length == mat4.length then mat
      else throw new Exception("mat4 was not of length 16!")
    def toJSArray: scala.scalajs.js.Array[Float] = toArray.toJSArray
  object mat4:
    val length: Int = 16

    def fromCheapMatrix4(matrix: CheapMatrix4): mat4 =
      mat4(matrix.toJSArray.toArray)

    def fromMatrix4(matrix: Matrix4): mat4 =
      mat4(matrix.toArray.map(_.toFloat))

    given IsShaderValue[mat4] =
      IsShaderValue.create[mat4](length, _.toArray)

  /** array data to send to the fragment shader
    *
    * @param size
    *   Size != Length! Size is the memory allocated, the max possible number of entries, e.g. you are sending 3 x vec2
    *   but the size is 16, meaning the max you _could_ send is 16 x vec2 but no more than that.
    * @param values
    *   The values to send
    * @param ev
    *   Implicit proof that T is a Shader value (float, vec2, vec3, vec4)
    */
  final case class array[T](size: Int, values: ArraySeq[T])(using ev: IsShaderValue[T]) extends ShaderPrimitive:
    val length: Int      = values.length * 4
    val isArray: Boolean = true
    val hash: String     = size.toString + values.mkString

    def toArray: Array[Float] =
      val data =
        values.unsafeArray
          .asInstanceOf[Array[T]]
          .map(p => expandTo4(ev.toArray(p)))
          .flatten

      val len           = data.length
      val allocatedSize = size * 4

      if (len == allocatedSize)
        data
      else if (len > allocatedSize)
        data.take(allocatedSize)
      else
        data ++ Array.fill[Float](allocatedSize - data.length)(0)

    def toJSArray: scala.scalajs.js.Array[Float] = toArray.toJSArray

    private val empty1: Array[Float] = Array[Float](0.0f)
    private val empty2: Array[Float] = Array[Float](0.0f, 0.0f)
    private val empty3: Array[Float] = Array[Float](0.0f, 0.0f, 0.0f)

    private def expandTo4(arr: Array[Float]): Array[Float] =
      arr.length match
        case 0 => arr
        case 1 => arr ++ empty3
        case 2 => arr ++ empty2
        case 3 => arr ++ empty1
        case 4 => arr
        case _ => arr

  object array:
    def apply[T: ClassTag](size: Int)(values: T*)(using ev: IsShaderValue[T]): array[T] =
      array(size, ArraySeq.from[T](values.toArray[T]))
    def apply[T: ClassTag](size: Int, values: Array[T])(using ev: IsShaderValue[T]): array[T] =
      array(size, ArraySeq.from[T](values))
    def apply[T: ClassTag](size: Int, values: List[T])(using ev: IsShaderValue[T]): array[T] =
      array(size, ArraySeq.from[T](values))

  /** Advanced usage only, a raw array of Float's to send to the fragment shader. Warning: The assumption here is that
    * you know what you're doing i.e. how the packing/unpacking rules work. If you don't, use a normal shader `array`!
    *
    * @param arr
    *   The array of Floats to send
    */
  final case class rawArray(arr: Array[Float]) extends ShaderPrimitive:
    val length: Int                              = arr.length
    val isArray: Boolean                         = true
    def toArray: Array[Float]                    = arr
    def toJSArray: scala.scalajs.js.Array[Float] = toArray.toJSArray
    val hash: String                             = arr.mkString
  object rawArray:
    def apply(values: Float*): rawArray =
      rawArray(values.toArray[Float])
    def apply(values: List[Float]): rawArray =
      rawArray(values.toArray)

  /** Advanced usage only, a raw array of Float's to send to the fragment shader. Warning: The assumption here is that
    * you know what you're doing i.e. how the packing/unpacking rules work. If you don't, use a normal shader `array`!
    *
    * @param arr
    *   The array of Floats to send
    */
  final case class rawJSArray(arr: scala.scalajs.js.Array[Float]) extends ShaderPrimitive:
    val length: Int                              = arr.length
    val isArray: Boolean                         = true
    def toArray: Array[Float]                    = arr.toArray
    def toJSArray: scala.scalajs.js.Array[Float] = arr
    val hash: String                             = arr.mkString
  object rawJSArray:
    def apply(values: Float*): rawJSArray =
      rawJSArray(values.toJSArray)
    def apply(values: List[Float]): rawJSArray =
      rawJSArray(values.toJSArray)
