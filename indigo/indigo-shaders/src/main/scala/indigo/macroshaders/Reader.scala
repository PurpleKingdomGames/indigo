package indigo.macroshaders

opaque type Reader[A, B] = A => B
object Reader:
  def apply[A, B](f: A => B): Reader[A, B] = f
  def pure[A, B](value: B): Reader[A, B]   = _ => value

  def join[A, B](frag: Reader[A, Reader[A, B]]): Reader[A, B] =
    (env: A) => frag(env)(env)

  extension [A, B](frag: Reader[A, B])
    def map[C](f: B => C): Reader[A, C]                        = (e: A) => f(run(e))
    def ap[C](f: Reader[A, B => C]): Reader[A, C]              = (e: A) => f(e)(run(e))
    def flatten[C](using ev: B <:< Reader[A, C]): Reader[A, C] = join((env: A) => ev(run(env)))
    def flatMap[C](f: B => Reader[A, C]): Reader[A, C]         = join(map(f))
    def ask: Reader[A, A]                                      = identity
    def asks(f: A => B): Reader[A, B]                          = f
    def run(env: A): B                                         = frag(env)
