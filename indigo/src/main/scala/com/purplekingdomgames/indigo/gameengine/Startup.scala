package com.purplekingdomgames.indigo.gameengine

sealed trait Startup[+ErrorType, +SuccessType]

object Startup {
  case class Failure[ErrorType](error: ErrorType)(implicit toReportable: ToReportable[ErrorType])
      extends Startup[ErrorType, Nothing] {
    def report: String = toReportable.report(error)
  }
  case class Success[SuccessType](success: SuccessType) extends Startup[Nothing, SuccessType]

  implicit def toSuccess[T](v: T): Success[T]                                         = Success(v)
  implicit def toFailure[T](v: T)(implicit toReportable: ToReportable[T]): Failure[T] = Failure(v)

  def fromEither[A, B](either: Either[A, B])(implicit toReportable: ToReportable[A]): Startup[A, B] =
    either match {
      case Left(e)  => Failure(e)
      case Right(s) => Success(s)
    }
}
