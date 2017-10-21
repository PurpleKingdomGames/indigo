package com.purplekingdomgames.indigo.gameengine

sealed trait Startup[+StartupErrorType, +StartupSuccessType]
case class StartupFailure[StartupErrorType](error: StartupErrorType)(implicit toReportable: ToReportable[StartupErrorType]) extends Startup[StartupErrorType, Nothing] {
  def report: String = toReportable.report(error)
}
case class StartupSuccess[StartupSuccessType](success: StartupSuccessType) extends Startup[Nothing, StartupSuccessType]

object Startup {
  implicit def toSuccess[T](v: T): StartupSuccess[T] = StartupSuccess[T](v)
  implicit def toFailure[T](v: T)(implicit toReportable: ToReportable[T]): StartupFailure[T] = StartupFailure[T](v)
}
