package com.purplekingdomgames.indigo.gameengine

import org.scalatest.{FunSpec, Matchers}

class AsepriteHelperSpec extends FunSpec with Matchers {

  describe("Create an Aseprite asset") {

    it("should be able to parse the json definition") {
      AsepriteHelper.fromJson(AsepriteSampleData.json) shouldEqual AsepriteSampleData.aseprite
    }

    it("should be able to convert the loaded definition into a renderable Sprite object") {
      AsepriteSampleData.aseprite.flatMap { as =>
        AsepriteHelper.toSprite(as, AsepriteSampleData.depth, AsepriteSampleData.imageAssetRef)
      } shouldEqual AsepriteSampleData.sprite
    }

  }

}

object AsepriteSampleData {

  val depth: Depth = Depth(1)

  val imageAssetRef: String = "trafficlights"

  val json: String =
    """
      |{ "frames": [
      |   {
      |    "filename": "trafficlights 0.ase",
      |    "frame": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "rotated": false,
      |    "trimmed": false,
      |    "spriteSourceSize": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "sourceSize": { "w": 64, "h": 64 },
      |    "duration": 100
      |   },
      |   {
      |    "filename": "trafficlights 1.ase",
      |    "frame": { "x": 64, "y": 0, "w": 64, "h": 64 },
      |    "rotated": false,
      |    "trimmed": false,
      |    "spriteSourceSize": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "sourceSize": { "w": 64, "h": 64 },
      |    "duration": 100
      |   },
      |   {
      |    "filename": "trafficlights 2.ase",
      |    "frame": { "x": 0, "y": 64, "w": 64, "h": 64 },
      |    "rotated": false,
      |    "trimmed": false,
      |    "spriteSourceSize": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "sourceSize": { "w": 64, "h": 64 },
      |    "duration": 100
      |   }
      | ],
      | "meta": {
      |  "app": "http://www.aseprite.org/",
      |  "version": "1.1.13",
      |  "image": "/Users/dave/repos/indigo/sandbox/trafficlights.png",
      |  "format": "RGBA8888",
      |  "size": { "w": 128, "h": 128 },
      |  "scale": "1",
      |  "frameTags": [
      |   { "name": "lights", "from": 0, "to": 2, "direction": "forward" }
      |  ]
      | }
      |}
    """.stripMargin

  val aseprite: Option[Aseprite] = Option {
    Aseprite(
      frames = List(
        AsepriteFrame(
          filename = "trafficlights 0.ase",
          frame = AsepriteRectangle(0, 0, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        ),
        AsepriteFrame(
          filename = "trafficlights 1.ase",
          frame = AsepriteRectangle(64, 0, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        ),
        AsepriteFrame(
          filename = "trafficlights 2.ase",
          frame = AsepriteRectangle(0, 64, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        )
      ),
      meta = AsepriteMeta(
        app = "http://www.aseprite.org/",
        version = "1.1.13",
        image = "/Users/dave/repos/indigo/sandbox/trafficlights.png",
        format = "RGBA8888",
        size = AsepriteSize(128, 128),
        scale = "1",
        frameTags = List(
          AsepriteFrameTag(
            name = "lights",
            from = 0,
            to = 2,
            direction = "forward"
          )
        )
      )
    )
  }

  val sprite: Option[Sprite] = Option {

    Sprite(
      bounds = Rectangle(Point(0, 0), Point(64, 64)),
      depth = depth,
      imageAssetRef = imageAssetRef,
      animations = Animations(
        spriteSheetSize = Point(128, 128),
        cycle = Cycle(
          label = "lights",
          frame = Frame(
            bounds = Rectangle(Point(0, 0), Point(64, 64)),
            current = true
          ),
          frames = List(
            Frame(
              bounds = Rectangle(Point(64, 0), Point(64, 64)),
              current = false
            ),
            Frame(
              bounds = Rectangle(Point(0, 64), Point(64, 64)),
              current = false
            )
          )
        ),
        cycles = Nil
      ),
      ref = None,
      effects = Effects.default
    )

  }

}