package com.purplekingdomgames.indigo.gameengine

sealed trait Startup[+Error, +Success]
case class StartupFailure[Error](error: Error)(implicit toReportable: ToReportable[Error]) extends Startup[Error, Nothing] {
  def report: String = toReportable.report(error)
}
case class StartupSuccess[Success](success: Success) extends Startup[Nothing, Success]

object Startup {
  implicit def toSuccess[T](v: T): StartupSuccess[T] = StartupSuccess(v)
  implicit def toFailure[T](v: T)(implicit toReportable: ToReportable[T]): StartupFailure[T] = StartupFailure(v)
}
