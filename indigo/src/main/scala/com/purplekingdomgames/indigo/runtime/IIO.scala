package com.purplekingdomgames.indigo.runtime

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
        IIO.raiseError(e)
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
      Right(unsafeRun())
    } catch {
      case t: Throwable =>
        Left(t)
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
    cata(x => pure[B](f(x)), IIO.raiseError(new Exception("Invalid map of an IIO.")))

  def flatMap[B](f: A => IIO[B]): IIO[B] =
    cata(x => f(x), IIO.raiseError(new Exception("Invalid flatMap of an IIO.")))

  def flatten[B](implicit ev: A <:< IIO[B]): IIO[B] =
    cata(x => ev(x), IIO.raiseError(new Exception("Invalid flatten of an IIO.")))

  def eq[B >: A](other: IIO[B]): Boolean =
    (this, other) match {
      case (IIO.Pure(a), IIO.Pure(b)) =>
        a == b

      case (IIO.Delay(a), IIO.Delay(b)) =>
        a == b

      case (IIO.RaiseError(a), IIO.RaiseError(b)) =>
        a == b

      case _ =>
        false
    }

}

object IIO {

  case class Pure[A](a: A)            extends IIO[A]
  case class Delay[A](thunk: () => A) extends IIO[A]
  case class RaiseError(e: Throwable) extends IIO[Nothing]

  def apply[A](a: => A): IIO[A] =
    pure(a)

  def pure[A](a: => A): IIO[A] =
    Pure(a)

  def delay[A](a: => A): IIO[A] =
    Delay(() => a)

  def raiseError(e: Throwable): IIO[Nothing] =
    RaiseError(e)

}
