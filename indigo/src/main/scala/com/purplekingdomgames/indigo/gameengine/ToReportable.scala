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

case class StartupErrors(errors: List[String])
object StartupErrors {

  implicit val stringToReportable: ToReportable[StartupErrors] =
    ToReportable.createToReportable(_.errors.mkString("\n"))

  def apply(errors: String*): StartupErrors = StartupErrors(errors.toList)
}