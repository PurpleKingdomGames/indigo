package indigo.shared.formats

import indigo.shared.animation.*
import indigo.shared.assets.AssetName
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.*
import indigo.shared.dice.Dice
import indigo.shared.materials.Material
import indigo.shared.scenegraph.*
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

@SuppressWarnings(Array("scalafix:DisableSyntax.noValPatterns"))
class AsepriteTests extends munit.FunSuite {

  test("should be able to convert the loaded definition into a renderable Sprite object") {
    val SpriteAndAnimations(sprite, animation) =
      AsepriteSampleData.aseprite
        .toSpriteAndAnimations(Dice.loaded(0), AsepriteSampleData.imageAssetRef)
        .get

    assertEquals(sprite.bindingKey, AsepriteSampleData.sprite.bindingKey)
    assertEquals(sprite.animationKey, AsepriteSampleData.sprite.animationKey)

    assertEquals(animation.cycles.length, 1)
    assertEquals(animation.currentCycleLabel, CycleLabel("lights"))
    assertEquals(animation.cycles.find(c => c.label == animation.currentCycleLabel).get.frames.length, 3)
  }

  test("should be able to convert the loaded definition into a Map of Clip instances") {
    val actual: Option[Map[CycleLabel, Clip[Material.Bitmap]]] =
      AsepriteSampleData.aseprite
        .toClips(AsepriteSampleData.imageAssetRef)

    assert(clue(actual.isDefined))

    val clips: Map[CycleLabel, Clip[Material.Bitmap]] =
      actual.get

    assertEquals(clips.size, 1)
    assert(clue(clips.contains(CycleLabel("lights"))))

    val clip: Clip[Material.Bitmap] = clips.get(CycleLabel("lights")).get

    assertEquals(clip.sheet.frameCount, 3)
    assert(clue(clip.sheet.frameDuration ~== Millis(100).toSeconds))
    assertEquals(clip.sheet.arrangement, ClipSheetArrangement.Horizontal)
    assertEquals(clip.sheet.wrapAt, 2)
    assertEquals(clip.sheet.startOffset, 0)
    assertEquals(clip.playMode, ClipPlayMode.loop)
  }

}

object AsepriteSampleData {

  val imageAssetRef: AssetName = AssetName("trafficlights")

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

  val aseprite: Aseprite =
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

  val animationKey: AnimationKey = AnimationKey("0000000000000000")

  val animation: Animation =
    Animation(
      animationKey,
      currentCycleLabel = CycleLabel("lights"),
      cycles = NonEmptyList(
        Cycle.create(
          label = "lights",
          frames = NonEmptyList(
            Frame(
              crop = Rectangle(Point(0, 0), Size(64, 64)),
              duration = Millis(100)
            ),
            Frame(
              crop = Rectangle(Point(64, 0), Size(64, 64)),
              duration = Millis(100)
            ),
            Frame(
              crop = Rectangle(Point(0, 64), Size(64, 64)),
              duration = Millis(100)
            )
          )
        )
      )
    )

  val sprite: Sprite[?] =
    Sprite(
      bindingKey = BindingKey("0000000000000000"),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      animationKey = animationKey,
      ref = Point.zero,
      Function.const(None),
      Material.Bitmap(imageAssetRef)
    )

}
