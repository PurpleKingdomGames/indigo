package indigo.gameengine

import indigo.gameengine.subsystems.SubSystem
import indigo.gameengine.scenegraph.animation.Animation
import indigo.gameengine.scenegraph.datatypes.FontInfo

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

  def additionalSubSystems: Set[SubSystem] =
    this match {
      case Startup.Failure(_) =>
        Set()

      case Startup.Success(_, _, _, s) =>
        s
    }

}

object Startup {

  final case class Failure[ErrorType](error: ErrorType)(implicit toReportable: ToReportable[ErrorType]) extends Startup[ErrorType, Nothing] {
    def report: String = toReportable.report(error)
  }
  final case class Success[SuccessType](success: SuccessType, animations: Set[Animation], fonts: Set[FontInfo], subSystems: Set[SubSystem]) extends Startup[Nothing, SuccessType] {
    def addAnimations(value: Animation*): Success[SuccessType] =
      addAnimations(value.toList)
    def addAnimations(value: List[Animation]): Success[SuccessType] =
      Success(success, animations ++ value, fonts, subSystems)

    def addFonts(value: FontInfo*): Success[SuccessType] =
      addFonts(value.toList)
    def addFonts(value: List[FontInfo]): Success[SuccessType] =
      Success(success, animations, fonts ++ value, subSystems)

    def addSubSystems(value: SubSystem*): Success[SuccessType] =
      addSubSystems(value.toList)
    def addSubSystems(value: List[SubSystem]): Success[SuccessType] =
      Success(success, animations, fonts, subSystems ++ value)
  }
  object Success {
    def apply[SuccessType](success: SuccessType): Success[SuccessType] =
      Success(success, Set(), Set(), Set())
  }

}
