package indigo.macroshaders

import scala.annotation.targetName

/** A `Shader` is a program that can be run on a graphics card as part of the rendering pipeline. Indigo supports two
  * kinds of shaders: Vertex and Fragment
  *
  * The Shader type is similar to a Reader monad (and so exposes `ask` and `asks`) that has some of the types fixed. The
  * idea is that it provides the shader programs environment context, for example it allows access to a `UV` field for
  * working with texture coordinates.
  */
opaque type Shader[Env, A] = Env => Program[A]
object Shader:
  inline def apply[Env, A](f: Env => Program[A]): Shader[Env, A] = f
  @targetName("Shader_apply_asks")
  inline def apply[Env, A](f: Env => A): Shader[Env, A] = (e: Env) => Program(f(e))
  inline def apply[Env, A](value: A): Shader[Env, A]    = _ => Program(value)
  inline def pure[Env, A](value: A): Shader[Env, A]     = _ => Program(value)
  inline def fixed[Env, A](value: A): Shader[Env, A]    = _ => Program(value)

  inline def join[Env, B](ctx: Shader[Env, Shader[Env, B]]): Shader[Env, B] =
    (env: Env) => ctx(env).run(env)

  extension [Env, A](inline ctx: Shader[Env, A])
    inline def toGLSL: String =
      ShaderMacros.toAST(ctx).render
    inline def map[B](f: A => B): Shader[Env, B]                          = (e: Env) => program(e).map(f)
    inline def ap[B](f: Shader[Env, A => B]): Shader[Env, B]              = (e: Env) => f(e).map(fn => fn(ctx.run(e)))
    inline def flatten[B](using ev: A <:< Shader[Env, B]): Shader[Env, B] = join((env: Env) => program(env).map(ev))
    inline def flatMap[B](f: A => Shader[Env, B]): Shader[Env, B]         = join(map(f))
    inline def ask: Shader[Env, Env]                                      = (e: Env) => Program(e)
    inline def asks(f: Env => A): Shader[Env, A]                          = (e: Env) => Program(f(e))
    inline def program(env: Env): Program[A]                              = ctx(env)
    inline def run(env: Env): A                                           = program(env).run
