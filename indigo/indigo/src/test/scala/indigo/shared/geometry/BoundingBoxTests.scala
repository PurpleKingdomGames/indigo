package indigo.shared.geometry

import indigo.Vector2
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size

class BoundingBoxTests extends munit.FunSuite {

  test("approx equal") {
    assert(BoundingBox(5.0, 5.0, 5.0, 5.0) ~== BoundingBox(4.999999, 5.00001, 4.999999, 5.00001))
    assert(!(BoundingBox(5.0, 5.0, 5.0, 5.0) ~== BoundingBox(4.98, 5.00001, 4.98, 5.00001)))
  }

  test("creating rectangles.should be able to construct a bounding box from two vertices") {
    val pt1 = Vertex(5, 6)
    val pt2 = Vertex(1, 3)

    val expected = BoundingBox(1, 3, 4, 3)

    assert(BoundingBox.fromTwoVertices(pt1, pt2) ~== expected)
  }

  test("creating rectangles.should be able to construct a bounding box from a cloud of vertices") {
    // left 0, right 6, top 7, bottom 13
    val vertices: Batch[Vertex] =
      Batch(
        Vertex(4, 11),
        Vertex(6, 8),
        Vertex(2, 9),
        Vertex(1, 13),
        Vertex(3, 10),
        Vertex(0, 12),
        Vertex(5, 7)
      )

    val expected: BoundingBox =
      BoundingBox(0, 7, 6.001, 6.001)

    val actual: BoundingBox =
      BoundingBox.fromVertexCloud(vertices)

    assertEquals(actual == expected, true)

    assert(vertices.forall(actual.contains))
  }

  test("creating rectangles.should be able to construct a bounding box from a rectangle") {

    val rectangle = Rectangle(Point(10, 20), Size(30, 40))

    val actual = BoundingBox.fromRectangle(rectangle)

    val expected = BoundingBox(Vertex(10, 20), Vertex(30, 40))

    assertEquals(actual, expected)
    assertEquals(actual.toRectangle, rectangle)

  }

  test(
    "Expand to include two bounding boxes.should return the original bounding box when it already encompasses the second one"
  ) {
    val a = BoundingBox(10, 20, 100, 200)
    val b = BoundingBox(20, 20, 50, 50)

    assertEquals(BoundingBox.expandToInclude(a, b) == a, true)
  }

  test("Expand to include two bounding boxes.should expand to meet the bounds of both") {
    val a = BoundingBox(10, 10, 20, 20)
    val b = BoundingBox(100, 100, 100, 100)

    assertEquals(BoundingBox.expandToInclude(a, b) == BoundingBox(10, 10, 190, 190), true)
  }

  test("expand a bounding box with negative size") {
    val a = BoundingBox(10, 10, -20, -20)

    assertEquals(a.expand(10), BoundingBox(20, 20, -40, -40))
  }

  test("expand a bounding box to include another bounding box with negative size") {
    val a = BoundingBox(50, 50, -20, -20)
    val b = BoundingBox(100, 100, 100, 100)

    assertEquals(BoundingBox.expandToInclude(a, b) == BoundingBox(30, 30, 170, 170), true)
  }

  test("expand a rectangle with negative start position") {
    val a = BoundingBox(-10, -10, 20, -20)

    assertEquals(a.expand(10), BoundingBox(-20, 0, 40, -40))
  }

  test("contract by a fixed amount") {
    val actual =
      BoundingBox(10, 20, 90, 80).contract(10)

    val expected =
      BoundingBox(20, 30, 70, 60)

    assertEquals(actual, expected)
  }

  test("contract by a fixed amount (negative)") {
    val actual =
      BoundingBox(-10, -20, 90, -80).contract(10)

    val expected =
      BoundingBox(0, -30, 70, -60)

    assertEquals(actual, expected)
  }

  test("intersecting vertices.should be able to detect if the point is inside the BoundingBox") {
    assertEquals(BoundingBox(0, 0, 10, 10).contains(Vertex(5, 5)), true)
  }

  test("intersecting vertices.should be able to detect that a point is outside the BoundingBox") {
    assertEquals(BoundingBox(0, 0, 10, 10).contains(Vertex(20, 5)), false)
  }

  test("Convert bounding box to line segments") {
    val expected =
      Batch(
        LineSegment(Vertex(0, 0), Vertex(0, 3)),
        LineSegment(Vertex(0, 3), Vertex(3, 3)),
        LineSegment(Vertex(3, 3), Vertex(3, 0)),
        LineSegment(Vertex(3, 0), Vertex(0, 0))
      )

    val actual =
      BoundingBox(0d, 0d, 3d, 3d).toLineSegments

    assertEquals(actual, expected)
  }

  test("intersecting lines.should find the intersection for the line passing through the bounding box") {
    val actual =
      BoundingBox(1, 1, 3, 3)
        .lineIntersectsAt(
          LineSegment((1d, 0d), (3d, 5d))
        )

    val expectd = Vertex(1.4, 1)

    assert(clue(actual).get ~== clue(expectd))
  }

  test("intersecting lines.should not find intersection for a line outside the bounding box") {
    assertEquals(
      BoundingBox(5, 5, 4, 4)
        .lineIntersectsAt(LineSegment((2d, 0d), (4d, 4d))),
      None
    )
  }

  test("intersecting lines.should not find intersection for a line inside the bounding box") {
    assertEquals(
      BoundingBox(5, 5, 4, 4)
        .lineIntersectsAt(LineSegment((6d, 6d), (7d, 7d))),
      None
    )
  }

  test("intersecting lines.detecting a hit") {
    assert(BoundingBox(1, 1, 3, 3).lineIntersects(LineSegment((1d, 2d), (5d, 2.5d))))
    assert(!BoundingBox(5, 5, 4, 4).lineIntersects(LineSegment((0d, 0d), (3d, 3d))))
  }

  test("intersecting lines.detecting a hit through zero coord") {

    val l = LineSegment(Vertex(1), Vertex(-1))

    // In the original test case,
    // This worked...
    assert(BoundingBox(Vertex(0.1), Vertex(2)).lineIntersects(l))
    assertEquals(BoundingBox(Vertex(0.1), Vertex(2)).lineIntersectsAt(l), Some(Vertex(0.1)))

    // But this didn't...
    assert(BoundingBox(Vertex(0), Vertex(2)).lineIntersects(l))
    assertEquals(BoundingBox(Vertex(0), Vertex(2)).lineIntersectsAt(l), Some(Vertex(0)))

  }

  test("encompasing bounding box.should return true when A encompases B") {
    val a = BoundingBox(10, 10, 110, 110)
    val b = BoundingBox(20, 20, 10, 10)

    assertEquals(BoundingBox.encompassing(a, b), true)
  }

  test("encompasing bounding box.should return false when A does not encompass B") {
    val a = BoundingBox(20, 20, 10, 10)
    val b = BoundingBox(10, 10, 110, 110)

    assertEquals(BoundingBox.encompassing(a, b), false)
  }

  test("encompasing bounding box.should return true when A encompases B and B has a negative size") {
    val a = Rectangle(10, 10, 110, 110)
    val b = Rectangle(30, 30, -10, -10)

    assertEquals(Rectangle.encompassing(a, b), true)
  }

  test("encompasing bounding box.should return false when A and B merely intersect") {
    val a = BoundingBox(10, 10, 20, 200)
    val b = BoundingBox(15, 15, 100, 10)

    assertEquals(BoundingBox.encompassing(a, b), false)
  }

  test("overlapping bounding box.should return true when A encompases B") {
    // This is the encompassing test, but if the encompass, then they overlap.
    val a = BoundingBox(10, 10, 110, 110)
    val b = BoundingBox(20, 20, 10, 10)

    assertEquals(BoundingBox.overlapping(a, b), true)
  }

  test("overlapping bounding boxes.should return true when A overlaps B") {
    val a = BoundingBox(10, 10, 20, 20)
    val b = BoundingBox(15, 15, 100, 100)

    assertEquals(BoundingBox.overlapping(a, b), true)
  }

  test("overlapping bounding boxes.should return false when A and B do not overlap") {
    val a = BoundingBox(10, 10, 20, 20)
    val b = BoundingBox(100, 100, 100, 100)

    assertEquals(BoundingBox.overlapping(a, b), false)
  }

  test("overlapping rectangles.should return true when A overlaps B and A has a negative size.") {
    val a = BoundingBox(105, 105, -20, -20)
    val b = BoundingBox(10, 10, 90, 90)

    assertEquals(BoundingBox.overlapping(a, b), true)
  }

  test("overlapping rectangles.should return false when A and B do not overlap and A has a negative size.") {
    val a = BoundingBox(125, 125, -10, -10)
    val b = BoundingBox(10, 10, 90, 90)

    assertEquals(BoundingBox.overlapping(a, b), false)
  }

  test("overlaps BoundingCircle (encompasses)") {
    val b = BoundingBox(Vertex(0, 0), Vertex(500))
    val c = BoundingCircle(Vertex(250), 10)

    assert(b.overlaps(c) == true)
  }

  test("overlaps BoundingCircle (edge)") {
    val b = BoundingBox(Vertex(0, 0), Vertex(500))
    val c = BoundingCircle(Vertex(505), 10)

    assert(b.overlaps(c) == true)
  }

  test("overlaps BoundingCircle (doesn't overlap)") {
    val b = BoundingBox(Vertex(0, 0), Vertex(500))
    val c = BoundingCircle(Vertex(600), 10)

    assert(b.overlaps(c) == false)
  }

  test("overlaps LineSegment") {
    val b = BoundingBox(Vertex(0, 0), Vertex(500))
    val l = LineSegment((-1.0, -1.0), (10.0, 10.0))

    assert(b.overlaps(l))
  }

  test("Expand should be able to expand in size by a given amount") {
    val a = BoundingBox(10, 10, 20, 20)
    val b = BoundingBox(0, 10, 100, 5)

    assertEquals(BoundingBox.expand(a, 10) == BoundingBox(0, 0, 40, 40), true)
    assertEquals(BoundingBox.expand(b, 50) == BoundingBox(-50, -40, 200, 105), true)
  }

  test("signed distance function") {
    val bb = BoundingBox(10, 20, 30, 40)

    // TL
    val tl = Vertex(5, 5)
    assertEquals(bb.sdf(tl), tl.distanceTo(bb.topLeft))

    // T
    val t = Vertex(15, 0)
    assertEquals(bb.sdf(t), t.distanceTo(Vertex(15, 20)))

    // TR
    val tr = Vertex(45, 5)
    assertEquals(bb.sdf(tr), tr.distanceTo(bb.topRight))

    // ML
    val ml = Vertex(2, 25)
    assertEquals(bb.sdf(ml), ml.distanceTo(Vertex(10, 25)))

    // M
    val m = Vertex(15, 25)
    assertEquals(bb.sdf(m), -m.distanceTo(Vertex(10, 25)))

    // MR
    val mr = Vertex(55, 30)
    assertEquals(bb.sdf(mr), mr.distanceTo(Vertex(40, 30)))

    // BL
    val bl = Vertex(5, 70)
    assertEquals(bb.sdf(bl), bl.distanceTo(bb.bottomLeft))

    // B
    val b = Vertex(20, 80)
    assertEquals(bb.sdf(b), b.distanceTo(Vertex(20, 60)))

    // BR
    val br = Vertex(45, 70)
    assertEquals(bb.sdf(br), br.distanceTo(bb.bottomRight))

  }

  test("should be able to find edges (positive)") {
    val a = BoundingBox(10, 20, 30, 40)

    assert(a.left == 10)
    assert(a.right == 40)
    assert(a.top == 20)
    assert(a.bottom == 60)
  }

  test("should be able to find edges (negative)") {
    val a = BoundingBox(10, 20, -30, -40)

    assert(a.left == -20)
    assert(a.right == 10)
    assert(a.top == -20)
    assert(a.bottom == 20)
  }

  test("LineSegment reflecton") {

    val ray = LineSegment((6.0, 1.0), (1.0, 6.0))
    val box = BoundingBox(3, 3, 2, 2)

    val actual =
      box.reflect(ray).get

    assert(clue(actual.at) ~== clue(Vertex(4.0, 3.0)))
    assert(clue(actual.normal) ~== clue(Vector2(0, -1)))
    assert(clue(actual.incident) ~== clue(Vector2(-0.7071, 0.7071)))
    assert(clue(actual.reflected) ~== clue(Vector2(-0.7071, -0.7071)))
    assert(clue(actual.toLineSegment) ~== clue(LineSegment((4.0, 3.0), (3.2928, 2.2928))))
    assert(clue(actual.toLineSegment(10)) ~== clue(LineSegment((4.0, 3.0), (-3.07106, -4.07106))))

  }

  test("LineSegment reflecton - along x-axis") {

    val ray = LineSegment((0.0, 2.0), (4.0, 2.0))
    val box = BoundingBox(3, 1, 3, 3)

    val actual =
      box.reflect(ray).get

    assert(clue(actual.at) ~== clue(Vertex(3.0, 2.0)))
    assert(clue(actual.normal) ~== clue(Vector2(-1, 0)))
    assert(clue(actual.incident) ~== clue(Vector2(1, 0)))
    assert(clue(actual.reflected) ~== clue(Vector2(-1, 0)))

  }

  test("resize") {
    assertEquals(BoundingBox(10, 10, 10, 10).resize(Vertex(20, 20)), BoundingBox(10, 10, 20, 20))
    assertEquals(BoundingBox(10, 10, 10, 10).resize(Vector2(20, 20)), BoundingBox(10, 10, 20, 20))
    assertEquals(BoundingBox(10, 10, 10, 10).resize(20, 20), BoundingBox(10, 10, 20, 20))
    assertEquals(BoundingBox(10, 10, 10, 10).resize(20), BoundingBox(10, 10, 20, 20))
  }

  test("resizeBy") {
    assertEquals(BoundingBox(10, 10, 10, 10).resizeBy(Vertex(20, 20)), BoundingBox(10, 10, 30, 30))
    assertEquals(BoundingBox(10, 10, 10, 10).resizeBy(Vector2(20, 20)), BoundingBox(10, 10, 30, 30))
    assertEquals(BoundingBox(10, 10, 10, 10).resizeBy(20, 20), BoundingBox(10, 10, 30, 30))
    assertEquals(BoundingBox(10, 10, 10, 10).resizeBy(20), BoundingBox(10, 10, 30, 30))
  }

}
