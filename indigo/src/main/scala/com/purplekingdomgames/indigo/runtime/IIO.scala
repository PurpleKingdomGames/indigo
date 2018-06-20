package com.purplekingdomgames.indigo.runtime


/**
  * IIO (IndigoIO) to differentiate from other IO monads
  *
  * @tparam A Type of the value being carried for evaluation
  */
sealed trait IIO[+A] {

  import IIO._

  def isError: Boolean =
    this match {
      case RaiseError(_) =>
        true

      case _ =>
        false
    }

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
      case Pure(a) =>
        f(a)

      case Delay(thunk) =>
        f(thunk())

      case RaiseError(_) =>
        default
    }

  def map[B](f: A => B): IIO[B] =
    this match {
      case Pure(a) =>
        pure(f(a))

      case Delay(thunk) =>
        try {
          delay(f(thunk()))
        } catch {
          case e: Throwable =>
            raiseError(e)
        }

      case RaiseError(e) =>
        raiseError(e)
    }

  def flatMap[B](f: A => IIO[B]): IIO[B] =
    this match {
      case Pure(a) =>
        f(a)

      case Delay(thunk) =>
        try {
          f(thunk())
        } catch {
          case e: Throwable =>
            raiseError(e)
        }

      case RaiseError(e) =>
        Option
        raiseError(e)
    }

  def flatten[B](implicit ev: A <:< IIO[B]): IIO[B] =
    cata(x => ev(x), raiseError(new Exception("Cannot flatten IIO in error state.")))

}

object IIO {

  case class Pure[A](a: A) extends IIO[A]
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