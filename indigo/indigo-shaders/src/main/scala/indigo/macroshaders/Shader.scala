package indigo.macroshaders

// opaque type Shader[A, B] = A => B
// object Shader:
//   def apply[A, B](f: A => B): Shader[A, B] = f
//   def pure[A, B](value: B): Shader[A, B]   = _ => value

//   def join[A, B](ctx: Shader[A, Shader[A, B]]): Shader[A, B] =
//     (env: A) => ctx(env)(env)

//   extension [A, B](ctx: Shader[A, B])
//     def map[C](f: B => C): Shader[A, C]                               = (e: A) => f(run(e))
//     def ap[C](f: Shader[A, B => C]): Shader[A, C]              = (e: A) => f(e)(run(e))
//     def flatten[C](using ev: B <:< Shader[A, C]): Shader[A, C] = join((env: A) => ev(run(env)))
//     def flatMap[C](f: B => Shader[A, C]): Shader[A, C]         = join(map(f))
//     def ask: Shader[A, A]                                             = identity
//     def asks(f: A => B): Shader[A, B]                                 = f
//     def run(env: A): B                                                       = ctx(env)

opaque type Shader[Env, A] = Env => Program[A]
object Shader:
  def apply[Env, A](f: Env => Program[A]): Shader[Env, A] = f
  // def pure[Env, A](value: A): Shader[A, A]                = _ => Program(value)

  extension [Env, A](inline ctx: Shader[Env, A])
    inline def toGLSL(): String =
      ShaderMacros.toAST(ctx).render
