package indigo.macroshaders

import indigo.macroshaders.ShaderDSL.*

import scala.compiletime.erasedValue
import scala.compiletime.summonInline
import scala.deriving.Mirror

object EnvReader:

  inline private def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[ValueOf[t]].value.asInstanceOf[String] :: summonLabels[ts]

  inline private def summonTypeName[T <: Tuple]: List[ShaderTypeOf[_]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[ShaderTypeOf[t]] :: summonTypeName[ts]

  inline def readUBO[T](using m: Mirror.ProductOf[T]): List[UBOField] =
    val labels  = summonLabels[m.MirroredElemLabels]
    val typeOfs = summonTypeName[m.MirroredElemTypes]
    labels.zip(typeOfs.map(_.typeOf)).map(p => UBOField(p._1, p._2))

  trait ShaderTypeOf[A]:
    def typeOf: String

  object ShaderTypeOf:

    given ShaderTypeOf[Int] with
      def typeOf: String = "int"

    given ShaderTypeOf[Float] with
      def typeOf: String = "float"

    given ShaderTypeOf[vec2] with
      def typeOf: String = "vec2"

    given ShaderTypeOf[vec3] with
      def typeOf: String = "vec3"

    given ShaderTypeOf[vec4] with
      def typeOf: String = "vec4"

  final case class UBOField(name: String, typeOf: String)
