package indigo.shared

import indigo.Batch
import indigo.shared.animation.Animation
import indigo.shared.datatypes.FontInfo
import indigo.shared.shader.ShaderProgram

/** The Startup data type describes either a successful or failed start up sequence. It can hold a value, as well as new
  * shaders, animations and fonts to be added to Indigo's registers. A new Startup instance is created each time the
  * setup function is called, at least once, but also on dynamic asset load.
  */
sealed trait Startup[+SuccessType] extends Product with Serializable derives CanEqual:
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

  def additionalShaders: Set[ShaderProgram] =
    this match
      case Startup.Failure(_) =>
        Set()

      case Startup.Success(_, _, _, s) =>
        s

object Startup:

  final case class Failure(errors: Batch[String]) extends Startup[Nothing] derives CanEqual:
    def report: String = errors.mkString("\n")

  object Failure:
    def apply(errors: String*): Failure =
      Failure(Batch.fromSeq(errors))

    def apply(errors: List[String]): Failure =
      Failure(Batch.fromList(errors))

  final case class Success[SuccessType](
      success: SuccessType,
      animations: Set[Animation],
      fonts: Set[FontInfo],
      shaders: Set[ShaderProgram]
  ) extends Startup[SuccessType] derives CanEqual:
    def addAnimations(value: Animation*): Success[SuccessType] =
      addAnimations(value.toList)
    def addAnimations(value: List[Animation]): Success[SuccessType] =
      Success(success, animations ++ value, fonts, shaders)
    def addAnimations(value: Batch[Animation]): Success[SuccessType] =
      Success(success, animations ++ value.toSet, fonts, shaders)

    def addFonts(value: FontInfo*): Success[SuccessType] =
      addFonts(value.toList)
    def addFonts(value: List[FontInfo]): Success[SuccessType] =
      Success(success, animations, fonts ++ value, shaders)
    def addFonts(value: Batch[FontInfo]): Success[SuccessType] =
      Success(success, animations, fonts ++ value.toSet, shaders)

    def addShaders(value: ShaderProgram*): Success[SuccessType] =
      addShaders(value.toList)
    def addShaders(value: List[ShaderProgram]): Success[SuccessType] =
      Success(success, animations, fonts, shaders ++ value)
    def addShaders(value: Batch[ShaderProgram]): Success[SuccessType] =
      Success(success, animations, fonts, shaders ++ value.toSet)

  object Success:
    def apply[SuccessType](success: SuccessType): Success[SuccessType] =
      Success(success, Set(), Set(), Set())
