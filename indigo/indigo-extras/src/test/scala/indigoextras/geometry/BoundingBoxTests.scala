package indigoextras.geometry

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point

class BoundingBoxTests extends munit.FunSuite {

  test("creating rectangles.should be able to construct a bounding box from two vertices") {
    val pt1 = Vertex(5, 6)
    val pt2 = Vertex(1, 3)

    val expected = BoundingBox(1, 3, 4, 3)

    assertEquals(BoundingBox.fromTwoVertices(pt1, pt2) === expected, true)
  }

  test("creating rectangles.should be able to construct a bounding box from a cloud of vertices") {
    //left 0, right 6, top 7, bottom 13
    val vertices: List[Vertex] =
      List(
        Vertex(4, 11),
        Vertex(6, 8),
        Vertex(2, 9),
        Vertex(1, 13),
        Vertex(3, 10),
        Vertex(0, 12),
        Vertex(5, 7)
      )

    val expected: BoundingBox =
      BoundingBox(0, 7, 6, 6)

    val actual: BoundingBox =
      BoundingBox.fromVertexCloud(vertices)

    assertEquals(actual === expected, true)
  }

  test("creating rectangles.should be able to construct a bounding box from a rectangle") {

    val rectangle = Rectangle(Point(10, 20), Point(30, 40))

    val actual = BoundingBox.fromRectangle(rectangle)

    val expected = BoundingBox(Vertex(10, 20), Vertex(30, 40))

    assertEquals(actual, expected)
    assertEquals(actual.toRectangle, rectangle)

  }

  test("Expand to include two bounding boxes.should return the original bounding box when it already encompasses the second one") {
    val a = BoundingBox(10, 20, 100, 200)
    val b = BoundingBox(20, 20, 50, 50)

    assertEquals(BoundingBox.expandToInclude(a, b) === a, true)
  }

  test("Expand to include two bounding boxes.should expand to meet the bounds of both") {
    val a = BoundingBox(10, 10, 20, 20)
    val b = BoundingBox(100, 100, 100, 100)

    assertEquals(BoundingBox.expandToInclude(a, b) === BoundingBox(10, 10, 190, 190), true)
  }

  test("intersecting vertices.should be able to detect if the point is inside the BoundingBox") {
    assertEquals(BoundingBox(0, 0, 10, 10).contains(Vertex(5, 5)), true)
  }

  test("intersecting vertices.should be able to detect that a point is outside the BoundingBox") {
    assertEquals(BoundingBox(0, 0, 10, 10).contains(Vertex(20, 5)), false)
  }

  test("Convert bounding box to line segments") {
    val expected =
      List(
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
          LineSegment((1d, 0d), (4d, 4d))
        )
        .map { v =>
          // Round to 2 dp
          Vertex(Math.floor(v.x * 100) / 100, Math.floor(v.y * 100) / 100)
        }

    val expectd = Some(Vertex(1.74, 1))

    assertEquals(actual, expectd)
  }

  test("intersecting lines.should not find intersection for a line outside the bounding box") {
    assertEquals(
      BoundingBox(5, 5, 4, 4)
        .lineIntersectsAt(LineSegment((2d, 0d), (4d, 4d))),
      None
    )
  }

  test("intersecting lines.detecting a hit") {
    assert(BoundingBox(1, 1, 3, 3).lineIntersects(LineSegment((1d, 2d), (4d, 2.5d))))
    assert(!BoundingBox(5, 5, 4, 4).lineIntersects(LineSegment((0d, 0d), (3d, 3d))))
  }

  test("encompasing rectangles.should return true when A encompases B") {
    val a = BoundingBox(10, 10, 110, 110)
    val b = BoundingBox(20, 20, 10, 10)

    assertEquals(BoundingBox.encompassing(a, b), true)
  }

  test("encompasing rectangles.should return false when A does not encompass B") {
    val a = BoundingBox(20, 20, 10, 10)
    val b = BoundingBox(10, 10, 110, 110)

    assertEquals(BoundingBox.encompassing(a, b), false)
  }

  test("encompasing rectangles.should return false when A and B merely intersect") {
    val a = BoundingBox(10, 10, 20, 200)
    val b = BoundingBox(15, 15, 100, 10)

    assertEquals(BoundingBox.encompassing(a, b), false)
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

  test("Expand should be able to expand in size by a given amount") {
    val a = BoundingBox(10, 10, 20, 20)
    val b = BoundingBox(0, 10, 100, 5)

    assertEquals(BoundingBox.expand(a, 10) === BoundingBox(0, 0, 40, 40), true)
    assertEquals(BoundingBox.expand(b, 50) === BoundingBox(-50, -40, 200, 105), true)
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

}
