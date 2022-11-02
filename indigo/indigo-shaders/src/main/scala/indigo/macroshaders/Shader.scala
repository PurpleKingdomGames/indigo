package indigo.macroshaders

import scala.annotation.targetName

/** Out `Shader` is a program that can be run on a graphics card as part of the rendering pipeline. Indigo supports two
  * kinds of shaders: Vertex and Fragment
  *
  * The Shader type is similar to a Reader monad (and so exposes `ask` and `asks`) that has some of the types fixed. The
  * idea is that it provides the shader programs environment context, for example it allows access to a `UV` field for
  * working with texture coordinates.
  */
opaque type Shader[In, Out] = In => Out
object Shader:
  inline def apply[In, Out](f: In => Out): Shader[In, Out] = f
  // inline def apply[In, Out](value: Out): Shader[In, Out]    = _ => value
  // inline def pure[In, Out](value: Out): Shader[In, Out]     = _ => value
  // inline def fixed[In, Out](value: Out): Shader[In, Out]    = _ => value
  // inline def ask[In]: Shader[In, In]                 = identity

  // inline def join[In, B](ctx: Shader[In, Shader[In, B]]): Shader[In, B] =
  //   (env: In) => ctx(env).run(env)

  extension [In, Out](inline ctx: Shader[In, Out]) inline def toGLSL: String = ShaderMacros.toAST(ctx).render
//   inline def apply(env: In): Out                                                   = run(env)
//   inline def map[B](f: Out => B): Shader[In, B]                                    = (e: In) => f(ctx(e))
//   inline def ap[B](f: Shader[In, Out => B]): Shader[In, B]                        = (e: In) => map(f.run(e))(e)
//   inline def flatten[B](using ev: Out <:< Shader[In, B]): Shader[In, B]           = join((env: In) => ev(run(env)))
//   inline def flatMap[B](f: Out => Shader[In, B]): Shader[In, B]                   = join(map(f))
//   inline def ask: Shader[In, In]                                                = identity
//   inline def asks(f: In => Out): Shader[In, Out]                                    = (e: In) => f(e)
//   inline def run(env: In): Out                                                     = ctx(env)
//   inline def |*|[B](other: Shader[In, B]): Shader[In, (Out, B)]                   = combine(other)
//   inline def combine[B](other: Shader[In, B]): Shader[In, (Out, B)]               = (e: In) => (run(e), other.run(e))
//   inline def merge[B, C](other: Shader[In, B])(f: ((Out, B)) => C): Shader[In, C] = combine(other).map(f)
//   inline def |>[B](ff: Pipeline[In, Out, B]): Shader[In, B]                       = pipe(ff)
//   inline def pipe[B](ff: Pipeline[In, Out, B]): Shader[In, B]                     = ff.runWith(ctx)
