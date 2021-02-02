// package indigo.platform.renderer.webgl2

// import indigo.shared.scenegraph.PointLight
// import indigo.shared.datatypes.Rectangle
// import indigo.shared.datatypes.Point
// import indigo.shared.scenegraph.DirectionLight
// import indigo.shared.scenegraph.SpotLight
// import indigo.shared.scenegraph.Light

// class RendererLightsTests extends munit.FunSuite {

//   val viewBound = Rectangle(0, 0, 200, 100)

//   test("should be able to tell if a point light is in range") {
//     val light = PointLight.default.withAttenuation(10)

//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(0, 0)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(200, 0)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(200, 100)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(0, 100)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(100, 50)), viewBound), true)

//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(-5, -5)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(205, 50)), viewBound), true)

//     // Attenuation + 1!!
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(-12, -12)), viewBound), false)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(212, 50)), viewBound), false)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(400, 10)), viewBound), false)
//   }

//   test("should be able to tell if a spot light is in range") {
//     val light = SpotLight.default.withAttenuation(10)

//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(0, 0)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(200, 0)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(200, 100)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(0, 100)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(100, 50)), viewBound), true)

//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(-5, -5)), viewBound), true)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(205, 50)), viewBound), true)

//     // Attenuation + 1!!
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(-12, -12)), viewBound), false)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(212, 50)), viewBound), false)
//     assertEquals(RendererLights.lightIsInRange(light.moveTo(Point(400, 10)), viewBound), false)
//   }

//   test("should be able to tell if a direction light is in range") {
//     val light = DirectionLight.default

//     assertEquals(RendererLights.lightIsInRange(light, viewBound), true)
//   }

//   test("should be able to filter out lights that are not in range") {

//     val lights: List[Light] =
//       List(
//         DirectionLight.default.withHeight(100),
//         SpotLight.default.moveTo(Point(-100, 10)).withAttenuation(10),
//         SpotLight.default.moveTo(Point(10, 10)).withAttenuation(10),
//         PointLight.default.moveTo(Point(-100, 10)).withAttenuation(10),
//         PointLight.default.moveTo(Point(10, 10)).withAttenuation(10)
//       )

//     val expected: List[Light] =
//       List(
//         DirectionLight.default.withHeight(100),
//         SpotLight.default.moveTo(Point(10, 10)).withAttenuation(10),
//         PointLight.default.moveTo(Point(10, 10)).withAttenuation(10)
//       )

//     val actual: List[Light] =
//       RendererLights.lightsInRange(lights, viewBound)

//     assertEquals(actual.length, 3)
//     assertEquals(expected, actual)

//   }

// }
