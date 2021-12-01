package indigoextras.geometry

import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

class VertexTests extends munit.FunSuite {

  def nearEnoughEqual(d1: Double, d2: Double, tolerance: Double): Boolean =
    d1 >= d2 - tolerance && d1 <= d2 + tolerance

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

  test("dot product") {
    assert(clue(Vertex(1, 0).dot(Vertex(1, 0))) == 1)
    assert(clue(Vertex(1, 0).dot(Vertex(0, -1))) == 0)
    assert(clue(Vertex(-1, 0).dot(Vertex(1, 0))) == -1)
    assert(clue(Vertex(1, 0).dot(Vertex(-1, 0))) == -1)
  }

  test("abs") {
    assertEquals(Vertex(1, 1).abs, Vertex(1, 1))
    assertEquals(Vertex(-1, 1).abs, Vertex(1, 1))
    assertEquals(Vertex(1, -1).abs, Vertex(1, 1))
    assertEquals(Vertex(-1, -1).abs, Vertex(1, 1))
  }

  test("min") {
    assertEquals(Vertex(10, 10).min(1), Vertex(1, 1))
    assertEquals(Vertex(10, 10).min(100), Vertex(10, 10))
    assertEquals(Vertex(10, 10).min(Vertex(50, 5)), Vertex(10, 5))
  }

  test("max") {
    assertEquals(Vertex(10, 10).max(1), Vertex(10, 10))
    assertEquals(Vertex(10, 10).max(100), Vertex(100, 100))
    assertEquals(Vertex(10, 10).max(Vertex(50, 5)), Vertex(50, 10))
  }

  test("clamp (double)") {
    assertEquals(Vertex(0.1, 0.1).clamp(0, 1), Vertex(0.1, 0.1))
    assertEquals(Vertex(-0.1, 1.1).clamp(0, 1), Vertex(0.0, 1.0))
    assertEquals(Vertex(1, 4).clamp(2, 3), Vertex(2, 3))
  }

  test("clamp (vertex)") {
    assertEquals(Vertex(0.1, 0.1).clamp(Vertex(0, 0), Vertex(1, 1)), Vertex(0.1, 0.1))
    assertEquals(Vertex(-0.1, 1.1).clamp(Vertex(0, 0), Vertex(1, 1)), Vertex(0.0, 1.0))

    assertEquals(Vertex(1, 4).clamp(Vertex(10, 20), Vertex(30, 40)), Vertex(10, 20))
    assertEquals(Vertex(12, 33).clamp(Vertex(10, 20), Vertex(30, 40)), Vertex(12, 33))
    assertEquals(Vertex(50, 100).clamp(Vertex(10, 20), Vertex(30, 40)), Vertex(30, 40))
  }

  test("length") {
    assertEquals(Vertex(10, 0).length, 10.0)
    assertEquals(Vertex(0, 10).length, 10.0)
    assert(nearEnoughEqual(Vertex(10, 10).length, 14.14d, 0.01))
  }

  test("invert") {
    assertEquals(Vertex(1, 1).invert, Vertex(-1, -1))
    assertEquals(Vertex(-1, 1).invert, Vertex(1, -1))
    assertEquals(Vertex(1, -1).invert, Vertex(-1, 1))
    assertEquals(Vertex(-1, -1).invert, Vertex(1, 1))
  }

  test("translate | moveBy | moveTo") {
    assertEquals(Vertex(1, 1).translate(Vertex(10, 10)), Vertex(11, 11))
    assertEquals(Vertex(1, 1).moveBy(Vertex(10, 10)), Vertex(11, 11))
    assertEquals(Vertex(1, 1).moveTo(Vertex(10, 10)), Vertex(10, 10))
  }

  test("translate | moveBy | moveTo (Vector2)") {
    assertEquals(Vertex(1, 1).translate(Vector2(10, 10)), Vertex(11, 11))
    assertEquals(Vertex(1, 1).moveBy(Vector2(10, 10)), Vertex(11, 11))
    assertEquals(Vertex(1, 1).moveTo(Vector2(10, 10)), Vertex(10, 10))
  }

  test("scaleBy") {
    assertEquals(Vertex(2, 2).scaleBy(Vertex(10, 2)), Vertex(20, 4))
    assertEquals(Vertex(2, 2).scaleBy(Vector2(10, 2)), Vertex(20, 4))
  }

  test("rotate around origin") {
    // 90
    assert(Vertex(1, 0).rotateBy(Radians.PIby2) ~== Vertex(0, 1))

    // -90
    assert(Vertex(1, 1).rotateBy(Radians.PIby2 * -1.0) ~== Vertex(1, -1))

    // 45
    val v = Vertex(1, 2)
    val expected = Vertex(-0.70710,2.12132)
    assert(v.rotateBy(Radians.PIby2 / 2.0) ~== expected)
    // magnitude should remain the same
    assert(Math.abs(v.length - expected.length) <= 0.0001)
  }

  test("rotate around given point") {
    assert(Vertex(3, 1).rotateBy(Radians.PIby2, Vertex(-2,-2)) ~== Vertex(-5, 3))
    assert(Vertex(-3, 4).rotateBy(Radians.PIby2 * -1.0, Vertex(-1,2)) ~== Vertex(1, 4))
  }

  test("rotate to") {
    // 0 to 270
    assert(Vertex(1, 0).rotateTo(Radians.PI + Radians.PIby2) ~== Vertex(0, -1))
    // 45 to 270
    assert(Vertex(1, 1).rotateTo(Radians.PI + Radians.PIby2) ~== Vertex(0, -1.41421))
  }

  test("angle") {
    // -90
    assertEquals(Vertex(0, -1).angle, Radians.PIby2 * -1.0)
    // -90 at different magnitude
    assertEquals(Vertex(0, -8.5).angle, Radians.PIby2 * -1.0)
  }

  test("round") {
    assertEquals(Vertex(2.2, 2.6).round, Vertex(2, 3))
  }

  test("twoVerticesToVector2") {
    assertEquals(Vertex(2, 2).makeVectorWith(Vertex(1, 5)), Vector2(-1, 3))
  }

  test("approx equal") {
    assert(Vertex(5.0, 5.0) ~== Vertex(4.999999, 5.00001))
    assert(!(Vertex(5.0, 5.0) ~== Vertex(-4.999999, 5.00001)))
    assert(!(Vertex(5.0, 5.0) ~== Vertex(4.98, 5.00001)))
  }

}
