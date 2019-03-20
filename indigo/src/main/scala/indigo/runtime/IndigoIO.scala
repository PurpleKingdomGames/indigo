package indigo.runtime

import indigo.shared.EqualTo

/**
  * IIO (IndigoIO) to differentiate from other IO monads
  *
  * @tparam A Type of the value being carried for evaluation
  */
sealed trait IndigoIO[+A] {

  def pure[B](b: => B): IndigoIO[B] =
    this match {
      case IndigoIO.Pure(_) =>
        IndigoIO.pure(b)

      case IndigoIO.Delay(_) =>
        IndigoIO.delay(b)

      case IndigoIO.RaiseError(e) =>
        IndigoIO.raiseError[B](e)
    }

  def isError: Boolean =
    this match {
      case IndigoIO.RaiseError(_) =>
        true

      case _ =>
        false
    }

  def recover[B >: A](default: IndigoIO[B]): IndigoIO[B] =
    cata(a => this.pure(a), default)

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeRun(): A =
    this match {
      case IndigoIO.Pure(a) =>
        a

      case IndigoIO.Delay(thunk) =>
        thunk()

      case IndigoIO.RaiseError(e) =>
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
      case IndigoIO.Pure(a) =>
        f(a)

      case IndigoIO.Delay(thunk) =>
        try {
          f(thunk())
        } catch {
          case _: Throwable =>
            default
        }

      case IndigoIO.RaiseError(_) =>
        default
    }

  def map[B](f: A => B): IndigoIO[B] =
    this match {
      case IndigoIO.RaiseError(e) =>
        cata(x => pure[B](f(x)), IndigoIO.raiseError[B](e))

      case _ =>
        cata(x => pure[B](f(x)), IndigoIO.raiseError[B](new Exception("Invalid map of an IIO.")))
    }

  def flatMap[B](f: A => IndigoIO[B]): IndigoIO[B] =
    this match {
      case IndigoIO.RaiseError(e) =>
        cata(x => f(x), IndigoIO.raiseError[B](e))

      case _ =>
        cata(x => f(x), IndigoIO.raiseError[B](new Exception("Invalid flatMap of an IIO.")))
    }

  def flatten[B](implicit ev: A <:< IndigoIO[B]): IndigoIO[B] =
    this match {
      case IndigoIO.RaiseError(e) =>
        cata(x => ev(x), IndigoIO.raiseError[B](e))

      case _ =>
        cata(x => ev(x), IndigoIO.raiseError[B](new Exception("Invalid flatten of an IIO.")))
    }

}

object IndigoIO {

  implicit def eqIIO[A](implicit eq: EqualTo[A]): EqualTo[IndigoIO[A]] =
    EqualTo.create[IndigoIO[A]] { (a, b) =>
      areEqual(a, b)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def areEqual[A](a1: IndigoIO[A], a2: IndigoIO[A])(implicit eq: EqualTo[A]): Boolean =
    (a1, a2) match {
      case (IndigoIO.Pure(a), IndigoIO.Pure(b)) =>
        eq.equal(a, b)

      case (IndigoIO.Delay(a), IndigoIO.Delay(b)) =>
        eq.equal(a(), b())

      case (IndigoIO.RaiseError(a), IndigoIO.RaiseError(b)) =>
        a == b

      case _ =>
        false
    }

  final case class Pure[A](a: A)               extends IndigoIO[A]
  final case class Delay[A](thunk: () => A)    extends IndigoIO[A]
  final case class RaiseError[A](e: Throwable) extends IndigoIO[A]

  def apply[A](a: => A): IndigoIO[A] =
    pure(a)

  def pure[A](a: => A): IndigoIO[A] =
    try {
      Pure(a)
    } catch {
      case e: Throwable =>
        RaiseError[A](e)
    }

  def delay[A](a: => A): IndigoIO[A] =
    Delay(() => a)

  def raiseError[A](e: Throwable): IndigoIO[A] =
    RaiseError[A](e)

}
