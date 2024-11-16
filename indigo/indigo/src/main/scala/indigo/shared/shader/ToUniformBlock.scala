package indigo.shared.shader

import indigo.shared.collections.Batch
import indigo.shared.datatypes.*
import indigo.shared.geometry.Vertex
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

import scala.compiletime.*
import scala.deriving.Mirror

trait ToUniformBlock[T]:
  def toUniformBlock(value: T): UniformBlock

object ToUniformBlock:
  import scala.deriving.*
  import scala.compiletime.{erasedValue, summonInline}

  inline given derived[T](using m: Mirror.ProductOf[T]): ToUniformBlock[T] =
    new ToUniformBlock[T]:
      def toUniformBlock(value: T): UniformBlock =
        UBOReader.readUBO[T](value: T).toUniformBlock

  object UBOReader:

    inline private def summonLabels[T <: Tuple]: List[String] =
      inline erasedValue[T] match
        case _: EmptyTuple => Nil
        case _: (t *: ts)  => summonInline[ValueOf[t]].value.asInstanceOf[String] :: summonLabels[ts]

    inline private def summonTypeName[T <: Tuple]: List[ShaderTypeOf[?]] =
      inline erasedValue[T] match
        case _: EmptyTuple => Nil
        case _: (t *: ts)  => summonTypeOrError[t] :: summonTypeName[ts]

    inline private def summonTypeOrError[T]: ShaderTypeOf[T] =
      summonFrom {
        case given ShaderTypeOf[T] => summonInline[ShaderTypeOf[T]]
        case _ =>
          error(
            "Unsupported type. Only supported types in Indigo shaders are: Int, Long, Float, Double, RGBA, RGB, Point, Size, Vertex, Vector2, Vector3, Vector4, Rectangle, Matrix4, Depth, Radians, Millis, Seconds, Array[Float], js.Array[Float]"
          )
      }

    inline def readUBO[T](value: T)(using m: Mirror.ProductOf[T]): UBODef =
      UBODef(
        constValue[m.MirroredLabel],
        value
          .asInstanceOf[Product]
          .productIterator
          .toList
          .zip(
            summonLabels[m.MirroredElemLabels]
              .zip(summonTypeName[m.MirroredElemTypes])
          )
          .map(p => UBOField(p._2._1, p._2._2.toShaderPrimitive(p._1.asInstanceOf[p._2._2.Out])))
      )

  trait ShaderTypeOf[A]:
    type Out = A
    def toShaderPrimitive(value: A): ShaderPrimitive

  object ShaderTypeOf:

    given ShaderTypeOf[Int] with
      def toShaderPrimitive(value: Int): ShaderPrimitive =
        ShaderPrimitive.float(value)

    given ShaderTypeOf[Long] with
      def toShaderPrimitive(value: Long): ShaderPrimitive =
        ShaderPrimitive.float(value)

    given ShaderTypeOf[Float] with
      def toShaderPrimitive(value: Float): ShaderPrimitive =
        ShaderPrimitive.float(value)

    given ShaderTypeOf[Double] with
      def toShaderPrimitive(value: Double): ShaderPrimitive =
        ShaderPrimitive.float(value)

    given ShaderTypeOf[RGBA] with
      def toShaderPrimitive(value: RGBA): ShaderPrimitive =
        ShaderPrimitive.vec4.fromRGBA(value)

    given ShaderTypeOf[RGB] with
      def toShaderPrimitive(value: RGB): ShaderPrimitive =
        ShaderPrimitive.vec3.fromRGB(value)

    given ShaderTypeOf[Point] with
      def toShaderPrimitive(value: Point): ShaderPrimitive =
        ShaderPrimitive.vec2.fromPoint(value)

    given ShaderTypeOf[Size] with
      def toShaderPrimitive(value: Size): ShaderPrimitive =
        ShaderPrimitive.vec2.fromSize(value)

    given ShaderTypeOf[Vertex] with
      def toShaderPrimitive(value: Vertex): ShaderPrimitive =
        ShaderPrimitive.vec2.fromVertex(value)

    given ShaderTypeOf[Vector2] with
      def toShaderPrimitive(value: Vector2): ShaderPrimitive =
        ShaderPrimitive.vec2.fromVector2(value)

    given ShaderTypeOf[Vector3] with
      def toShaderPrimitive(value: Vector3): ShaderPrimitive =
        ShaderPrimitive.vec3.fromVector3(value)

    given ShaderTypeOf[Vector4] with
      def toShaderPrimitive(value: Vector4): ShaderPrimitive =
        ShaderPrimitive.vec4.fromVector4(value)

    given ShaderTypeOf[Rectangle] with
      def toShaderPrimitive(value: Rectangle): ShaderPrimitive =
        ShaderPrimitive.vec4.fromRectangle(value)

    given ShaderTypeOf[Matrix4] with
      def toShaderPrimitive(value: Matrix4): ShaderPrimitive =
        ShaderPrimitive.mat4.fromMatrix4(value)

    given ShaderTypeOf[Depth] with
      def toShaderPrimitive(value: Depth): ShaderPrimitive =
        ShaderPrimitive.float.fromDepth(value)

    given ShaderTypeOf[Radians] with
      def toShaderPrimitive(value: Radians): ShaderPrimitive =
        ShaderPrimitive.float.fromRadians(value)

    given ShaderTypeOf[Millis] with
      def toShaderPrimitive(value: Millis): ShaderPrimitive =
        ShaderPrimitive.float.fromMillis(value)

    given ShaderTypeOf[Seconds] with
      def toShaderPrimitive(value: Seconds): ShaderPrimitive =
        ShaderPrimitive.float.fromSeconds(value)

    given ShaderTypeOf[scala.Array[Float]] with
      def toShaderPrimitive(value: Array[Float]): ShaderPrimitive =
        ShaderPrimitive.rawArray(value)

    import scalajs.js.Array as JSArray

    given ShaderTypeOf[JSArray[Float]] with
      def toShaderPrimitive(value: JSArray[Float]): ShaderPrimitive =
        ShaderPrimitive.rawJSArray(value)

  final case class UBODef(name: String, fields: List[UBOField]):
    def toUniformBlock: UniformBlock =
      UniformBlock(
        UniformBlockName(name),
        Batch.fromList(fields).map { f =>
          Uniform(f.name) -> f.typeOf
        }
      )

  final case class UBOField(name: String, typeOf: ShaderPrimitive)
