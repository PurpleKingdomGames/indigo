package indigoextras.waypoints

import indigo.Radians
import indigo.shared.datatypes.Vector2

class WaypointPathTests extends munit.FunSuite:
  test("test position for non looped paths") {
    val waypoints = List(Vector2(1, 0), Vector2(0, 1))
    val path      = WaypointPath(waypoints, 0.0, loop = false)

    assertEquals(path.calculatePosition(0.0)._1, Vector2(1, 0))
    assertEquals(path.calculatePosition(0.5)._1, Vector2(0.5, 0.5))
    assertEquals(path.calculatePosition(1.0)._1, Vector2(0, 1))
  }

  test("test position for looped paths") {
    val waypoints = List(Vector2(1, 0), Vector2(0, 1))
    val path      = WaypointPath(waypoints, 0.0, loop = true)

    assertEquals(path.calculatePosition(0.0)._1, Vector2(1, 0))
    assertEquals(path.calculatePosition(0.5)._1, Vector2(0, 1))
    assertEquals(path.calculatePosition(1.0)._1, Vector2(1, 0))
  }

  test("test rotation") {
    val waypoints = List(Vector2(1, 0), Vector2(0, 1))
    val path      = WaypointPath(waypoints, 0.0, loop = false)

    val expectedAngle = (Radians.PIby2 * 3 / 2).toDouble
    assertEqualsDouble(path.calculatePosition(0.0)._2.toDouble, expectedAngle, 0.01)
    assertEqualsDouble(path.calculatePosition(0.5)._2.toDouble, expectedAngle, 0.01)
    assertEqualsDouble(path.calculatePosition(1.0)._2.toDouble, expectedAngle, 0.01)
  }
