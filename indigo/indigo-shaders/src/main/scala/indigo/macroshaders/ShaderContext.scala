package indigo.macroshaders

// opaque type ShaderContext[A, B] = A => B
// object ShaderContext:
//   def apply[A, B](f: A => B): ShaderContext[A, B] = f
//   def pure[A, B](value: B): ShaderContext[A, B]   = _ => value

//   def join[A, B](ctx: ShaderContext[A, ShaderContext[A, B]]): ShaderContext[A, B] =
//     (env: A) => ctx(env)(env)

//   extension [A, B](ctx: ShaderContext[A, B])
//     def map[C](f: B => C): ShaderContext[A, C]                               = (e: A) => f(run(e))
//     def ap[C](f: ShaderContext[A, B => C]): ShaderContext[A, C]              = (e: A) => f(e)(run(e))
//     def flatten[C](using ev: B <:< ShaderContext[A, C]): ShaderContext[A, C] = join((env: A) => ev(run(env)))
//     def flatMap[C](f: B => ShaderContext[A, C]): ShaderContext[A, C]         = join(map(f))
//     def ask: ShaderContext[A, A]                                             = identity
//     def asks(f: A => B): ShaderContext[A, B]                                 = f
//     def run(env: A): B                                                       = ctx(env)

opaque type ShaderContext[Env, A] = Env => Program[A]
object ShaderContext:
  def apply[Env, A](f: Env => Program[A]): ShaderContext[Env, A] = f
  // def pure[Env, A](value: A): ShaderContext[A, A]                = _ => Program(value)

  extension [Env, A](inline ctx: ShaderContext[Env, A])
    inline def toGLSL(): String =
      ShaderMacros.toAST(ctx).render
