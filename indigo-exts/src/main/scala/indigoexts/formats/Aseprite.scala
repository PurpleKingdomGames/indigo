package indigoexts.formats

import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.animation._
import indigo.gameengine.scenegraph.datatypes._
import indigo.runtime.IndigoLogger
import indigo.collections.NonEmptyList
import indigo.AsString
import io.circe.generic.auto._
import io.circe.parser._

final case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta)

final case class AsepriteFrame(filename: String, frame: AsepriteRectangle, rotated: Boolean, trimmed: Boolean, spriteSourceSize: AsepriteRectangle, sourceSize: AsepriteSize, duration: Int)

final case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int)

final case class AsepriteMeta(app: String, version: String, image: String, format: String, size: AsepriteSize, scale: String, frameTags: List[AsepriteFrameTag])

final case class AsepriteSize(w: Int, h: Int)

final case class AsepriteFrameTag(name: String, from: Int, to: Int, direction: String)
object AsepriteFrameTag {
  implicit val show: AsString[AsepriteFrameTag] =
    AsString.create { ft =>
      s"""FrameTag(${ft.name}, ${ft.from.toString}, ${ft.to.toString}, ${ft.direction})"""
    }
}

object Aseprite {

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference", "org.wartremover.warts.Nothing"))
  def fromJson(json: String): Option[Aseprite] =
    decode[Aseprite](json) match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into Aseprite: " + e.getMessage)
        None
    }

  private def extractFrames(frameTag: AsepriteFrameTag, asepriteFrames: List[AsepriteFrame]): List[Frame] =
    asepriteFrames.slice(frameTag.from, frameTag.to + 1).map { aseFrame =>
      Frame(
        bounds = Rectangle(
          position = Point(aseFrame.frame.x, aseFrame.frame.y),
          size = Point(aseFrame.frame.w, aseFrame.frame.h)
        ),
        duration = aseFrame.duration
      )
    }

  private def extractCycles(aseprite: Aseprite): List[Cycle] =
    aseprite.meta.frameTags
      .map { frameTag =>
        extractFrames(frameTag, aseprite.frames) match {
          case Nil =>
            IndigoLogger.info("Failed to extract cycle with frameTag: " + implicitly[AsString[AsepriteFrameTag]].show(frameTag))
            None
          case x :: xs =>
            Option(
              Cycle.create(frameTag.name, NonEmptyList(x, xs))
            )
        }
      }
      .collect { case Some(s) => s }

  @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
  def toSpriteAndAnimations(aseprite: Aseprite, depth: Depth, imageAssetRef: String): Option[SpriteAndAnimations] =
    extractCycles(aseprite) match {
      case Nil =>
        IndigoLogger.info("No animation frames found in Aseprite")
        None
      case x :: xs =>
        val animations: Animation =
          Animation(
            animationsKey = AnimationKey(BindingKey.generate.value),
            imageAssetRef = imageAssetRef,
            spriteSheetSize = Point(aseprite.meta.size.w, aseprite.meta.size.h),
            currentCycleLabel = x.label,
            cycle = x,
            cycles = xs.foldLeft(Map.empty[CycleLabel, Cycle])((a, b) => a ++ Map(b.label -> b)),
            actions = Nil
          )
        Option(
          SpriteAndAnimations(
            Sprite(
              bindingKey = BindingKey.generate,
              bounds = Rectangle(
                position = Point(0, 0),
                size = Point(x.frames.head.bounds.size.x, x.frames.head.bounds.size.y)
              ),
              depth = depth,
              animationsKey = animations.animationsKey,
              ref = Point(0, 0),
              effects = Effects.default,
              eventHandler = (_: (Rectangle, GlobalEvent)) => None
            ),
            animations
          )
        )
    }

}

final case class SpriteAndAnimations(sprite: Sprite, animations: Animation)
