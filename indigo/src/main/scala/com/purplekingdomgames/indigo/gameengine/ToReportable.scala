package com.purplekingdomgames.indigo.gameengine

trait ToReportable[T] {
  def report(t: T): String
}

object ToReportable {
  def createToReportable[T](f: T => String): ToReportable[T] =
    new ToReportable[T] {
      def report(t: T): String = f(t)
    }
}
