package indigoextras.geometry

import indigo.shared.EqualTo._

class VertexTests extends munit.FunSuite {

  test("Distance.horizontal distance") {
    assertEquals(Vertex.zero.distanceTo(Vertex(10, 0)), 10d)
  }

  test("Distance.vertical distance") {
    assertEquals(Vertex.zero.distanceTo(Vertex(0, 0.5)), 0.5d)
    assertEquals(Vertex(0, 0.1).distanceTo(Vertex(0, 0.5)), 0.4d)
  }

  test("Distance.diagonal distance") {
    val a = (0.9d - 0.1d) * 0.9d - 0.1d
    val b = (0.9d - 0.1d) * 0.9d - 0.1d
    val c = Math.sqrt(a + b)

    assertEquals(nearEnoughEqual(Vertex(0.1, 0.1).distanceTo(Vertex(0.9, 0.9)), c, 0.025d), true)
  }

  test("Distance.diagonal distance > 1") {
    val a = Math.pow(100.0d, 2)
    val b = Math.pow(100.0d, 2)
    val c = Math.sqrt(a + b)

    assertEquals(nearEnoughEqual(Vertex(0.0, 0.0).distanceTo(Vertex(100.0, 100.0)), c, 0.025d), true)
  }

  def nearEnoughEqual(d1: Double, d2: Double, tolerance: Double): Boolean =
    d1 >= d2 - tolerance && d1 <= d2 + tolerance

}
