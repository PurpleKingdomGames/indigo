package com.purplekingdomgames.indigo.runtime


/**
  * IIO (IndigoIO) to differentiate from other IO monads
  *
  * @tparam A Type of the value being carried for evaluation
  */
sealed trait IIO[+A] {

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

//  def cata[B](f: IIO[A] => B, default: B): B =
//    ???
//
//  def map[B](iIO: IIO[A])(f: A => IIO[B]): IIO[B] =
//    ???

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

//  def cata[A, B](iIO: IIO[A])(f: IIO[A] => B, default: B): B =

}