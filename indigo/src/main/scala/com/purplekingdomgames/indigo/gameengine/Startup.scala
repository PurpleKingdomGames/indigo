package com.purplekingdomgames.indigo.gameengine

sealed trait Startup[+ErrorType, +SuccessType]
case class StartupFailure[ErrorType](error: ErrorType)(implicit toReportable: ToReportable[ErrorType]) extends Startup[ErrorType, Nothing] {
  def report: String = toReportable.report(error)
}
case class StartupSuccess[SuccessType](success: SuccessType) extends Startup[Nothing, SuccessType]

object Startup {
  implicit def toSuccess[T](v: T): StartupSuccess[T] = StartupSuccess(v)
  implicit def toFailure[T](v: T)(implicit toReportable: ToReportable[T]): StartupFailure[T] = StartupFailure(v)
}
