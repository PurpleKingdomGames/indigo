package indigo.physics

import indigo.physics.*
import indigo.shared.datatypes.Vector2
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.BoundingCircle
import indigo.shared.geometry.Vertex

class ColliderTests extends munit.FunSuite:

  test("Collision - Circle vs Circle") {
    val a = Collider("a", BoundingCircle(0, 0, 5))
    val b = Collider("b", BoundingCircle(9, 0, 5))

    assert(clue(a).hitTest(clue(b)))
    assert(!clue(a).hitTest(clue(b.moveBy(Vertex(2, 0)))))
  }

  test("Collision - Circle vs Box") {
    val a = Collider("a", BoundingCircle(0, 0, 5))
    val b = Collider("b", BoundingBox(4, 0, 5, 5))

    assert(clue(a).hitTest(clue(b)))
    assert(!clue(a).hitTest(clue(b.moveBy(Vertex(2, 0)))))
  }

  test("Collision - Box vs Box") {
    val a = Collider("a", BoundingBox(0, 0, 5, 5))
    val b = Collider("b", BoundingBox(4, 0, 5, 5))

    assert(clue(a).hitTest(clue(b)))
    assert(!clue(a).hitTest(clue(b.moveBy(Vertex(2, 0)))))
  }
