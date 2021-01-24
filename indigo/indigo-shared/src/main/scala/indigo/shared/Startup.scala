package indigo.shared

import indigo.shared.animation.Animation
import indigo.shared.datatypes.FontInfo
import indigo.shared.display.Shader

sealed trait Startup[+SuccessType] extends Product with Serializable {
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

  def additionalShaders: Set[Shader] =
    this match {
      case Startup.Failure(_) =>
        Set()

      case Startup.Success(_, _, _, s) =>
        s
    }

}

object Startup {

  final case class Failure(errors: List[String]) extends Startup[Nothing] {
    def report: String = errors.mkString("\n")
  }
  object Failure {
    def apply(errors: String*): Failure =
      Failure(errors.toList)
  }

  final case class Success[SuccessType](
      success: SuccessType,
      animations: Set[Animation],
      fonts: Set[FontInfo],
      shaders: Set[Shader],
  ) extends Startup[SuccessType] {
    def addAnimations(value: Animation*): Success[SuccessType] =
      addAnimations(value.toList)
    def addAnimations(value: List[Animation]): Success[SuccessType] =
      Success(success, animations ++ value, fonts, shaders)

    def addFonts(value: FontInfo*): Success[SuccessType] =
      addFonts(value.toList)
    def addFonts(value: List[FontInfo]): Success[SuccessType] =
      Success(success, animations, fonts ++ value, shaders)

    def addShaders(value: Shader*): Success[SuccessType] =
      addShaders(value.toList)
    def addShaders(value: List[Shader]): Success[SuccessType] =
      Success(success, animations, fonts, shaders ++ value)
  }
  object Success {
    def apply[SuccessType](success: SuccessType): Success[SuccessType] =
      Success(success, Set(), Set(), Set())
  }

}
