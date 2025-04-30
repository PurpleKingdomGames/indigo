package indigo.physics.simulation

import indigo.*
import indigo.physics.*

class CollisionsTests extends munit.FunSuite:

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

  test("findCollisionGroups") {
    val c1 = Collider.Circle(tag, BoundingCircle(3, 2, 2)).withFriction(Friction.zero)   // overlaps c2 and c3
    val c2 = Collider.Circle(tag, BoundingCircle(1, 4, 2)).withFriction(Friction.zero)   // overlaps c1
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withFriction(Friction.zero)   // overlaps c1
    val c4 = Collider.Circle(tag, BoundingCircle(10, 20, 2)).withFriction(Friction.zero) // not touching
    val c5 = Collider.Circle(tag, BoundingCircle(8, 4, 2)).withFriction(Friction.zero)   // overlaps c3

    val idx1 = IndexedCollider(0, c1, c1)
    val idx2 = IndexedCollider(1, c2, c2)
    val idx3 = IndexedCollider(2, c3, c3)
    val idx4 = IndexedCollider(3, c4, c4)
    val idx5 = IndexedCollider(4, c5, c5)

    val indexed =
      Batch(
        idx1,
        idx2,
        idx3,
        idx4,
        idx5
      )

    val actual =
      Collisions.findCollisionGroups(indexed, Batch.empty, settings)

    val expected =
      Batch(
        idx1 -> Batch(c2, c3),
        idx2 -> Batch(c1),
        idx3 -> Batch(c1, c5),
        idx4 -> Batch(),
        idx5 -> Batch(c3)
      )

    assert(actual.length == expected.length)
    assert(actual.forall(p => expected.exists(q => q._1 == p._1 && q._2.forall(p._2.contains))))
  }

  test("findCollisionGroups, with transient") {
    val c1 = Collider.Circle(tag, BoundingCircle(3, 2, 2)).withFriction(Friction.zero)   // overlaps c2 and c3
    val c2 = Collider.Circle(tag, BoundingCircle(1, 4, 2)).withFriction(Friction.zero)   // overlaps c1
    val c3 = Collider.Circle(tag, BoundingCircle(5, 4, 2)).withFriction(Friction.zero)   // overlaps c1
    val c4 = Collider.Circle(tag, BoundingCircle(10, 20, 2)).withFriction(Friction.zero) // not touching
    val c5 = Collider.Circle(tag, BoundingCircle(8, 4, 2)).withFriction(Friction.zero)   // overlaps c3

    val idx1 = IndexedCollider(0, c1, c1)
    val idx2 = IndexedCollider(1, c2, c2)
    val idx3 = IndexedCollider(2, c3, c3)

    val indexed =
      Batch(
        idx1,
        idx2,
        idx3
      )

    val actual =
      Collisions.findCollisionGroups(
        indexed,
        Batch(c4, c5),
        settings
      )

    val expected =
      Batch(
        idx1 -> Batch(c2, c3),
        idx2 -> Batch(c1),
        idx3 -> Batch(c1, c5)
      )

    assert(actual.length == expected.length)
    assert(actual.forall(p => expected.exists(q => q._1 == p._1 && q._2.forall(p._2.contains))))
  }
