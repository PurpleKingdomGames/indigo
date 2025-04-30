package indigo.physics.simulation

import indigo.*
import indigo.physics.*
import indigo.syntax.*

class MoveTests extends munit.FunSuite:

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

  test("moveColliders") {
    val collider =
      Collider.Circle(tag, BoundingCircle(10, 10, 5)).withVelocity(Vector2(0, 10)).withFriction(Friction.zero)

    val colliders =
      Batch(collider)

    val w = World(
      colliders,
      Batch(Vector2(0)),
      Resistance.zero,
      settings
    )

    val actual =
      Move.colliders(1.second, w.colliders, w.combinedForce, w.resistance)

    val expected =
      Batch(
        IndexedCollider(
          0,
          collider,
          collider.moveTo(Vertex(10, 20))
        )
      )

    assertEquals(actual, expected)
  }

  test("moveColliders - respect terminal velocity") {
    val colliderA =
      Collider
        .Circle(tag, BoundingCircle(10, 10, 5))
        .withVelocity(Vector2(0, -10))
        .withTerminalVelocity(Vector2(5))
        .withFriction(Friction.zero)
    val colliderB =
      Collider
        .Circle(tag, BoundingCircle(10, 10, 5))
        .withVelocity(Vector2(0, 10))
        .withTerminalVelocity(Vector2(5))
        .withFriction(Friction.zero)

    val colliders =
      Batch(colliderA, colliderB)

    val w = World(
      colliders,
      Batch(Vector2(0)),
      Resistance.zero,
      settings
    )

    val actual =
      Move.colliders(1.second, w.colliders, w.combinedForce, w.resistance)

    val expected =
      Batch(
        IndexedCollider(
          0,
          colliderA,
          colliderA.moveTo(Vertex(10, 5)).withVelocity(Vector2(0, -5))
        ),
        IndexedCollider(
          1,
          colliderB,
          colliderB.moveTo(Vertex(10, 15)).withVelocity(Vector2(0, 5))
        )
      )

    assertEquals(actual, expected)
  }
