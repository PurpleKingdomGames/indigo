// package indigo.shaders

// object ShaderDsl2 {

// // A fragment shader is: env => color
// // Which is a Reader. Like a reader. Kinda.

//   trait ShaderAny
//   trait ShaderValue extends ShaderAny

//   trait ShaderEnv {
//     val time: float
//   }

//   trait ShaderExpression extends ShaderAny

//   // implicit class RefWithFunction1(val ref: ShaderRef) {
//   //   def ->(exp: ShaderExpression): ShaderFunction1 =
//   //     ShaderFunction1(ref, exp)
//   // }

//   implicit class StringToShaderRef(val sc: StringContext) extends AnyVal {
//     def r(): ShaderRef = ShaderRef(sc.parts.mkString)
//   }

//   final case class ShaderFunction1(arg: ShaderRef, expression: ShaderExpression) extends ShaderValue

//   // final case class ShaderS(s: ShaderFunction1)
//   // final case class ShaderSF(f: ShaderFunction1 -> ShaderFunction1)

//   /*
// Example

// val f: env => float
// Take an env and produce a float

//    */

// // Shader(10)

//   final case class Plus[A <: ShaderAny](a: A, b: float)     extends ShaderExpression
//   final case class Minus[A <: ShaderAny](a: A, b: float)    extends ShaderExpression
//   final case class Multiply[A <: ShaderAny](a: A, b: float) extends ShaderExpression
//   final case class Divide[A <: ShaderAny](a: A, b: float)   extends ShaderExpression

//   final case class ShaderRef(name: String) extends ShaderAny {
//     def ->(exp: ShaderExpression): ShaderFunction1 =
//       ShaderFunction1(this, exp)

//     def +(other: float): Plus[ShaderRef] =
//       Plus(this, other)

//     def -(other: float): Minus[ShaderRef] =
//       Minus(this, other)

//     def *(other: float): Multiply[ShaderRef] =
//       Multiply(this, other)

//     def /(other: float): Divide[ShaderRef] =
//       Divide(this, other)
//   }

//   final case class float(value: Float) extends ShaderValue {
//     def +(other: float): Plus[float] =
//       Plus(this, other)

//     def -(other: float): Minus[float] =
//       Minus(this, other)

//     def *(other: float): Multiply[float] =
//       Multiply(this, other)

//     def /(other: float): Divide[float] =
//       Divide(this, other)
//   }

// }

// // final class Channel[A, B](val f: A => B)

// /*

// Latest thought:

// You can't write complete shaders, your constrained by when you're given.

// First, send time in as a Uniform to both shaders.

// Vertex shaders - most of what is there moves the vertex to the right place and then spends the rest of it's time piping data to the fragment shader.
// So we could just give you a Vertex => Vertex function/expression if you want to modify it.

// Fragment - all the values currently being piped over are what you have to work with. (What if you want to send something custom?)
// Your job is the write a function / expression that takes a pixel value and creates the output color.

// Problems:
// - No custom channels - could use (expensive) uniforms?
// - What happens if you want to draw a shape. Can the maths eb expressive enough? (probably...)

//  */
