package indigo.shared

sealed trait GameContext[+A] {

  def pure[B](b: => B): GameContext[B] =
    this match {
      case GameContext.Pure(_) =>
        GameContext.pure(b)

      case GameContext.Delay(_) =>
        GameContext.delay(b)

      case GameContext.RaiseError(e) =>
        GameContext.raiseError[B](e)
    }

  def isError: Boolean =
    this match {
      case GameContext.RaiseError(_) =>
        true

      case _ =>
        false
    }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeRun(): A =
    this match {
      case GameContext.Pure(a) =>
        a

      case GameContext.Delay(thunk) =>
        thunk()

      case GameContext.RaiseError(e) =>
        throw e
    }

  def attemptRun: Either[Throwable, A] =
    try {
      Right[Throwable, A](unsafeRun())
    } catch {
      case t: Throwable =>
        Left[Throwable, A](t)
    }

  def map[B](f: A => B): GameContext[B]

  def flatMap[B](f: A => GameContext[B]): GameContext[B]

  def flatten[B](implicit ev: A <:< GameContext[B]): GameContext[B]

  def recover[B >: A](value: GameContext[B]): GameContext[B]
}

object GameContext {

  implicit def eqIIO[A](implicit eq: EqualTo[A]): EqualTo[GameContext[A]] =
    EqualTo.create[GameContext[A]] { (a, b) =>
      areEqual(a, b)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def areEqual[A](a1: GameContext[A], a2: GameContext[A])(implicit eq: EqualTo[A]): Boolean =
    (a1, a2) match {
      case (GameContext.Pure(a), GameContext.Pure(b)) =>
        eq.equal(a, b)

      case (GameContext.Delay(a), GameContext.Delay(b)) =>
        eq.equal(a(), b())

      case (GameContext.RaiseError(a), GameContext.RaiseError(b)) =>
        a == b

      case _ =>
        false
    }

  final case class Pure[A](a: A) extends GameContext[A] {
    def map[B](f: A => B): GameContext[B] =
      try {
        Pure(f(a))
      } catch {
        case e: Throwable =>
          RaiseError[B](e)
      }

    def flatMap[B](f: A => GameContext[B]): GameContext[B] =
      try {
        f(a)
      } catch {
        case e: Throwable =>
          RaiseError[B](e)
      }

    def flatten[B](implicit ev: A <:< GameContext[B]): GameContext[B] =
      ev(a)

    def recover[B >: A](value: GameContext[B]): GameContext[B] =
      this
  }

  final case class Delay[A](thunk: () => A) extends GameContext[A] {
    def map[B](f: A => B): GameContext[B] =
      try {
        Delay(() => f(thunk()))
      } catch {
        case e: Throwable =>
          RaiseError[B](e)
      }

    def flatMap[B](f: A => GameContext[B]): GameContext[B] =
      try {
        f(thunk())
      } catch {
        case e: Throwable =>
          RaiseError[B](e)
      }

    def flatten[B](implicit ev: A <:< GameContext[B]): GameContext[B] =
      try {
        ev(thunk())
      } catch {
        case e: Throwable =>
          RaiseError[B](e)
      }

    def recover[B >: A](value: GameContext[B]): GameContext[B] =
      try {
        Delay(() => thunk())
      } catch {
        case _: Throwable =>
          value
      }
  }

  final case class RaiseError[A](e: Throwable) extends GameContext[A] {
    def map[B](f: A => B): GameContext[B] =
      RaiseError[B](e)

    def flatMap[B](f: A => GameContext[B]): GameContext[B] =
      RaiseError[B](e)

    def flatten[B](implicit ev: A <:< GameContext[B]): GameContext[B] =
      RaiseError[B](e)

    def recover[B >: A](value: GameContext[B]): GameContext[B] =
      value
  }

  def apply[A](a: => A): GameContext[A] =
    pure(a)

  def pure[A](a: => A): GameContext[A] =
    try {
      Pure(a)
    } catch {
      case e: Throwable =>
        RaiseError[A](e)
    }

  def delay[A](a: => A): GameContext[A] =
    Delay(() => a)

  def raiseError[A](e: Throwable): GameContext[A] =
    RaiseError[A](e)

}
