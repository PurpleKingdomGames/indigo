package indigo.shared.formats

import indigo.shared.IndigoLogger
import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.Cycle
import indigo.shared.animation.CycleLabel
import indigo.shared.animation.Frame
import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.dice.Dice
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Clip
import indigo.shared.scenegraph.ClipPlayMode
import indigo.shared.scenegraph.ClipSheet
import indigo.shared.scenegraph.ClipSheetArrangement
import indigo.shared.scenegraph.Sprite
import indigo.shared.time.Millis

final case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta) derives CanEqual:
  def toSpriteAndAnimations(dice: Dice, assetName: AssetName): Option[SpriteAndAnimations] =
    Aseprite.toSpriteAndAnimations(this, dice, assetName)

  def toClips(assetName: AssetName): Option[Map[CycleLabel, Clip[Material.Bitmap]]] =
    Aseprite.toClips(this, assetName)

final case class AsepriteFrame(
    filename: String,
    frame: AsepriteRectangle,
    rotated: Boolean,
    trimmed: Boolean,
    spriteSourceSize: AsepriteRectangle,
    sourceSize: AsepriteSize,
    duration: Int
) derives CanEqual

final case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int) derives CanEqual:
  def position: Point = Point(x, y)
  def size: Size      = Size(w, h)

final case class AsepriteMeta(
    app: String,
    version: String,
    format: String,
    size: AsepriteSize,
    scale: String,
    frameTags: List[AsepriteFrameTag]
) derives CanEqual

final case class AsepriteSize(w: Int, h: Int) derives CanEqual:
  def toSize: Size = Size(w, h)

final case class AsepriteFrameTag(name: String, from: Int, to: Int, direction: String) derives CanEqual

final case class SpriteAndAnimations(sprite: Sprite[Material.Bitmap], animations: Animation) derives CanEqual:
  def modifySprite(alter: Sprite[Material.Bitmap] => Sprite[Material.Bitmap]): SpriteAndAnimations =
    this.copy(sprite = alter(sprite))

object Aseprite:

  def toSpriteAndAnimations(aseprite: Aseprite, dice: Dice, assetName: AssetName): Option[SpriteAndAnimations] =
    extractCycles(aseprite) match {
      case Nil =>
        IndigoLogger.info("No animation frames found in Aseprite")
        None
      case x :: xs =>
        val animations: Animation =
          Animation(
            animationKey = AnimationKey.fromDice(dice),
            currentCycleLabel = x.label,
            cycles = NonEmptyList.pure(x, xs)
          )
        Option(
          SpriteAndAnimations(
            Sprite(
              bindingKey = BindingKey.fromDice(dice),
              material = Material.Bitmap(assetName),
              animationKey = animations.animationKey,
              animationActions = Batch.empty,
              eventHandlerEnabled = false,
              eventHandler = Function.const(None),
              position = Point(0, 0),
              rotation = Radians.zero,
              scale = Vector2.one,
              ref = Point(0, 0),
              flip = Flip.default
            ),
            animations
          )
        )
    }

  def toClips(aseprite: Aseprite, assetName: AssetName): Option[Map[CycleLabel, Clip[Material.Bitmap]]] =
    extractClipData(aseprite)
      .map(
        _.map { clipData =>
          clipData.label ->
            Clip(
              size = clipData.size,
              sheet = clipData.sheet,
              playMode = ClipPlayMode.default,
              material = Material.Bitmap(assetName),
              eventHandlerEnabled = false,
              eventHandler = Function.const(None),
              position = Point.zero,
              rotation = Radians.zero,
              scale = Vector2.one,
              ref = Point.zero,
              flip = Flip.default
            )
        }
      )
      .map(_.toMap)

  private def extractCycles(aseprite: Aseprite): List[Cycle] =
    aseprite.meta.frameTags
      .map { frameTag =>
        extractFrames(frameTag, aseprite.frames) match {
          case Nil =>
            IndigoLogger.info(s"Failed to extract cycle with frameTag: ${frameTag.toString()}")
            None
          case x :: xs =>
            Option(
              Cycle.create(frameTag.name, NonEmptyList.pure(x, xs))
            )
        }
      }
      .collect { case Some(s) => s }

  private def extractFrames(frameTag: AsepriteFrameTag, asepriteFrames: List[AsepriteFrame]): List[Frame] =
    asepriteFrames.slice(frameTag.from, frameTag.to + 1).map { aseFrame =>
      Frame(
        crop = Rectangle(
          position = Point(aseFrame.frame.x, aseFrame.frame.y),
          size = Size(aseFrame.frame.w, aseFrame.frame.h)
        ),
        duration = Millis(aseFrame.duration.toLong)
      )
    }

  private def extractClipData(aseprite: Aseprite): Option[List[ClipData]] =
    aseprite.frames match
      case f :: Nil =>
        Option(
          aseprite.meta.frameTags
            .map { frameTag =>
              val sheet =
                ClipSheet(
                  frameCount = (frameTag.to - frameTag.from) + 1,
                  frameDuration = Millis(f.duration.toLong).toSeconds,
                  wrapAt = 1,
                  arrangement = ClipSheetArrangement.Horizontal,
                  startOffset = frameTag.from
                )

              ClipData(CycleLabel(frameTag.name), f.frame.size, sheet)
            }
        )

      case f1 :: f2 :: _ =>
        val arrangement: ClipSheetArrangement =
          if f2.frame.x > f1.frame.x then ClipSheetArrangement.Horizontal
          else ClipSheetArrangement.Vertical

        val wrapAt: Int =
          arrangement match
            case ClipSheetArrangement.Horizontal =>
              aseprite.meta.size.toSize.width / f1.frame.w

            case ClipSheetArrangement.Vertical =>
              aseprite.meta.size.toSize.height / f1.frame.h

        Option(
          aseprite.meta.frameTags
            .map { frameTag =>
              val sheet =
                ClipSheet(
                  frameCount = (frameTag.to - frameTag.from) + 1,
                  frameDuration = Millis(f1.duration.toLong).toSeconds,
                  wrapAt = wrapAt,
                  arrangement = arrangement,
                  startOffset = frameTag.from
                )

              ClipData(CycleLabel(frameTag.name), f1.frame.size, sheet)
            }
        )

      case Nil =>
        IndigoLogger.info(s"No frames were found during Aseprite converstion to Clips")
        None

final case class ClipData(label: CycleLabel, size: Size, sheet: ClipSheet)
