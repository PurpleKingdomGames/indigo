package indigo.runtime

import indigo.shared.Eq

/**
  * IIO (IndigoIO) to differentiate from other IO monads
  *
  * @tparam A Type of the value being carried for evaluation
  */
sealed trait IIO[+A] {

  def pure[B](b: => B): IIO[B] =
    this match {
      case IIO.Pure(_) =>
        IIO.pure(b)

      case IIO.Delay(_) =>
        IIO.delay(b)

      case IIO.RaiseError(e) =>
        IIO.raiseError[B](e)
    }

  def isError: Boolean =
    this match {
      case IIO.RaiseError(_) =>
        true

      case _ =>
        false
    }

  def recover[B >: A](default: IIO[B]): IIO[B] =
    cata(a => this.pure(a), default)

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeRun(): A =
    this match {
      case IIO.Pure(a) =>
        a

      case IIO.Delay(thunk) =>
        thunk()

      case IIO.RaiseError(e) =>
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
      case IIO.Pure(a) =>
        f(a)

      case IIO.Delay(thunk) =>
        try {
          f(thunk())
        } catch {
          case _: Throwable =>
            default
        }

      case IIO.RaiseError(_) =>
        default
    }

  def map[B](f: A => B): IIO[B] =
    this match {
      case IIO.RaiseError(e) =>
        cata(x => pure[B](f(x)), IIO.raiseError[B](e))

      case _ =>
        cata(x => pure[B](f(x)), IIO.raiseError[B](new Exception("Invalid map of an IIO.")))
    }

  def flatMap[B](f: A => IIO[B]): IIO[B] =
    this match {
      case IIO.RaiseError(e) =>
        cata(x => f(x), IIO.raiseError[B](e))

      case _ =>
        cata(x => f(x), IIO.raiseError[B](new Exception("Invalid flatMap of an IIO.")))
    }

  def flatten[B](implicit ev: A <:< IIO[B]): IIO[B] =
    this match {
      case IIO.RaiseError(e) =>
        cata(x => ev(x), IIO.raiseError[B](e))

      case _ =>
        cata(x => ev(x), IIO.raiseError[B](new Exception("Invalid flatten of an IIO.")))
    }

}

object IIO {

  implicit def eqIIO[A](implicit eq: Eq[A]): Eq[IIO[A]] =
    Eq.create[IIO[A]] { (a, b) =>
      areEqual(a, b)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def areEqual[A](a1: IIO[A], a2: IIO[A])(implicit eq: Eq[A]): Boolean =
    (a1, a2) match {
      case (IIO.Pure(a), IIO.Pure(b)) =>
        eq.equal(a, b)

      case (IIO.Delay(a), IIO.Delay(b)) =>
        eq.equal(a(), b())

      case (IIO.RaiseError(a), IIO.RaiseError(b)) =>
        a == b

      case _ =>
        false
    }

  final case class Pure[A](a: A)               extends IIO[A]
  final case class Delay[A](thunk: () => A)    extends IIO[A]
  final case class RaiseError[A](e: Throwable) extends IIO[A]

  def apply[A](a: => A): IIO[A] =
    pure(a)

  def pure[A](a: => A): IIO[A] =
    try {
      Pure(a)
    } catch {
      case e: Throwable =>
        RaiseError[A](e)
    }

  def delay[A](a: => A): IIO[A] =
    Delay(() => a)

  def raiseError[A](e: Throwable): IIO[A] =
    RaiseError[A](e)

}
