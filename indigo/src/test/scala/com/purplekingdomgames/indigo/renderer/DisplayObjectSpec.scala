package com.purplekingdomgames.indigo.renderer

import org.scalatest.{FunSpec, Matchers}

class DisplayObjectSpec extends FunSpec with Matchers {

  describe("Ordering and compressing a list of display objects") {

    val base = DisplayObject(1, 1, 0, 10, 10, "", 1, 1, 1, 1, false, false, SpriteSheetFrame.defaultOffset)

    it("should be able to order a list by depth") {

      val list = List(
        base.copy(z = 6),
        base.copy(z = 100),
        base.copy(z = -50),
        base.copy(z = 4),
        base.copy(z = 1),
        base.copy(z = 12),
        base.copy(z = 2)
      )

      val expected = List(
        base.copy(z = -50),
        base.copy(z = 1),
        base.copy(z = 2),
        base.copy(z = 4),
        base.copy(z = 6),
        base.copy(z = 12),
        base.copy(z = 100)
      ).reverse

      DisplayObject.sortByDepth(list) shouldEqual expected

    }

    it("should be able to compress a display object list") {

      val list: List[DisplayObject] = List(
        base.copy(imageRef = "a"),
        base.copy(imageRef = "a"),
        base.copy(imageRef = "a"),
        base.copy(imageRef = "b"),
        base.copy(imageRef = "b"),
        base.copy(imageRef = "a"),
        base.copy(imageRef = "b")
      )

      val result: List[CompressedDisplayObject] = DisplayObject.compress(list)

      result.length shouldEqual 4
      result(3).imageRef shouldEqual "a"
      result(2).imageRef shouldEqual "b"
      result(1).imageRef shouldEqual "a"
      result(0).imageRef shouldEqual "b"

      result(3).vertices.length shouldEqual 3 * (3 * 6)
      result(2).vertices.length shouldEqual 2 * (3 * 6)
      result(1).vertices.length shouldEqual 1 * (3 * 6)
      result(0).vertices.length shouldEqual 1 * (3 * 6)

      result(3).textureCoordinates.length shouldEqual 3 * (2 * 6)
      result(2).textureCoordinates.length shouldEqual 2 * (2 * 6)
      result(1).textureCoordinates.length shouldEqual 1 * (2 * 6)
      result(0).textureCoordinates.length shouldEqual 1 * (2 * 6)

      result(3).effectValues.length shouldEqual 3 * (4 * 6)
      result(2).effectValues.length shouldEqual 2 * (4 * 6)
      result(1).effectValues.length shouldEqual 1 * (4 * 6)
      result(0).effectValues.length shouldEqual 1 * (4 * 6)
    }

  }

}
