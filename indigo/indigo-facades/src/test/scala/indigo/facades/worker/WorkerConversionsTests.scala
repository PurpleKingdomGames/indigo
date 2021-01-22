package indigo.facades.worker

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.{FontChar, FontInfo, FontKey}
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName

class WorkerConversionsTests extends munit.FunSuite {

  // FontInfo

  test("fontinfo") {
    val material = Material.Textured(AssetName("font-sheet"))

    val chars = List(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 16, 16),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey  = FontKey("test1")
    val fontInfo = FontInfo(fontKey, material, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    val expected =
      fontInfo

    val actual =
      FontInfoConversion.fromJS(FontInfoConversion.toJS(expected))

    assertEquals(actual, expected)
  }

  // Animation

  test("animation") {
    val expected =
      AnimationSample.animation

    val actual =
      AnimationConversion.fromJS(AnimationConversion.toJS(expected))

    assertEquals(actual, expected)
  }

  // Primitives

  test("point") {
    val expected =
      Point(20, 30)

    val actual =
      PointConversion.fromJS(PointConversion.toJS(expected))

    assertEquals(actual, expected)
  }

  test("vector2") {
    val expected =
      Vector2(20, 30)

    val actual =
      Vector2Conversion.fromJS(Vector2Conversion.toJS(expected))

    assertEquals(actual, expected)
  }

  test("rectangle") {
    val expected =
      Rectangle(Point(20, 30), Point(50, 100))

    val actual =
      RectangleConversion.fromJS(RectangleConversion.toJS(expected))

    assertEquals(actual, expected)
  }

  // SceneFrameData

  import indigo.shared.platform.SceneFrameData
  import indigo.shared.datatypes.mutable.CheapMatrix4
  import indigo.shared.platform.AssetMapping
  import indigo.shared.platform.TextureRefAndOffset
  import indigo.shared.time.GameTime
  import indigo.shared.time.Seconds
  import indigo.shared.scenegraph.SceneUpdateFragment

  import scalajs.js.JSConverters._

  test("scene frame data") {

    val scene =
      SceneUpdateFragment.empty

    val expected =
      SceneFrameData(
        gameTime = GameTime(Seconds(1), Seconds(0.1), GameTime.FPS(30)),
        scene = scene,
        assetMapping = AssetMapping(Map("texture1" -> TextureRefAndOffset("atlas1", Vector2(1024, 1024), Point(32, 32)))),
        screenWidth = 320,
        screenHeight = 200,
        orthographicProjectionMatrix = CheapMatrix4.orthographic(320, 200).mat.toJSArray
      )

    val actual =
      SceneFrameDataConversion.fromJS(SceneFrameDataConversion.toJS(expected))

    assertEquals(actual, expected)
  }

}

import indigo.shared.animation.Frame
import indigo.shared.time.Millis
import indigo.shared.animation.Cycle
import indigo.shared.animation.CycleLabel
import indigo.shared.collections.NonEmptyList
import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationKey

object AnimationSample {

  val frame1: Frame =
    Frame(Rectangle(Point(0, 0), Point(10, 10)), Millis(100))

  val frame2: Frame =
    Frame(Rectangle(0, 0, 20, 10), Millis(100))

  val frame3: Frame =
    Frame(Rectangle(0, 0, 30, 10), Millis(100))

  val frame4: Frame =
    Frame(Rectangle(0, 0, 40, 10), Millis(100))

  val frame5: Frame =
    Frame(Rectangle(0, 0, 50, 10), Millis(100))

  val frame6: Frame =
    Frame(Rectangle(0, 0, 60, 10), Millis(100))

  val cycleLabel1: CycleLabel =
    CycleLabel("cycle 1")

  val cycleLabel2: CycleLabel =
    CycleLabel("cycle 2")

  val cycle1: Cycle =
    Cycle.create(cycleLabel1.value, NonEmptyList(frame1, frame2, frame3))

  val cycle2: Cycle =
    Cycle.create(cycleLabel2.value, NonEmptyList(frame4, frame5, frame6))

  val cycles: NonEmptyList[Cycle] =
    NonEmptyList(cycle1, cycle2)

  val key: AnimationKey =
    AnimationKey("test anim")

  val animation: Animation =
    Animation(
      key,
      Material.Textured(AssetName("imageAssetRef")),
      cycles.head.label,
      cycles
    )

}
