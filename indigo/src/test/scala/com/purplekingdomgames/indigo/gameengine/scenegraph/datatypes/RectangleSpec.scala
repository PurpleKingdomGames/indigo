package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes
import org.scalatest.{FunSpec, Matchers}

class RectangleSpec extends FunSpec with Matchers {

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

}
