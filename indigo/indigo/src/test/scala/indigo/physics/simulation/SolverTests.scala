package indigo.physics.simulation

import indigo.*
import indigo.physics.*
import indigo.syntax.*

class SolverTests extends munit.FunSuite:

  given [A]: CanEqual[Collider[String], Collider[String]] =
    CanEqual.derived

  given QuadTree.InsertOptions =
    QuadTree.options(
      idealCount = 1,
      minSize = 1,
      maxDepth = 16
    )

  val settings = SimulationSettings(
    bounds = BoundingBox(0, 0, 10, 10),
    idealCount = 1,
    minSize = 1,
    maxDepth = 16
  )

  val tag = "test"

  test("(Circles) solveCollisions") {
    val c1 = Collider.Circle(tag, BoundingCircle(3, 2, 2)).withVelocity(Vector2(0, 10)).withFriction(Friction.zero)
    val c2 = Collider.Circle(tag, BoundingCircle(1, 4, 2)).withVelocity(Vector2(10, 0)).withFriction(Friction.zero)
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c1, c1) -> Batch(c2, c3),
        IndexedCollider(1, c2, c2) -> Batch(c1),
        IndexedCollider(2, c3, c3) -> Batch(c1)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c1.withPosition(3, 1.1715).withVelocity(0, -7.07106),
        c2.withPosition(0.1715, 4.8284).withVelocity(-7.07106, 7.07106),
        c3.withPosition(5.8284, 4.8284).withVelocity(7.07106, 7.07106)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
    assert(clue(actual(2).proposed) ~== clue(expected(2)))
  }

  test("(Circles) solveCollisions - two dynamic - no movement") {
    val c2 = Collider.Circle(tag, BoundingCircle(1, 4, 2)).withVelocity(Vector2(10, 0)).withFriction(Friction.zero)
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c2, c2) -> Batch(),
        IndexedCollider(1, c3, c3) -> Batch()
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c2,
        c3
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("(Circles) solveCollisions - two dynamic - collide") {
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 = Collider.Circle(tag, BoundingCircle(8, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(4, 4).withVelocity(-10, 0),
        c5.withPosition(9, 4).withVelocity(10, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - two dynamic - collide") {
    val b1 = Collider.Box(tag, BoundingBox(3, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val b2 = Collider.Box(tag, BoundingBox(6, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, b1, b1) -> Batch(b2),
        IndexedCollider(1, b2, b2) -> Batch(b1)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        b1.withPosition(3, 2).withVelocity(10, 0), // These seem weird, it's because of the initial velocities.
        b2.withPosition(7, 2).withVelocity(10, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("(Circles) solveCollisions - two dynamic - collide - zero time delta - displacement resolved") {
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 = Collider.Circle(tag, BoundingCircle(8, 4, 2)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(4, 4).withVelocity(-10, 0),
        c5.withPosition(9, 4).withVelocity(10, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - two dynamic - collide - zero time delta - displacement resolved") {
    val c3 = Collider.Box(tag, BoundingBox(3, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 = Collider.Box(tag, BoundingBox(6, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(3, 2).withVelocity(10, 0), // These seem weird, it's because of the initial velocities.
        c5.withPosition(7, 2).withVelocity(10, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
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
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(4.25, 4).withVelocity(-5, 0),
        c5.withPosition(8.75, 4).withVelocity(5, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
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
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(2.75, 2).withVelocity(5, 0), // These seem weird, it's because of the initial velocities.
        c5.withPosition(6.75, 2).withVelocity(5, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("(Circles) solveCollisions - one dynamic one static - collide") {
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withVelocity(Vector2(10, 0)).withFriction(Friction.zero)
    val c5 =
      Collider.Circle(tag, BoundingCircle(8, 4, 2)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(3, 4).withVelocity(-10, 0),
        c5
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - one dynamic one static - collide") {
    val c3 = Collider.Box(tag, BoundingBox(3, 2, 4, 4)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val c5 =
      Collider.Box(tag, BoundingBox(6, 2, 4, 4)).withVelocity(Vector2(-10, 0)).makeStatic.withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(3, 2).withVelocity(10, 0), // These seem weird, it's because of the initial velocities.
        c5
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("(Boxes) solveCollisions - one dynamic one static - collide - vertical") {
    val c3 = Collider.Box(tag, BoundingBox(2, 0, 3, 3)).withVelocity(Vector2(0, 10)).withFriction(Friction.zero)
    val c5 =
      Collider.Box(tag, BoundingBox(0, 2, 8, 2)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c3, c3) -> Batch(c5),
        IndexedCollider(1, c5, c5) -> Batch(c3)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c3.withPosition(2, -2).withVelocity(0, -10),
        c5
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }

  test("Example: A circle coming to rest on top of a box, as it reaches the edge, should fall off not bounce back.") {
    val circle =
      Collider.Circle(tag, BoundingCircle(0, -1, 1)).withVelocity(Vector2(-10, 0)).withFriction(Friction.zero)
    val box =
      Collider.Box(tag, BoundingBox(0, 0, 3, 3)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    assert(circle.hitTest(box))

    val collisions =
      Batch(
        IndexedCollider(0, circle, circle) -> Batch(box)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        circle.withPosition(-0.0855, -1.1425).withVelocity(-10, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
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
        IndexedCollider(0, box, box) -> Batch(circle)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        box.withPosition(1.4284, -4.4284).withVelocity(7.07106, -7.07106)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
  }

  test("Example: From the top, striking the corner") {
    val circle =
      Collider.Circle(tag, BoundingCircle(0, -0.5, 1)).withVelocity(Vector2(-10, 10)).withFriction(Friction.zero)
    val box =
      Collider.Box(tag, BoundingBox(0, 0, 3, 3)).withVelocity(Vector2(0, 0)).makeStatic.withFriction(Friction.zero)

    assert(circle.hitTest(box))

    val collisions =
      Batch(
        IndexedCollider(0, circle, circle) -> Batch(box)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        circle.withPosition(-0.4901, -1.0651).withVelocity(-10, -10)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
  }

  test("Another test - solved collision") {
    val c1 = Collider.Circle(tag, BoundingCircle(3, 0, 2)).withVelocity(Vector2(1, 0)).withFriction(Friction.zero)
    val c2 = Collider.Circle(tag, BoundingCircle(6, 0, 2)).withVelocity(Vector2(-1, 0)).withFriction(Friction.zero)

    val collisions =
      Batch(
        IndexedCollider(0, c1, c1) -> Batch(c2),
        IndexedCollider(1, c2, c2) -> Batch(c1)
      )

    val actual =
      Solver.solveAllCollisions(collisions)

    val expected =
      Batch(
        c1.withPosition(2, 0).withVelocity(0, 0),
        c2.withPosition(7, 0).withVelocity(0, 0)
      )

    assert(clue(actual(0).proposed) ~== clue(expected(0)))
    assert(clue(actual(1).proposed) ~== clue(expected(1)))
  }
