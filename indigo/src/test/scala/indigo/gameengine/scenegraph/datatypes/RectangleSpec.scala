package indigo.gameengine.scenegraph.datatypes

import org.scalatest.{FunSpec, Matchers}

class RectangleSpec extends FunSpec with Matchers {

  describe("creating rectangles") {

    it("should be able to construct a rectangle from two points") {
      val pt1 = Point(5, 6)
      val pt2 = Point(1, 3)

      val expected = Rectangle(1, 3, 4, 3)

      Rectangle.fromTwoPoints(pt1, pt2) shouldEqual expected
    }

  }

  describe("Expand to include two rectangles") {

    it("should return the original rectangle when it already encompasses the second one") {
      val a = Rectangle(10, 20, 100, 200)
      val b = Rectangle(20, 20, 50, 50)

      Rectangle.expandToInclude(a, b) shouldEqual a
    }

    it("should expand to meet the bounds of both") {
      val a = Rectangle(10, 10, 20, 20)
      val b = Rectangle(100, 100, 100, 100)

      Rectangle.expandToInclude(a, b) shouldEqual Rectangle(10, 10, 190, 190)
    }

  }

  describe("intersecting rectangles") {

    it("should return true when two rectangles intersect") {
      val a = Rectangle(10, 10, 25, 25)
      val b = Rectangle(20, 20, 50, 50)

      Rectangle.intersecting(a, b) shouldEqual true
    }

    it("should return false when they do not intersect") {
      val a = Rectangle(10, 10, 20, 20)
      val b = Rectangle(100, 100, 100, 100)

      Rectangle.intersecting(a, b) shouldEqual false
    }
  }

  describe("encompasing rectangles") {
    it("should return true when A encompases B") {
      val a = Rectangle(10, 10, 110, 110)
      val b = Rectangle(20, 20, 10, 10)

      Rectangle.encompassing(a, b) shouldEqual true
    }

    it("should return false when A does not encompass B") {
      val a = Rectangle(20, 20, 10, 10)
      val b = Rectangle(10, 10, 110, 110)

      Rectangle.encompassing(a, b) shouldEqual false
    }

    it("should return false when A and B merely intersect") {
      val a = Rectangle(10, 10, 20, 200)
      val b = Rectangle(15, 15, 100, 10)

      Rectangle.encompassing(a, b) shouldEqual false
    }
  }

  describe("overlapping rectangles") {
    it("should return true when A overlaps B") {
      val a = Rectangle(10, 10, 20, 20)
      val b = Rectangle(15, 15, 100, 100)

      Rectangle.overlapping(a, b) shouldEqual true
    }

    it("should return false when A and B do not overlap") {
      val a = Rectangle(10, 10, 20, 20)
      val b = Rectangle(100, 100, 100, 100)

      Rectangle.overlapping(a, b) shouldEqual false
    }
  }

}
