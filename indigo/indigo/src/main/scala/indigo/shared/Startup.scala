package indigo.shared

import indigo.shared.animation.Animation
import indigo.shared.collections.Batch
import indigo.shared.datatypes.FontInfo
import indigo.shared.shader.Shader

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

  def additionalShaders: Set[Shader] =
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

  final case class Success[SuccessType](
      success: SuccessType,
      animations: Set[Animation],
      fonts: Set[FontInfo],
      shaders: Set[Shader]
  ) extends Startup[SuccessType]
      derives CanEqual:
    def addAnimations(value: Animation*): Success[SuccessType] =
      addAnimations(Batch.fromSeq(value))
    def addAnimations(value: Batch[Animation]): Success[SuccessType] =
      Success(success, animations ++ value.toSet, fonts, shaders)

    def addFonts(value: FontInfo*): Success[SuccessType] =
      addFonts(Batch.fromSeq(value))
    def addFonts(value: Batch[FontInfo]): Success[SuccessType] =
      Success(success, animations, fonts ++ value.toSet, shaders)

    def addShaders(value: Shader*): Success[SuccessType] =
      addShaders(Batch.fromSeq(value))
    def addShaders(value: Batch[Shader]): Success[SuccessType] =
      Success(success, animations, fonts, shaders ++ value.toSet)

  object Success:
    def apply[SuccessType](success: SuccessType): Success[SuccessType] =
      Success(success, Set(), Set(), Set())
