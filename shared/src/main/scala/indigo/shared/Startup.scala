package indigo.shared

import indigo.shared.animation.Animation
import indigo.shared.datatypes.FontInfo

sealed trait Startup[+ErrorType, +SuccessType] extends Product with Serializable {
  def additionalAnimations: Set[Animation] =
    this match {
      case Startup.Failure(_) =>
        Set()

      case Startup.Success(_, a, _) =>
        a
    }

  def additionalFonts: Set[FontInfo] =
    this match {
      case Startup.Failure(_) =>
        Set()

      case Startup.Success(_, _, f) =>
        f
    }

}

object Startup {

  final case class Failure[ErrorType](error: ErrorType)(implicit toReportable: ToReportable[ErrorType]) extends Startup[ErrorType, Nothing] {
    def report: String = toReportable.report(error)
  }
  final case class Success[SuccessType](success: SuccessType, animations: Set[Animation], fonts: Set[FontInfo]) extends Startup[Nothing, SuccessType] {
    def addAnimations(value: Animation*): Success[SuccessType] =
      addAnimations(value.toList)
    def addAnimations(value: List[Animation]): Success[SuccessType] =
      Success(success, animations ++ value, fonts)

    def addFonts(value: FontInfo*): Success[SuccessType] =
      addFonts(value.toList)
    def addFonts(value: List[FontInfo]): Success[SuccessType] =
      Success(success, animations, fonts ++ value)
  }
  object Success {
    def apply[SuccessType](success: SuccessType): Success[SuccessType] =
      Success(success, Set(), Set())
  }

}
