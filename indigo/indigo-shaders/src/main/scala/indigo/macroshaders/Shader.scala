package indigo.macroshaders

import scala.annotation.targetName

/** A `Shader` is a program that can be run on a graphics card as part of the rendering pipeline. Indigo supports two
  * kinds of shaders: Vertex and Fragment
  *
  * The Shader type is similar to a Reader monad (and so exposes `ask` and `asks`) that has some of the types fixed. The
  * idea is that it provides the shader programs environment context, for example it allows access to a `UV` field for
  * working with texture coordinates.
  */
opaque type Shader[Env, A] = Env => A
object Shader:
  inline def apply[Env, A](f: Env => A): Shader[Env, A] = f
  // inline def apply[Env, A](value: A): Shader[Env, A]    = _ => value
  // inline def pure[Env, A](value: A): Shader[Env, A]     = _ => value
  // inline def fixed[Env, A](value: A): Shader[Env, A]    = _ => value
  // inline def ask[Env]: Shader[Env, Env]                 = identity

  // inline def join[Env, B](ctx: Shader[Env, Shader[Env, B]]): Shader[Env, B] =
  //   (env: Env) => ctx(env).run(env)

  extension [Env, A](inline ctx: Shader[Env, A]) inline def toGLSL: String = ShaderMacros.toAST(ctx).render
//   inline def apply(env: Env): A                                                   = run(env)
//   inline def map[B](f: A => B): Shader[Env, B]                                    = (e: Env) => f(ctx(e))
//   inline def ap[B](f: Shader[Env, A => B]): Shader[Env, B]                        = (e: Env) => map(f.run(e))(e)
//   inline def flatten[B](using ev: A <:< Shader[Env, B]): Shader[Env, B]           = join((env: Env) => ev(run(env)))
//   inline def flatMap[B](f: A => Shader[Env, B]): Shader[Env, B]                   = join(map(f))
//   inline def ask: Shader[Env, Env]                                                = identity
//   inline def asks(f: Env => A): Shader[Env, A]                                    = (e: Env) => f(e)
//   inline def run(env: Env): A                                                     = ctx(env)
//   inline def |*|[B](other: Shader[Env, B]): Shader[Env, (A, B)]                   = combine(other)
//   inline def combine[B](other: Shader[Env, B]): Shader[Env, (A, B)]               = (e: Env) => (run(e), other.run(e))
//   inline def merge[B, C](other: Shader[Env, B])(f: ((A, B)) => C): Shader[Env, C] = combine(other).map(f)
//   inline def |>[B](ff: Pipeline[Env, A, B]): Shader[Env, B]                       = pipe(ff)
//   inline def pipe[B](ff: Pipeline[Env, A, B]): Shader[Env, B]                     = ff.runWith(ctx)
