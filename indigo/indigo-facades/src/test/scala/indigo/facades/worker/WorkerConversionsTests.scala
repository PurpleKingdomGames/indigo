package indigo.facades.worker

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.{FontChar, FontInfo, FontKey}
import indigo.shared.datatypes.Material
import indigo.shared.datatypes.Depth
import indigo.shared.assets.AssetName
import indigo.shared.animation.Frame
import indigo.shared.time.Millis
import indigo.shared.animation.Cycle
import indigo.shared.animation.CycleLabel
import indigo.shared.collections.NonEmptyList
import indigo.shared.animation.Animation
import indigo.shared.animation.AnimationKey
import indigo.shared.platform.SceneFrameData
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.platform.AssetMapping
import indigo.shared.platform.TextureRefAndOffset
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.scenegraph.PointLight
import indigo.shared.scenegraph.CloneBlank
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Sprite
import indigo.shared.animation.AnimationKey
import indigo.shared.datatypes.BindingKey
import indigo.shared.scenegraph.SceneAudio
import indigo.shared.scenegraph.SceneAudioSource
import indigo.shared.scenegraph.PlaybackPattern
import indigo.shared.audio.Volume
import indigo.shared.audio.Track
import indigo.shared.scenegraph.Clone
import indigo.shared.scenegraph.CloneId
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.CloneTransformData

import scalajs.js.JSConverters._

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

  // Text

  test("text") {
    val expected =
      Text("Hello, World!", FontKey("my font"))

    val actual =
      TextConversion.fromJS(TextConversion.toJS(expected))

    assertEquals(actual.position, expected.position)
    assertEquals(actual.rotation, expected.rotation)
    assertEquals(actual.scale, expected.scale)
    assertEquals(actual.depth, expected.depth)
    assertEquals(actual.ref, expected.ref)
    assertEquals(actual.flip, expected.flip)
    assertEquals(actual.text, expected.text)
    assertEquals(actual.alignment, expected.alignment)
    assertEquals(actual.fontKey, expected.fontKey)
    assertEquals(actual.effects, expected.effects)
  }

  // Sprite

  test("sprite") {
    val expected =
      Sprite(BindingKey("binding key 1"), AnimationKey("anim key 1"))

    val actual =
      SpriteConversion.fromJS(SpriteConversion.toJS(expected))

    assertEquals(actual.position, expected.position)
    assertEquals(actual.rotation, expected.rotation)
    assertEquals(actual.scale, expected.scale)
    assertEquals(actual.depth, expected.depth)
    assertEquals(actual.ref, expected.ref)
    assertEquals(actual.flip, expected.flip)
    assertEquals(actual.animationActions, expected.animationActions)
    assertEquals(actual.animationKey, expected.animationKey)
    assertEquals(actual.effects, expected.effects)
  }

  // SceneFrameData

  test("scene frame data") {

    val scene =
      SceneUpdateFragment.empty
        .addGameLayerNodes(
          Group(
            Group(
              Graphic(64, 64, Material.Lit(AssetName("albedo"), AssetName("emmisive")))
                .withCrop(10, 10, 20, 20)
            ).moveBy(10, 10)
          )
        )
        .addLightingLayerNodes(
          Clone(CloneId("light clone"), Depth(10), CloneTransformData.startAt(Point(20, 20))),
          CloneBatch(
            CloneId("light batch"), 
            Depth(100), 
            CloneTransformData.startAt(Point(1, 1)).withRotation(Radians(0.2)), 
            List(CloneTransformData.startAt(Point(1, 12))),
            Some(BindingKey("static key"))
          )
        )
        .addUiLayerNodes(
          Graphic(64, 64, Material.Lit(AssetName("albedo"), AssetName("emmisive")))
            .withCrop(10, 10, 20, 20)
            .rotateTo(Radians(10.0))
            .scaleBy(2.0, 1.5)
        )
        .addLights(PointLight(Point(10, 10), 10, RGB.Red, 1.2, 30))
        .withAmbientLightAmount(0.5)
        .addCloneBlanks(
          CloneBlank(
            CloneId("test clone blank"),
            Graphic(64, 64, Material.Lit(AssetName("albedo"), AssetName("emmisive")))
          )
        )
        .withAudio(
          SceneAudio(
            SceneAudioSource(
              BindingKey("audio"),
              PlaybackPattern.SingleTrackLoop(Track(AssetName("track"), Volume(0.75))),
              Volume(0.2)
            )
          )
        )
        .withColorOverlay(RGBA.Green.withAmount(0.2))

    val expected =
      SceneFrameData(
        gameTime = GameTime(Seconds(1), Seconds(0.1), GameTime.FPS(30)),
        scene = scene,
        assetMapping = AssetMapping(Map("texture1" -> TextureRefAndOffset("atlas1", Vector2(1024, 1024), Point(32, 32)))),
        screenWidth = 320,
        screenHeight = 200,
        orthographicProjectionMatrix = CheapMatrix4.orthographic(320, 200)
      )

    val actual =
      SceneFrameDataConversion.fromJS(SceneFrameDataConversion.toJS(expected))

    assertEquals(actual.gameTime, expected.gameTime)
    assertEquals(actual.scene, expected.scene)
    assertEquals(actual.assetMapping, expected.assetMapping)
    assertEquals(actual.screenWidth, expected.screenWidth)
    assertEquals(actual.screenHeight, expected.screenHeight)
    assertEquals(actual.orthographicProjectionMatrix.mat.length, expected.orthographicProjectionMatrix.mat.length)
    assert(actual.orthographicProjectionMatrix.mat.zip(expected.orthographicProjectionMatrix.mat).forall(p => p._1 == p._2))
  }

}

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
