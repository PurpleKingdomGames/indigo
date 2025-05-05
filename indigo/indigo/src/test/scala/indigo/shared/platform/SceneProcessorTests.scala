package indigo.shared.platform

import indigo.*

class SceneProcessorTests extends munit.FunSuite {

  /*
  For reference, this is what the shader is expecting.
  layout (std140) uniform IndigoDynamicLightingData {
    float numOfLights;
    vec4 lightFlags[8]; // vec4(active, type, use far, falloff type)
    vec4 lightColor[8];
    vec4 lightSpecular[8];
    vec4 lightPositionRotation[8];
    vec4 lightNearFarAngleIntensity[8];
  };
   */

  test("convert an ambient light to UBO data Array[Float]") {

    val light: Light =
      AmbientLight(RGBA.Red.withAmount(0.5))

    val actual: LightData =
      SceneProcessor.makeLightData(light)

    val expected: LightData =
      LightData(
        scalajs.js.Array[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
        scalajs.js.Array[Float](1.0f, 0.0f, 0.0f, 0.5f), // lightColor
        scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
        scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
        scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleIntensity
      )

    assertEquals(actual.toArray.toList, expected.toArray.toList)

  }

  test("convert a direction light to UBO data Array[Float]") {

    val light: Light =
      DirectionLight(RGBA.Cyan.withAlpha(0.5), RGBA.White, Radians(0.25))

    val actual: LightData =
      SceneProcessor.makeLightData(light)

    val expected: LightData =
      LightData(
        scalajs.js.Array[Float](1.0f, 1.0f, 0.0f, 0.0f),  // lightFlags
        scalajs.js.Array[Float](0.0f, 1.0f, 1.0f, 0.5f),  // lightColor
        scalajs.js.Array[Float](1.0f, 1.0f, 1.0f, 1.0f),  // lightSpecular
        scalajs.js.Array[Float](0.0f, 0.0f, 0.25f, 0.0f), // lightPositionRotation
        scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)   // lightNearFarAngleAttenuation
      )

    assertEquals(actual.toArray.toList, expected.toArray.toList)

  }

  test("Combining lights into data") {
    val lights: Batch[Light] =
      Batch(
        AmbientLight(RGBA.Red.withAmount(0.5)),
        DirectionLight(RGBA.Cyan.withAlpha(0.5), RGBA.White, Radians(0.25)),
        AmbientLight(RGBA.Green.withAmount(0.8))
      )

    val actual: scalajs.js.Array[Float] =
      SceneProcessor.makeLightsData(lights.toJSArray)

    val expected: scalajs.js.Array[Float] =
      scalajs.js.Array[Float](3, 0, 0, 0) ++ // first value, even though single float, requires space of vec4.
        (
          LightData(
            scalajs.js.Array[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
            scalajs.js.Array[Float](1.0f, 0.0f, 0.0f, 0.5f), // lightColor
            scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
            scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
            scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleAttenuation
          ) +
            LightData(
              scalajs.js.Array[Float](1.0f, 1.0f, 0.0f, 0.0f),  // lightFlags
              scalajs.js.Array[Float](0.0f, 1.0f, 1.0f, 0.5f),  // lightColor
              scalajs.js.Array[Float](1.0f, 1.0f, 1.0f, 1.0f),  // lightSpecular
              scalajs.js.Array[Float](0.0f, 0.0f, 0.25f, 0.0f), // lightPositionRotation
              scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)   // lightNearFarAngleAttenuation
            ) +
            LightData(
              scalajs.js.Array[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
              scalajs.js.Array[Float](0.0f, 1.0f, 0.0f, 0.8f), // lightColor
              scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
              scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
              scalajs.js.Array[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleAttenuation
            ) + LightData.empty +                              // There are always 8 lights.
            LightData.empty +
            LightData.empty +
            LightData.empty +
            LightData.empty
        ).toArray

    assertEquals(actual.toList.map(to2dp), expected.toList.map(to2dp))
  }

  test("Layer compacting") {
    val layers: Batch[LayerEntry] =
      SceneProcessorTestData.uncompacted

    val actual =
      SceneProcessor.compactLayers(layers)

    val expected =
      SceneProcessorTestData.compacted

    assertEquals(clue(actual), clue(expected))
  }

  def to2dp(d: Float): Double =
    Math.round(d.toDouble * 100).toDouble / 100.0

}

object SceneProcessorTestData:

  val shape: Shape.Box =
    Shape.Box(Rectangle(0, 0, 100, 100), Fill.Color(RGBA.Red))

  val uncompacted: Batch[LayerEntry] =
    Batch(
      LayerEntry(Layer.empty),
      LayerEntry(LayerKey("b"), Layer.empty),
      LayerEntry(
        LayerKey("c"),
        Layer.Stack(
          Layer.Content(shape),
          Layer.Content(shape)
        )
      ),
      LayerEntry(
        LayerKey("d"),
        Layer.Stack(
          Layer.empty.withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape).withCamera(Camera.Fixed(Point(10))),
          Layer.Stack(
            Layer(shape).withCamera(Camera.Fixed(Point(10))),
            Layer(shape)
          )
        )
      )
    )

  val compacted: Batch[(Option[LayerKey], Batch[Layer.Content])] =
    Batch(
      (None, Batch(Layer.Content.empty)),
      (Some(LayerKey("b")), Batch(Layer.Content.empty)),
      (
        Some(LayerKey("c")),
        Batch(
          Layer.Content(shape, shape)
        )
      ),
      (
        Some(LayerKey("d")),
        Batch(
          Layer.Content(shape).withCamera(Camera.Fixed(Point.zero)),
          Layer.Content(shape, shape).withCamera(Camera.Fixed(Point(10))),
          Layer.Content(shape)
        )
      )
    )
