// package indigo.macroshaders

// /** A `Pipeline` is very similar to a `SignalFunction` but for shader programs.
//   *
//   * It is simply a function that maps a `Shader[Env, A]` to a `Shader[Env, A]`, conviently disguised a simple `A => B`.
//   * So far, it's exactly a Functor that is weirdly outside of `Shader`, and it is true that you do not need Pipelines to
//   * write shaders. What `Pipeline`s give you is nice transformation syntax. If you've ever see a visual shader editor -
//   * it's a bit like that.
//   */
// opaque type Pipeline[Env, A, B] = Shader[Env, A] => Shader[Env, B]
// object Pipeline:

//   import Shader.*

//   inline def apply[Env, A, B](f: A => B): Pipeline[Env, A, B] =
//     lift(f)

//   extension [Env, A, B](ff: Pipeline[Env, A, B])
//     def runWith: Shader[Env, A] => Shader[Env, B] = ff

//     def >>>[C](other: Pipeline[Env, B, C]): Pipeline[Env, A, C] =
//       andThen(other)

//     def andThen[C](other: Pipeline[Env, B, C]): Pipeline[Env, A, C] =
//       ff andThen other

//     def &&&[C](other: Pipeline[Env, A, C]): Pipeline[Env, A, (B, C)] =
//       and(other)

//     def and[C](other: Pipeline[Env, A, C]): Pipeline[Env, A, (B, C)] =
//       Pipeline.parallel(ff, other)

//   /** Equvilent to `pure` but for Pipelines
//     */
//   def arr[Env, A, B](f: A => B): Pipeline[Env, A, B] =
//     lift[Env, A, B](f)

//   def lift[Env, A, B](f: A => B): Pipeline[Env, A, B] =
//     (fa: Shader[Env, A]) => fa.map(f)

//   def flatLift[Env, A, B](f: A => Shader[Env, B]): Pipeline[Env, A, B] =
//     (fa: Shader[Env, A]) => fa.flatMap(f)

//   def parallel[Env, A, B, C](
//       fa: Pipeline[Env, A, B],
//       fb: Pipeline[Env, A, C]
//   ): Pipeline[Env, A, (B, C)] =
//     (s: Shader[Env, A]) => fa.runWith(s) |*| fb.runWith(s)
