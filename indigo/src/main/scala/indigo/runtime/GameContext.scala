package indigo.runtime

import indigo.shared.EqualTo

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

  def recover[B >: A](default: GameContext[B]): GameContext[B] =
    cata(a => this.pure(a), default)

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

  def cata[B](f: A => B, default: B): B =
    this match {
      case GameContext.Pure(a) =>
        f(a)

      case GameContext.Delay(thunk) =>
        try {
          f(thunk())
        } catch {
          case _: Throwable =>
            default
        }

      case GameContext.RaiseError(_) =>
        default
    }

  def map[B](f: A => B): GameContext[B] =
    this match {
      case GameContext.RaiseError(e) =>
        cata(x => pure[B](f(x)), GameContext.raiseError[B](e))

      case _ =>
        cata(x => pure[B](f(x)), GameContext.raiseError[B](new Exception("Invalid map of an IIO.")))
    }

  def flatMap[B](f: A => GameContext[B]): GameContext[B] =
    this match {
      case GameContext.RaiseError(e) =>
        cata(x => f(x), GameContext.raiseError[B](e))

      case _ =>
        cata(x => f(x), GameContext.raiseError[B](new Exception("Invalid flatMap of an IIO.")))
    }

  def flatten[B](implicit ev: A <:< GameContext[B]): GameContext[B] =
    this match {
      case GameContext.RaiseError(e) =>
        cata(x => ev(x), GameContext.raiseError[B](e))

      case _ =>
        cata(x => ev(x), GameContext.raiseError[B](new Exception("Invalid flatten of an IIO.")))
    }

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

  final case class Pure[A](a: A)               extends GameContext[A]
  final case class Delay[A](thunk: () => A)    extends GameContext[A]
  final case class RaiseError[A](e: Throwable) extends GameContext[A]

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
