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
