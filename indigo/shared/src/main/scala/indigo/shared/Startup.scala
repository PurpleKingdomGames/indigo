package indigo.shared

import indigo.shared.animation.Animation
import indigo.shared.datatypes.FontInfo
import indigo.shared.events.GlobalEvent

sealed trait Startup[+ErrorType, +SuccessType] extends Product with Serializable {
  def additionalAnimations: Set[Animation] =
    this match {
      case Startup.Failure(_) =>
        Set()

      case Startup.Success(_, a, _, _) =>
        a
    }

  def additionalFonts: Set[FontInfo] =
    this match {
      case Startup.Failure(_) =>
        Set()

      case Startup.Success(_, _, f, _) =>
        f
    }

  def startUpEvents: List[GlobalEvent] =
    this match {
      case Startup.Failure(_) =>
        Nil

      case Startup.Success(_, _, _, events) =>
        events
    }

}

object Startup {

  final case class Failure[ErrorType](error: ErrorType)(implicit toReportable: ToReportable[ErrorType]) extends Startup[ErrorType, Nothing] {
    def report: String = toReportable.report(error)
  }
  final case class Success[SuccessType](
      success: SuccessType,
      animations: Set[Animation],
      fonts: Set[FontInfo],
      globalEvents: List[GlobalEvent]
  ) extends Startup[Nothing, SuccessType] {
    def addAnimations(value: Animation*): Success[SuccessType] =
      addAnimations(value.toList)
    def addAnimations(value: List[Animation]): Success[SuccessType] =
      Success(success, animations ++ value, fonts, globalEvents)

    def addFonts(value: FontInfo*): Success[SuccessType] =
      addFonts(value.toList)
    def addFonts(value: List[FontInfo]): Success[SuccessType] =
      Success(success, animations, fonts ++ value, globalEvents)

    def addGlobalEvents(events: GlobalEvent*): Success[SuccessType] =
      addGlobalEvents(events.toList)
    def addGlobalEvents(events: List[GlobalEvent]): Success[SuccessType] =
      Success(success, animations, fonts, globalEvents ++ events)
  }
  object Success {
    def apply[SuccessType](success: SuccessType): Success[SuccessType] =
      Success(success, Set(), Set(), Nil)
  }

}
