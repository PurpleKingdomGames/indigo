package indigo.shared.formats

import indigo.shared.scenegraph.Sprite
import indigo.shared.animation.Animation
import indigo.shared.datatypes.Depth
import indigo.shared.assets.AssetName
import indigo.shared.dice.Dice
import indigo.shared.IndigoLogger
import indigo.shared.animation.AnimationKey
import indigo.shared.datatypes.Material
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Effects
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.animation.Cycle
import indigo.shared.time.Millis
import indigo.shared.animation.Frame

final case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta) {

  def toSpriteAndAnimations(dice: Dice, assetName: AssetName): Option[SpriteAndAnimations] =
    Aseprite.toSpriteAndAnimations(this, dice, assetName)

}

final case class AsepriteFrame(filename: String, frame: AsepriteRectangle, rotated: Boolean, trimmed: Boolean, spriteSourceSize: AsepriteRectangle, sourceSize: AsepriteSize, duration: Int)

final case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int)

final case class AsepriteMeta(app: String, version: String, image: String, format: String, size: AsepriteSize, scale: String, frameTags: List[AsepriteFrameTag])

final case class AsepriteSize(w: Int, h: Int)

final case class AsepriteFrameTag(name: String, from: Int, to: Int, direction: String)

final case class SpriteAndAnimations(sprite: Sprite, animations: Animation)
object Aseprite {

  def toSpriteAndAnimations(aseprite: Aseprite, dice: Dice, assetName: AssetName): Option[SpriteAndAnimations] =
    extractCycles(aseprite) match {
      case Nil =>
        IndigoLogger.info("No animation frames found in Aseprite")
        None
      case x :: xs =>
        val animations: Animation =
          Animation(
            animationKey = AnimationKey.fromDice(dice),
            material = Material.Basic(assetName, 1.0),
            currentCycleLabel = x.label,
            cycles = NonEmptyList.pure(x, xs)
          )
        Option(
          SpriteAndAnimations(
            Sprite(
              bindingKey = BindingKey.fromDice(dice),
              position = Point(0, 0),
              depth = Depth(1),
              rotation = Radians.zero,
              scale = Vector2.one,
              animationKey = animations.animationKey,
              ref = Point(0, 0),
              effects = Effects.default,
              eventHandler = (_: (Rectangle, GlobalEvent)) => Nil
            ),
            animations
          )
        )
    }

  def extractCycles(aseprite: Aseprite): List[Cycle] =
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
          size = Point(aseFrame.frame.w, aseFrame.frame.h)
        ),
        duration = Millis(aseFrame.duration.toLong)
      )
    }

}
