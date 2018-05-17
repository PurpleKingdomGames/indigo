package com.purplekingdomgames.indigoexts.formats

import com.purplekingdomgames.indigo.gameengine.events.GameEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.util.Logger
import io.circe.generic.auto._
import io.circe.parser._

case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta)

case class AsepriteFrame(filename: String,
                         frame: AsepriteRectangle,
                         rotated: Boolean,
                         trimmed: Boolean,
                         spriteSourceSize: AsepriteRectangle,
                         sourceSize: AsepriteSize,
                         duration: Int)

case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int)

case class AsepriteMeta(app: String,
                        version: String,
                        image: String,
                        format: String,
                        size: AsepriteSize,
                        scale: String,
                        frameTags: List[AsepriteFrameTag])

case class AsepriteSize(w: Int, h: Int)

case class AsepriteFrameTag(name: String, from: Int, to: Int, direction: String)

object AsepriteHelper {

  def fromJson(json: String): Option[Aseprite] =
    decode[Aseprite](json) match {
      case Right(s) => Some(s)
      case Left(e) =>
        Logger.info("Failed to deserialise json into Aseprite: " + e.getMessage)
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
            Logger.info("Failed to extract cycle with frameTag: " + frameTag)
            None
          case x :: xs =>
            Option(
              Cycle(
                label = frameTag.name,
                frame = x,
                frames = xs
              )
            )
        }
      }
      .collect { case Some(s) => s }

  def toSpriteAndAnimations(aseprite: Aseprite, depth: Depth, imageAssetRef: String): Option[SpriteAndAnimations] =
    extractCycles(aseprite) match {
      case Nil =>
        Logger.info("No animation frames found in Aseprit: " + aseprite)
        None
      case x :: xs =>
        val animations: Animations =
          Animations(
            animationsKey = AnimationsKey(BindingKey.generate.value),
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
                size = Point(x.frame.bounds.size.x, x.frame.bounds.size.y)
              ),
              depth = depth,
              animationsKey = animations.animationsKey,
              ref = Point(0, 0),
              effects = Effects.default,
              eventHandler = (_: (Rectangle, GameEvent)) => None
            ),
            animations
          )
        )
    }

}

case class SpriteAndAnimations(sprite: Sprite, animations: Animations)
