package indigo.physics

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

  test("velocityDirectionAngle") {
    val c = Collider("a", BoundingCircle(0, 0, 5))

    assert(closeEnough(clue(c.withVelocity(Vector2(0.0, 0.0)).velocityDirectionAngle.toDegrees), clue(0.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(1.0, 0.0)).velocityDirectionAngle.toDegrees), clue(0.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(0.0, 1.0)).velocityDirectionAngle.toDegrees), clue(90.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(-1.0, 0.0)).velocityDirectionAngle.toDegrees), clue(180.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(0.0, -1.0)).velocityDirectionAngle.wrap.toDegrees), clue(270.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(0.0, -1.0)).velocityDirectionAngle.toDegrees), clue(-90.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(1.0, 1.0)).velocityDirectionAngle.toDegrees), clue(45.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(-1.0, 1.0)).velocityDirectionAngle.toDegrees), clue(135.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(-1.0, -1.0)).velocityDirectionAngle.wrap.toDegrees), clue(225.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(-1.0, -1.0)).velocityDirectionAngle.toDegrees), clue(-135.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(1.0, -1.0)).velocityDirectionAngle.wrap.toDegrees), clue(315.0)))
    assert(closeEnough(clue(c.withVelocity(Vector2(1.0, -1.0)).velocityDirectionAngle.toDegrees), clue(-45.0)))
  }

  def closeEnough(a: Double, b: Double): Boolean =
    Math.abs(a - b) <= 0.0001
