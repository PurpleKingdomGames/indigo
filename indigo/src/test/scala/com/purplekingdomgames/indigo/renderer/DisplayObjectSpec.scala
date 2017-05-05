package com.purplekingdomgames.indigo.renderer

import org.scalatest.{FunSpec, Matchers}

class DisplayObjectSpec extends FunSpec with Matchers {

  describe("Coordinate transformations") {

    it("should be able to transform texture coordinates") {

      val base = List(
        Vector2(0, 0),
        Vector2(0, 1),
        Vector2(1, 0)
      )

      val expected = List(
        Vector2(10, 10),
        Vector2(10, 12),
        Vector2(12, 10)
      )

      val actual = DisplayObject.transformTextureCoords(
        baseCoords = base,
        translate = Vector2(10, 10),
        scale = Vector2(2, 2)
      )

      actual shouldEqual expected

    }

    it("should be able to convert the texture coord list to a js array") {

      val base = List(
        Vector2(1, 2),
        Vector2(3, 4),
        Vector2(5, 6)
      )

      val expected = scalajs.js.Array[Int](1, 2, 3, 4, 5, 6)

      val actual = DisplayObject.convertCoordsToJsArray(base)

      actual.toList shouldEqual expected.toList

    }

  }

}
