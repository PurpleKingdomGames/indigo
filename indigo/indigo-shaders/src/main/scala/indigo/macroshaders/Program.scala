package indigo.macroshaders

opaque type Program[A] = A
object Program:
  inline def apply[A](value: A): Program[A] = value
  inline def pure[A](value: A): Program[A]  = value

  extension [Env, A](inline prog: Program[A])
    inline def map[B](f: A => B): Program[B]                                = f(run)
    inline def ap[B](f: Program[A => B]): Program[B]                        = f(run)
    inline def flatten[B](using ev: A <:< Program[B]): Program[B]           = ev(run)
    inline def flatMap[B](f: A => Program[B]): Program[B]                   = map(f)
    inline def run: A                                                       = prog
    inline def |*|[B](other: Program[B]): Program[(A, B)]                   = combine(other)
    inline def combine[B](other: Program[B]): Program[(A, B)]               = (prog.run, other.run)
    inline def merge[B, C](other: Program[B])(f: ((A, B)) => C): Program[C] = combine(other).map(f)
    inline def |>[B](ff: Pipeline[A, B]): Program[B]                        = pipe(ff)
    inline def pipe[B](ff: Pipeline[A, B]): Program[B]                      = ff.runWith(prog)
