package indigo.platform.renderer

import utest._
import indigo.shared.scenegraph.PointLight
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.scenegraph.DirectionLight
import indigo.shared.scenegraph.SpotLight

object RendererLightsTests extends TestSuite {

  val viewBound = Rectangle(0, 0, 200, 100)

  val tests: Tests =
    Tests {

      "should be able to tell if a point light is in range" - {
        val light = PointLight.default.withAttenuation(10)

        RendererLights.lightIsInRange(light.moveTo(Point(0, 0)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(200, 0)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(200, 100)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(0, 100)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(100, 50)), viewBound) ==> true

        RendererLights.lightIsInRange(light.moveTo(Point(-5, -5)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(205, 50)), viewBound) ==> true

        // Attenuation + 1!!
        RendererLights.lightIsInRange(light.moveTo(Point(-12, -12)), viewBound) ==> false
        RendererLights.lightIsInRange(light.moveTo(Point(212, 50)), viewBound) ==> false
        RendererLights.lightIsInRange(light.moveTo(Point(400, 10)), viewBound) ==> false
      }

      "should be able to tell if a spot light is in range" - {
        val light = SpotLight.default.withAttenuation(10)

        RendererLights.lightIsInRange(light.moveTo(Point(0, 0)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(200, 0)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(200, 100)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(0, 100)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(100, 50)), viewBound) ==> true

        RendererLights.lightIsInRange(light.moveTo(Point(-5, -5)), viewBound) ==> true
        RendererLights.lightIsInRange(light.moveTo(Point(205, 50)), viewBound) ==> true

        // Attenuation + 1!!
        RendererLights.lightIsInRange(light.moveTo(Point(-12, -12)), viewBound) ==> false
        RendererLights.lightIsInRange(light.moveTo(Point(212, 50)), viewBound) ==> false
        RendererLights.lightIsInRange(light.moveTo(Point(400, 10)), viewBound) ==> false
      }

      "should be able to tell if a direction light is in range" - {
        val light = DirectionLight.default

        RendererLights.lightIsInRange(light, viewBound) ==> true
      }

      "should be able to filter out lights that are not in range" - {

        val lights =
          List(
            DirectionLight.default.withHeight(100),
            SpotLight.default.moveTo(Point(-100, 10)).withAttenuation(10),
            SpotLight.default.moveTo(Point(10, 10)).withAttenuation(10),
            PointLight.default.moveTo(Point(-100, 10)).withAttenuation(10),
            PointLight.default.moveTo(Point(10, 10)).withAttenuation(10)
          )

        val expected =
          List(
            DirectionLight.default.withHeight(100),
            SpotLight.default.moveTo(Point(10, 10)).withAttenuation(10),
            PointLight.default.moveTo(Point(10, 10)).withAttenuation(10)
          )

        val actual =
          RendererLights.lightsInRange(lights, viewBound)

        actual.length ==> 3
        expected ==> actual

      }

    }

}
