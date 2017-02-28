package com.purplekingdomgames.indigo.gameengine

import upickle.default._

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

  def fromJson(json: String): Option[Aseprite] = {
    try {
      Option(read[Aseprite](json))
    } catch {
      case e: Throwable =>
        println("Failed to deserialise json into Aseprite: " + e.getMessage)
        None
    }
  }

  private def extractFrames(frameTag: AsepriteFrameTag, asepriteFrames: List[AsepriteFrame]): List[Frame] = {
    asepriteFrames.slice(frameTag.from, frameTag.to).map { aseFrame =>
      Frame(
        bounds = Rectangle(
          position = Point(aseFrame.frame.x, aseFrame.frame.y),
          size = Point(aseFrame.frame.w, aseFrame.frame.h)
        )
      )
    }
  }

  private def extractCycles(aseprite: Aseprite): List[Cycle] = {
    aseprite.meta.frameTags.map { frameTag =>
      extractFrames(frameTag, aseprite.frames) match {
        case Nil =>
          println("Failed to extract cycle with frameTag: " + frameTag)
          None
        case x :: xs =>
          Option(
            Cycle(
              label = frameTag.name,
              frame = x.copy(current = true),
              frames = xs
            )
          )
      }
    }.collect { case Some(s) => s }
  }

  def toSprite(aseprite: Aseprite, depth: Depth, imageAssetRef: String): Option[Sprite] = {
    extractCycles(aseprite) match {
      case Nil =>
        println("No animation frames found in Aseprit: " + aseprite)
        None
      case x :: xs =>
        val animations: Animations =
          Animations(
            spriteSheetSize = Point(aseprite.meta.size.w, aseprite.meta.size.h),
            cycle = x,
            cycles = xs
          )
        Option(
          Sprite(
            bounds = Rectangle(
              position = Point(0, 0),
              size = Point(x.frame.bounds.size.x, x.frame.bounds.size.y)
            ),
            depth = depth,
            imageAssetRef = imageAssetRef,
            animations = animations
          )
        )
    }
  }

}

/*
import upickle.default._

write(1)                          ==> "1"

write(Seq(1, 2, 3))               ==> "[1,2,3]"

read[Seq[Int]]("[1, 2, 3]")       ==> List(1, 2, 3)

write((1, "omg", true))           ==> """[1,"omg",true]"""

type Tup = (Int, String, Boolean)

read[Tup]("""[1, "omg", true]""") ==> (1, "omg", true)
 */
