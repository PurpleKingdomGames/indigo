package indigo.renderer

import indigo.gameengine.display.DisplayObject
import indigo.shared.EqualTo._

import utest._

object CompressedDisplayObjectTests extends TestSuite {

  val modifyZ: Int => DisplayObject => DisplayObject = newZ =>
    d => DisplayObject(d.x, d.y, newZ, d.width, d.height, d.imageRef, d.alpha, d.tintR, d.tintG, d.tintB, d.flipHorizontal, d.flipVertical, d.frame)

  val modifyImageRef: String => DisplayObject => DisplayObject = newImageRef =>
    d => DisplayObject(d.x, d.y, d.z, d.width, d.height, newImageRef, d.alpha, d.tintR, d.tintG, d.tintB, d.flipHorizontal, d.flipVertical, d.frame)

  val tests: Tests =
    Tests {
      "Ordering and compressing a list of display objects" - {

        val base = DisplayObject(1, 1, 0, 10, 10, "", 1, 1, 1, 1, false, false, SpriteSheetFrame.defaultOffset)

        "should be able to order a list by depth" - {

          val list = List(
            modifyZ(6)(base),
            modifyZ(100)(base),
            modifyZ(-50)(base),
            modifyZ(4)(base),
            modifyZ(1)(base),
            modifyZ(12)(base),
            modifyZ(2)(base)
          )

          val expected = List(
            modifyZ(-50)(base),
            modifyZ(1)(base),
            modifyZ(2)(base),
            modifyZ(4)(base),
            modifyZ(6)(base),
            modifyZ(12)(base),
            modifyZ(100)(base)
          ).reverse

          CompressedDisplayObject.sortByDepth(list) === expected ==> true

        }

        "should be able to compress a display object list" - {

          val list: List[DisplayObject] = List(
            modifyImageRef("a")(base),
            modifyImageRef("a")(base),
            modifyImageRef("a")(base),
            modifyImageRef("b")(base),
            modifyImageRef("b")(base),
            modifyImageRef("a")(base),
            modifyImageRef("b")(base)
          )

          val result: List[CompressedDisplayObject] = CompressedDisplayObject.compress(list)

          result.length ==> 4
          result(3).imageRef ==> "a"
          result(2).imageRef ==> "b"
          result(1).imageRef ==> "a"
          result(0).imageRef ==> "b"

          result(3).vertices.length ==> 3 * (3 * 6)
          result(2).vertices.length ==> 2 * (3 * 6)
          result(1).vertices.length ==> 1 * (3 * 6)
          result(0).vertices.length ==> 1 * (3 * 6)

          result(3).textureCoordinates.length ==> 3 * (2 * 6)
          result(2).textureCoordinates.length ==> 2 * (2 * 6)
          result(1).textureCoordinates.length ==> 1 * (2 * 6)
          result(0).textureCoordinates.length ==> 1 * (2 * 6)

          result(3).effectValues.length ==> 3 * (4 * 6)
          result(2).effectValues.length ==> 2 * (4 * 6)
          result(1).effectValues.length ==> 1 * (4 * 6)
          result(0).effectValues.length ==> 1 * (4 * 6)
        }

      }
    }
}
