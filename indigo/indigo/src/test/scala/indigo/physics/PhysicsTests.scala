package indigo.physics

import indigo.*
import indigo.physics.*
import indigo.syntax.*

class PhysicsTests extends munit.FunSuite:

  val tag = "test"

  test("moveColliders") {
    val collider =
      Collider.Circle(tag, BoundingCircle(10, 10, 5)).withVelocity(Vector2(0, 10)).withFriction(Friction.zero)

    val colliders =
      Batch(collider)

    val w = World(
      colliders,
      Batch(Vector2(0)),
      Resistance.zero
    )

    val actual =
      Physics.Internal.moveColliders(1.second, w)

    val expected =
      Batch(
        Physics.Internal.IndexedCollider(
          0,
          collider.moveTo(Vertex(10, 20))
        )
      )

    assertEquals(actual, expected)
  }

  test("findCollisionGroups") {
    val c1 = Collider.Circle(tag, BoundingCircle(3, 2, 2)).withFriction(Friction.zero)   // overlaps c2 and c3
    val c2 = Collider.Circle(tag, BoundingCircle(1, 4, 2)).withFriction(Friction.zero)   // overlaps c1
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withFriction(Friction.zero)   // overlaps c1
    val c4 = Collider.Circle(tag, BoundingCircle(10, 20, 2)).withFriction(Friction.zero) // not touching
    val c5 = Collider.Circle(tag, BoundingCircle(8, 4, 2)).withFriction(Friction.zero)   // overlaps c3

    val idx1 = Physics.Internal.IndexedCollider(0, c1)
    val idx2 = Physics.Internal.IndexedCollider(1, c2)
    val idx3 = Physics.Internal.IndexedCollider(2, c3)
    val idx4 = Physics.Internal.IndexedCollider(3, c4)
    val idx5 = Physics.Internal.IndexedCollider(4, c5)

    val indexed =
      Batch(
        idx1,
        idx2,
        idx3,
        idx4,
        idx5
      )

    val actual =
      Physics.Internal.findCollisionGroups(indexed)

    val expected =
      Batch(
        c1 -> Batch(c2, c3),
        c2 -> Batch(c1),
        c3 -> Batch(c1, c5),
        c4 -> Batch(),
        c5 -> Batch(c3)
      )

    assertEquals(actual, expected)
  }

  test("(Circles) solveCollisions") {
    val c1 = Collider.Circle(tag, BoundingCircle(3, 2, 2)).withVelocity(Vector2(0, 10)).withFriction(Friction.zero)
    val c2 = Collider.Circle(tag, BoundingCircle(1, 4, 2)).withVelocity(Vector2(10, 0)).withFriction(Friction.zero)
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        c1 -> Batch(c2, c3),
        c2 -> Batch(c1),
        c3 -> Batch(c1)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c1.withPosition(3, 1.1715).withVelocity(0, -7.07106),
        c2.withPosition(0.1715, 4.8284).withVelocity(-7.07106, 7.07106),
        c3.withPosition(5.8284, 4.8284).withVelocity(7.07106, 7.07106)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
    assert(clue(actual(2)) ~== clue(expected(2)))
  }

  test("(Circles) solveCollisions - two dynamic - no movement") {
    val c2 = Collider.Circle(tag, BoundingCircle(1, 4, 2)).withVelocity(Vector2(10, 0)).withFriction(Friction.zero)
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        c2 -> Batch(),
        c3 -> Batch()
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c2,
        c3
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Circles) solveCollisions - two dynamic - collide") {
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 = Collider.Circle(tag, BoundingCircle(8, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(4, 4).withVelocity(-10, 0),
        c5.withPosition(9, 4).withVelocity(10, 0)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - two dynamic - collide") {
    val b1 = Collider.Box(tag, BoundingBox(3, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val b2 = Collider.Box(tag, BoundingBox(6, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        b1 -> Batch(b2),
        b2 -> Batch(b1)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        b1.withPosition(3, 2).withVelocity(10, 0), // These seem weird, it's because of the initial velocities.
        b2.withPosition(7, 2).withVelocity(10, 0)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Circles) solveCollisions - two dynamic - collide - zero time delta - displacement resolved") {
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 = Collider.Circle(tag, BoundingCircle(8, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(4, 4).withVelocity(-10, 0),
        c5.withPosition(9, 4).withVelocity(10, 0)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - two dynamic - collide - zero time delta - displacement resolved") {
    val c3 = Collider.Box(tag, BoundingBox(3, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 = Collider.Box(tag, BoundingBox(6, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(3, 2).withVelocity(10, 0), // These seem weird, it's because of the initial velocities.
        c5.withPosition(7, 2).withVelocity(10, 0)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Circles) solveCollisions - two dynamic - collide - half bounce") {
    val c3 = Collider
      .Circle(tag, BoundingCircle(5, 4, 2))
      .withVelocity(Vector2(-10, 0))
      .withRestitution(Restitution(0.5))
      .withFriction(Friction.zero)

    val c5 = Collider
      .Circle(tag, BoundingCircle(8, 4, 2))
      .withVelocity(Vector2(-10, 0))
      .withRestitution(Restitution(0.5))
      .withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(4.25, 4).withVelocity(-5, 0),
        c5.withPosition(8.75, 4).withVelocity(5, 0)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - two dynamic - collide - half bounce") {
    val c3 = Collider
      .Box(tag, BoundingBox(3, 2, 4, 4))
      .withVelocity(Vector2(-10, 0))
      .withRestitution(Restitution(0.5))
      .withFriction(Friction.zero)

    val c5 = Collider
      .Box(tag, BoundingBox(6, 2, 4, 4))
      .withVelocity(Vector2(-10, 0))
      .withRestitution(Restitution(0.5))
      .withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(2.75, 2).withVelocity(5, 0), // These seem weird, it's because of the initial velocities.
        c5.withPosition(6.75, 2).withVelocity(5, 0)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Circles) solveCollisions - one dynamic one static - collide") {
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(10, 0)).withFriction(Friction.zero)
    val c5 =
      Collider.Circle(tag, BoundingCircle(8, 4, 2)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(3, 4).withVelocity(-10, 0),
        c5
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - one dynamic one static - collide") {
    val c3 = Collider.Box(tag, BoundingBox(3, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 =
      Collider.Box(tag, BoundingBox(6, 2, 4, 4)).withVelocity(Vector2(-10, 0)).makeStatic.withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(3, 2).withVelocity(10, 0), // These seem weird, it's because of the initial velocities.
        c5
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - one dynamic one static - collide - vertical") {
    val c3 = Collider.Box(tag, BoundingBox(2, 0, 3, 3)).withVelocity(Vector2(0, 10)).withFriction(Friction.zero)
    val c5 =
      Collider.Box(tag, BoundingBox(0, 2, 8, 2)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    val collisions =
      Batch(
        c3 -> Batch(c5),
        c5 -> Batch(c3)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(2, -2).withVelocity(0, -10),
        c5
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
    assert(clue(actual(1)) ~== clue(expected(1)))
  }

  test("Example: A circle coming to rest on top of a box, as it reaches the edge, should fall off not bounce back.") {
    val circle =
      Collider.Circle(tag, BoundingCircle(0, -1, 1)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val box =
      Collider.Box(tag, BoundingBox(0, 0, 3, 3)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    assert(circle.hitTest(box))

    val collisions =
      Batch(
        circle -> Batch(box)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        circle.withPosition(-0.0855, -1.1425).withVelocity(-10, 0)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
  }

  test("Example: A box travelling down y whose corner hits a static circle at ~45 degrees should not get stuck.") {
    val pointInsideCircle = Vertex(1.4, -1.4)
    val box = Collider
      .Box(tag, BoundingBox(pointInsideCircle + Vertex(0, -3), Vertex(3, 3)))
      .withVelocity(Vector2(0, 10))
      .withFriction(Friction.zero)
    val circle =
      Collider.Circle(tag, BoundingCircle(0, 0, 2)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    val collisions =
      Batch(
        box -> Batch(circle)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        box.withPosition(1.4284, -4.4284).withVelocity(7.07106, -7.07106)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
  }

  test("Example: From the top, striking the corner") {
    val circle =
      Collider.Circle(tag, BoundingCircle(0, -0.5, 1)).withVelocity(Vector2(-10, 10)).withFriction(Friction.zero)
    val box =
      Collider.Box(tag, BoundingBox(0, 0, 3, 3)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    assert(circle.hitTest(box))

    val collisions =
      Batch(
        circle -> Batch(box)
      )

    val actual =
      Physics.Internal.solveAllCollisions(collisions)

    val expected =
      Batch(
        circle.withPosition(-0.4901, -1.0651).withVelocity(-10, -10)
      )

    assert(clue(actual(0)) ~== clue(expected(0)))
  }
