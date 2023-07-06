package indigo.physics

import indigo.physics.*
import indigo.shared.datatypes.Vector2
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.BoundingCircle
import indigo.shared.geometry.LineSegment
import indigo.shared.geometry.Vertex

class DisplacementTests extends munit.FunSuite:

  val idA = "a"
  val idB = "b"

  // Box to Box

  test("Displacement - Box to Box - no corners inside (cross)") {

    val a = Collider.Box(idA, BoundingBox(1, 3, 5, 2)).withVelocity(Vector2(10, 10))
    val b = Collider.Box(idB, BoundingBox(3, 1, 2, 5)).makeStatic

    val actual =
      a.displacementWith(b)

    val expected =
      Displacement(3, Vector2(-1, 0), LineSegment(Vertex(3, 4), Vertex(2, 4)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))

  }

  test("Displacement - Box to Box (1)") {

    val a = Collider.Box(idA, BoundingBox(1, 1, 5, 5)).withVelocity(Vector2(10, 10))
    val b = Collider.Box(idB, BoundingBox(2, 2, 5, 5)).makeStatic

    val actual =
      a.displacementWith(b)

    val expected =
      Displacement(5.6568, Vector2(-1, -1), LineSegment(Vertex(2, 2), Vertex(1, 1)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))

  }

  test("Displacement - Box to Box (1, reverse)") {

    val a = Collider.Box(idA, BoundingBox(2, 2, 2, 2)).withVelocity(Vector2(10, 10))
    val b = Collider.Box(idB, BoundingBox(1, 1, 2, 2)).makeStatic

    val actual =
      a.displacementWith(b)

    val expected =
      Displacement(1.4142, Vector2(1, 1), LineSegment(Vertex(3, 3), Vertex(4, 4)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))

  }

  test("Displacement - Box to Box (b inside a)") {

    val a = Collider.Box(idA, BoundingBox(0, 0, 4, 4)).withVelocity(Vector2(10, 0))
    val b = Collider.Box(idB, BoundingBox(2, 1, 2, 1)).makeStatic

    val actual =
      a.displacementWith(b)

    val expected =
      Displacement(2.8284, Vector2(-1, 1), LineSegment(Vertex(2, 2), Vertex(1, 3)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))

  }

  test("Displacement - Box to Box (even collision on x axis)") {

    val a = Collider.Box(idA, BoundingBox(0, 0, 2, 2)).withVelocity(Vector2(2, 0))
    val b = Collider.Box(idB, BoundingBox(1.5, 0, 2, 2)).withVelocity(Vector2(-2, 0))

    val actualA =
      a.displacementWith(b)

    val expectedA =
      Displacement(0.5, Vector2(-1, 0), LineSegment(Vertex(1.5, 1), Vertex(0.5, 1)), Mass.one, Some(Mass.one))

    assert(clue(actualA) ~== clue(expectedA))
    assert(clue(actualA.displaceBy) ~== clue(Vector2(-0.25, 0)))

    val actualB =
      b.displacementWith(a)

    val expectedB =
      Displacement(0.5, Vector2(1, 0), LineSegment(Vertex(2, 1), Vertex(3, 1)), Mass.one, Some(Mass.one))

    assert(clue(actualB) ~== clue(expectedB))
    assert(clue(actualB.displaceBy) ~== clue(Vector2(0.25, 0)))
  }

  test("Displacement - Box to Box (2)") {

    val a = Collider.Box(idA, BoundingBox(3, 2, 4, 4)).withVelocity(Vector2(-10, 0))
    val b = Collider.Box(idB, BoundingBox(6, 2, 4, 4)).withVelocity(Vector2(-10, 0))

    val actual =
      a.displacementWith(b)

    val expected =
      Displacement(1, Vector2(-1, 0), LineSegment(Vertex(6, 4), Vertex(5, 4)), Mass.one, Some(Mass.one))

    assert(clue(actual) ~== clue(expected))

  }

  test("Displacement - Box to Box (3 - platform)") {

    val a = Collider.Box(idA, BoundingBox(2, 0, 3, 3)).withVelocity(Vector2(0, 10))
    val b = Collider.Box(idB, BoundingBox(0, 2, 8, 2)).withVelocity(Vector2(0, 0))

    val actual =
      a.displacementWith(b)

    val expected =
      Displacement(1, Vector2(0, -1), LineSegment(Vertex(3.5, 2), Vertex(3.5, 1)), Mass.one, Option(Mass.one))

    assert(clue(actual) ~== clue(expected))

  }

  // Circle to Circle

  test("Displacement - Circle to Circle") {
    val a = Collider.Circle(idA, BoundingCircle(0, 0, 5))
    val b = Collider.Circle(idB, BoundingCircle(9, 0, 5))

    val actual = a.displacementWith(b)
    val expected =
      Displacement(1.0, Vector2(-1, 0), LineSegment(Vertex(0, 0), Vertex(9, 0)), Mass.one, Option(Mass.one))

    assert(clue(actual) ~== clue(expected))
    assert(clue(actual.displaceBy) ~== clue(Vector2(-0.5, 0)))
  }

  test("Displacement - Circle to Circle (b is static)") {
    val a = Collider.Circle(idA, BoundingCircle(0, 0, 5))
    val b = Collider.Circle(idB, BoundingCircle(9, 0, 5)).makeStatic

    val actual   = a.displacementWith(b)
    val expected = Displacement(1.0, Vector2(-1, 0), LineSegment(Vertex(0, 0), Vertex(9, 0)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))
    assert(clue(a.displacementWith(b).displaceBy) ~== clue(Vector2(-1.0, 0)))
  }

  // Circle to Box

  test("Displacement - Circle to Box - corner") {
    val a = Collider.Circle(idA, BoundingCircle(0, 0, 3)).withVelocity(Vector2(10))
    val b = Collider.Box(idB, BoundingBox(2, 2, 6, 6)).makeStatic

    val actual = a.displacementWith(b)
    val expected =
      Displacement(0.1715, Vector2(-0.7071, -0.7071), LineSegment(Vertex(0, 0), Vertex(5, 5)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))
  }

  test("Displacement - Circle to Box - side") {
    val a = Collider.Circle(idA, BoundingCircle(0, 4, 3)).withVelocity(Vector2(10))
    val b = Collider.Box(idB, BoundingBox(2, 1, 6, 6)).makeStatic

    val actual   = a.displacementWith(b)
    val expected = Displacement(1, Vector2(-1, 0), LineSegment(Vertex(0, 4), Vertex(5, 4)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))
  }

  test("Displacement - Circle to Box - top") {
    val a = Collider.Circle(idA, BoundingCircle(0, -0.5, 1)).withVelocity(Vector2(10))
    val b = Collider.Box(idB, BoundingBox(0, 0, 3, 3)).makeStatic

    val actual = a.displacementWith(b)
    val expected =
      Displacement(0.375, Vector2(-0.6, -0.8), LineSegment(Vertex(0, -0.5), Vertex(1.5, 1.5)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))
  }

  // Box to Circle

  test("Displacement - Box to Circle - corner") {
    val a = Collider.Box(idA, BoundingBox(0, 0, 3, 3)).withVelocity(Vector2(10))
    val b = Collider.Circle(idB, BoundingCircle(4, 4, 2)).makeStatic

    val actual = a.displacementWith(b)
    val expected =
      Displacement(0.5857, Vector2(-0.7071), LineSegment(Vertex(1.5, 1.5), Vertex(4, 4)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))
  }

  test("Displacement - Box to Circle - side") {
    val a = Collider.Box(idA, BoundingBox(0, 2, 3, 4)).withVelocity(Vector2(10))
    val b = Collider.Circle(idB, BoundingCircle(4, 4, 2)).makeStatic

    val actual   = a.displacementWith(b)
    val expected = Displacement(1, Vector2(-1, 0), LineSegment(Vertex(1.5, 4), Vertex(4, 4)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))
  }

  test("Displacement - Circle to Box - top - long platform") {
    val a = Collider.Circle(idA, BoundingCircle(50, 90, 20)).withVelocity(Vector2(10, 10))
    val b = Collider.Box(idB, BoundingBox(0, 100, 1000, 100)).makeStatic

    val actual = a.displacementWith(b)
    val expected =
      Displacement(10, Vector2(0, -1), LineSegment(Vertex(50, 90), Vertex(50, 150)), Mass.one, None)

    assert(clue(actual) ~== clue(expected))
  }
