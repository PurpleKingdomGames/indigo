package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

import org.scalatest.{FunSpec, Matchers}

class PointSpec extends FunSpec with Matchers {

  describe("Interpolation") {

    it("should be able to calculate a linear interpolation") {

      Point.linearInterpolation(Point(10, 10), Point(20, 20), 10, 5) shouldEqual Point(15, 15)
      Point.linearInterpolation(Point(10, 20), Point(20, 20), 10, 5) shouldEqual Point(15, 20)
      Point.linearInterpolation(Point(20, 10), Point(20, 20), 10, 5) shouldEqual Point(20, 15)
      Point.linearInterpolation(Point(10, 10), Point(20, 20), 10, 1) shouldEqual Point(11, 11)
      Point.linearInterpolation(Point(10, 10), Point(-10, -10), 100, 99) shouldEqual Point(-9, -9)

    }

  }
}
