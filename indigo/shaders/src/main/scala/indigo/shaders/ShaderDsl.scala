package indigo.shaders

// trait ShaderS {

// }

// trait ShaderSF {
//   val f: ShaderFunction
// }
sealed trait ShaderFunction[P1 <: ShaderPrimitive[_], P2 <: ShaderPrimitive[_]]
final case class ShaderFunctionRef[P1 <: ShaderPrimitive[_], P2 <: ShaderPrimitive[_]](in: ShaderRef[P1], out: ShaderValue[P2]) extends ShaderFunction[P1, P2] {
  override def toString(): String =
    out.toString()
}
// final case class ShaderFunctionValue[P1 <: ShaderPrimitive[_], P2 <: ShaderPrimitive[_]](in: ShaderValue[P1], out: ShaderValue[P2]) extends ShaderFunction[P1, P2]

// final case class ShaderOpPipe[P <: ShaderPrimitive](in: ShaderValue[P], f: ShaderFunctionRef[P])

sealed trait ShaderRef[P <: ShaderPrimitive[_]] {
  def __name: String
}
sealed trait ShaderValue[P <: ShaderPrimitive[_]]
sealed trait ShaderPrimitive[T] {
  def __sample: T
}
sealed trait ShaderOp

object ShaderDsl {

  trait SampleProvider[T] {
    def giveSample: T
  }
  object SampleProvider {
    def create[T](sp: ShaderPrimitive[T]): SampleProvider[T] =
      new SampleProvider[T] {
        def giveSample: T = sp.__sample
      }
  }

  implicit val floatSampleProvider: SampleProvider[Float] =
    SampleProvider.create[Float](float)

  /*

For a given input (uniform) of running time
Use the time to change the alpha of a solid colour
Output the resulting colour to the out channel

Borrow from Signal and SignalFunction - but you're not doing real work, you're declaring intent...

   */

  final case class Input(name: String) extends ShaderRef[float.type] {
    def __name: String = name
  }

  final case class Eval[T, P <: ShaderPrimitive[_]](f: P => ShaderPrimitive[Float])(implicit sp: SampleProvider[T]) extends ShaderValue[ShaderPrimitive[_]] {
    override def toString(): String =
      sp.giveSample.toString()
  }

  // Types
  final case object float extends ShaderPrimitive[Float] {
    type T = Float
    val __sample: Float = 0.0f
  }

  // final case class vec2(x: Float, y: Float)

  // final case class vec3(x: Float, y: Float, z: Float)

  // final case class vec4(x: Float, y: Float, z: Float, w: Float)
  final case class rgba(r: Float, g: Float, b: Float, a: Float) extends ShaderPrimitive[Float] {
    type T = Float
    val __sample: Float = 0.0f
  }

  // final case class mat4(
  //     m0: Float,
  //     m1: Float,
  //     m2: Float,
  //     m3: Float,
  //     m4: Float,
  //     m5: Float,
  //     m6: Float,
  //     m7: Float,
  //     m8: Float,
  //     m9: Float,
  //     m10: Float,
  //     m11: Float,
  //     m12: Float,
  //     m13: Float,
  //     m14: Float,
  //     m15: Float
  // )

  // Operations
  // final case class max(a: Double, b: Double)
  // final case class min(a: Double, b: Double)
  // final case class dot(a: vec2, b: vec2)
  // final case class cos(f: float)
  // final case class sin(f: float)
  // final case class tan(f: float)
}
