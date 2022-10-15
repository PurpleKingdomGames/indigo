package indigo.macroshaders

opaque type Program[A] = A
object Program:
  def apply[A](value: A): Program[A] = value
  def pure[A](value: A): Program[A]  = value

  extension [Env, A](prog: Program[A])
    def map[B](f: A => B): Program[B]                                = f(run)
    def ap[B](f: Program[A => B]): Program[B]                        = f(run)
    def flatten[B](using ev: A <:< Program[B]): Program[B]           = ev(run)
    def flatMap[B](f: A => Program[B]): Program[B]                   = map(f)
    def run: A                                                       = prog
    def |*|[B](other: Program[B]): Program[(A, B)]                   = combine(other)
    def combine[B](other: Program[B]): Program[(A, B)]               = (prog.run, other.run)
    def merge[B, C](other: Program[B])(f: ((A, B)) => C): Program[C] = combine(other).map(f)
    def |>[B](ff: Pipeline[A, B]): Program[B]                        = pipe(ff)
    def pipe[B](ff: Pipeline[A, B]): Program[B]                      = ff.runWith(prog)
