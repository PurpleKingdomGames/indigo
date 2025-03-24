package indigoextras.waypoints

import indigo.Batch
import indigo.Radians
import indigo.Vertex

class WaypointPathTests extends munit.FunSuite:
  test("test position for non looped paths") {
    val waypoints = Batch(Vertex(1, 0), Vertex(0, 1))
    val path      = WaypointPath(waypoints, WaypointPathConfig(0.0, looping = false))

    assertEquals(path.calculatePosition(0.0).position, Vertex(1, 0))
    assertEquals(path.calculatePosition(0.5).position, Vertex(0.5, 0.5))
    assertEquals(path.calculatePosition(1.0).position, Vertex(0, 1))
  }

  test("test position for looped paths") {
    val waypoints = Batch(Vertex(1, 0), Vertex(0, 1))
    val path      = WaypointPath(waypoints, WaypointPathConfig(0.0, looping = true))

    assertEquals(path.calculatePosition(0.0).position, Vertex(1, 0))
    assertEquals(path.calculatePosition(0.5).position, Vertex(0, 1))
    assertEquals(path.calculatePosition(1.0).position, Vertex(1, 0))
  }

  test("test rotation") {
    val waypoints = Batch(Vertex(1, 0), Vertex(0, 1))
    val path      = WaypointPath(waypoints, WaypointPathConfig(0.0, looping = false))

    val expectedAngle = (Radians.PIby2 * 3 / 2).toDouble
    assertEqualsDouble(path.calculatePosition(0.0).direction.toDouble, expectedAngle, 0.01)
    assertEqualsDouble(path.calculatePosition(0.5).direction.toDouble, expectedAngle, 0.01)
    assertEqualsDouble(path.calculatePosition(1.0).direction.toDouble, expectedAngle, 0.01)
  }
