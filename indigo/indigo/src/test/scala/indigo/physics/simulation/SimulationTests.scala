package indigo.physics.simulation

import indigo.*
import indigo.physics.*
import indigo.syntax.*

class SimulationTests extends munit.FunSuite:

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

  test("Simulation.updateWorld should update the world") {
    val c1 = Collider.Circle(tag, BoundingCircle(2, 0, 2)).withVelocity(Vector2(1, 0)).withFriction(Friction.zero)
    val c2 = Collider.Circle(tag, BoundingCircle(7, 0, 2)).withVelocity(Vector2(-1, 0)).withFriction(Friction.zero)

    val world = World(settings).withColliders(c1, c2)

    val actual =
      Simulation.updateWorld(world, Seconds(1), Batch.empty, settings)

    val expected =
      World(settings)
        .withColliders(
          c1.withPosition(2, 0).withVelocity(0, 0),
          c2.withPosition(7, 0).withVelocity(0, 0)
        )

    assert(clue(actual.unsafeGet.colliders(0)) ~== clue(expected.colliders(0)))
    assert(clue(actual.unsafeGet.colliders(1)) ~== clue(expected.colliders(1)))
  }

  test("Simulation.updateWorld should consider, but not return transient colliders") {
    val c1 = Collider.Circle(tag, BoundingCircle(0, 0, 2)).withVelocity(Vector2(1, 0)).withFriction(Friction.zero)
    val c2 =
      Collider.Circle(tag, BoundingCircle(4, 0, 2)).withFriction(Friction.zero).makeStatic

    val world = World(settings).withColliders(c1)

    val actual =
      Simulation.updateWorld(world, Seconds(1), Batch(c2), settings)

    val expected =
      World(settings)
        .withColliders(
          c1.withPosition(-1, 0).withVelocity(-1, 0)
        )

    assert(clue(actual.unsafeGet.colliders.length) == clue(1))
    assert(clue(actual.unsafeGet.colliders(0)) ~== clue(expected.colliders(0)))
  }
