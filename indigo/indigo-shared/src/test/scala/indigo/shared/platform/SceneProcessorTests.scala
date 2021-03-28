package indigo.shared.platform

import indigo.shared.scenegraph.Light
import indigo.shared.scenegraph.AmbientLight
import indigo.shared.datatypes.RGBA
import indigo.shared.scenegraph.DirectionLight
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.Radians

class SceneProcessorTests extends munit.FunSuite {

  /*
  For reference, this is what the shader is expecting.
  layout (std140) uniform IndigoDynamicLightingData {
    float numOfLights;
    vec4 lightFlags[8]; // vec4(active, type, ???, ???)
    vec4 lightColor[8];
    vec4 lightSpecular[8];
    vec4 lightPositionRotation[8];
    vec4 lightNearFarAngleAttenuation[8];
  };
   */

  test("convert an ambient light to UBO data Array[Float]") {

    val light: Light =
      AmbientLight(RGBA.Red.withAmount(0.5))

    val actual: LightData =
      SceneProcessor.makeLightData(light)

    val expected: LightData =
      LightData(
        Array[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
        Array[Float](1.0f, 0.0f, 0.0f, 0.5f), // lightColor
        Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
        Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
        Array[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleAttenuation
      )

    assertEquals(actual.toArray.toList, expected.toArray.toList)

  }

  test("convert an direction light to UBO data Array[Float]") {

    val light: Light =
      DirectionLight(0.75, RGB.Cyan, 0.5, Radians(0.25))

    val actual: LightData =
      SceneProcessor.makeLightData(light)

    val expected: LightData =
      LightData(
        Array[Float](1.0f, 1.0f, 0.0f, 0.0f),   // lightFlags
        Array[Float](0.0f, 1.0f, 1.0f, 0.5f),   // lightColor
        Array[Float](0.0f, 0.0f, 0.0f, 0.0f),   // lightSpecular
        Array[Float](0.0f, 0.0f, 0.75f, 0.25f), // lightPositionRotation
        Array[Float](0.0f, 0.0f, 0.0f, 0.0f)    // lightNearFarAngleAttenuation
      )

    assertEquals(actual.toArray.toList, expected.toArray.toList)

  }

  test("Combining lights into data") {
    val lights: List[Light] =
      List(
        AmbientLight(RGBA.Red.withAmount(0.5)),
        DirectionLight(0.75, RGB.Cyan, 0.5, Radians(0.25)),
        AmbientLight(RGBA.Green.withAmount(0.8))
      )

    val actual: Array[Float] =
      SceneProcessor.makeLightsData(lights)

    val expected: Array[Float] =
      Array[Float](3) ++
        (LightData(
          Array[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
          Array[Float](1.0f, 0.0f, 0.0f, 0.5f), // lightColor
          Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
          Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
          Array[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleAttenuation
        ) +
          LightData(
            Array[Float](1.0f, 1.0f, 0.0f, 0.0f),   // lightFlags
            Array[Float](0.0f, 1.0f, 1.0f, 0.5f),   // lightColor
            Array[Float](0.0f, 0.0f, 0.0f, 0.0f),   // lightSpecular
            Array[Float](0.0f, 0.0f, 0.75f, 0.25f), // lightPositionRotation
            Array[Float](0.0f, 0.0f, 0.0f, 0.0f)    // lightNearFarAngleAttenuation
          ) +
          LightData(
            Array[Float](1.0f, 0.0f, 0.0f, 0.0f), // lightFlags
            Array[Float](0.0f, 1.0f, 0.0f, 0.8f), // lightColor
            Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightSpecular
            Array[Float](0.0f, 0.0f, 0.0f, 0.0f), // lightPositionRotation
            Array[Float](0.0f, 0.0f, 0.0f, 0.0f)  // lightNearFarAngleAttenuation
          )).toArray

    assertEquals(actual.toList.map(to2dp), expected.toList.map(to2dp))
  }

  def to2dp(d: Float): Double =
    Math.round(d.toDouble * 100).toDouble / 100.0

}
