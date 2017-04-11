package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.PowerOfTwo
import org.scalatest.{FunSpec, Matchers}

class TextureAtlasSpec extends FunSpec with Matchers {

  describe("The texture atlas functions") {

    it("should be able to pick the right bucket for my image size") {
      TextureAtlasFunctions.pickPowerOfTwoSizeFor(TextureAtlas.supportedSizes, 116, 24).value shouldEqual 128
    }

    it("should be able to tell if an image is too big") {

      TextureAtlasFunctions.isTooBig(PowerOfTwo.Max, 10, 10) shouldEqual false
      TextureAtlasFunctions.isTooBig(PowerOfTwo._512, 1024, 1024) shouldEqual true

    }

    it("should be able to filter out images that are too large") {

      val original = List(
        ImageRef("a", 10, 10),
        ImageRef("b", 1024, 1024),
        ImageRef("c", 512, 512)
      )

      val expected = List(
        ImageRef("a", 10, 10),
        ImageRef("c", 512, 512)
      )

      TextureAtlasFunctions.filterTooLarge(PowerOfTwo._512)(original) shouldEqual expected

    }

    it("should be able to sort the images into descending size order") {

      val original = List(
        ImageRef("a", 10, 10),
        ImageRef("b", 1024, 1024),
        ImageRef("c", 512, 512),
        ImageRef("d", 700, 600)
      )

      val expected = List(
        TextureDetails(ImageRef("d", 700, 600), PowerOfTwo._1024),
        TextureDetails(ImageRef("b", 1024, 1024), PowerOfTwo._1024),
        TextureDetails(ImageRef("c", 512, 512), PowerOfTwo._512),
        TextureDetails(ImageRef("a", 10, 10), PowerOfTwo._16)
      )

      TextureAtlasFunctions.inflateAndSortByPowerOfTwo(original) shouldEqual expected

    }

    it("should be able to create a tree from one image") {

      val imageRef = ImageRef("b", 1024, 1024)
      val powerOfTwo = PowerOfTwo._1024

      val original = TextureDetails(imageRef, powerOfTwo)

      val expected = AtlasQuadNode(
        powerOfTwo,
        AtlasTexture(
          imageRef
        )
      )

      TextureAtlasFunctions.convertTextureDetailsToTree(original) shouldEqual expected

    }
  }

  describe("tree manipulation") {

    val a =
      AtlasQuadNode(
        PowerOfTwo._1024,
        AtlasTexture(
          ImageRef("a", 1024, 768)
        )
      )

    val b =
      AtlasQuadNode(
        PowerOfTwo._512,
        AtlasTexture(
          ImageRef("b", 500, 400)
        )
      )

    val c =
      AtlasQuadNode(
        PowerOfTwo._64,
        AtlasTexture(
          ImageRef("c", 62, 48)
        )
      )

    val d =
      AtlasQuadNode(
        PowerOfTwo._128,
        AtlasTexture(
          ImageRef("d", 62, 127)
        )
      )

    val aPlusB =
      AtlasQuadNode(
        PowerOfTwo._2048,
        AtlasQuadDivision(
          a,
          AtlasQuadNode(
            PowerOfTwo._1024,
            AtlasQuadDivision(
              b,
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512),
              AtlasQuadEmpty(PowerOfTwo._512)
            )
          ),
          AtlasQuadEmpty(PowerOfTwo._1024),
          AtlasQuadEmpty(PowerOfTwo._1024)
        )
      )

    it("should be able to merge two single item trees together") {

      val max = PowerOfTwo._4096

      TextureAtlasFunctions.mergeTrees(a, b, max) shouldEqual Some(aPlusB)

    }

    it("should be able to merge a single item tree with a more complex tree together") {

      val expected =
        AtlasQuadNode(
          PowerOfTwo._2048,
          AtlasQuadDivision(
            a,
            AtlasQuadNode(
              PowerOfTwo._1024,
              AtlasQuadDivision(
                b,
                AtlasQuadNode(
                  PowerOfTwo._512,
                  AtlasQuadDivision(
                    AtlasQuadNode(
                      PowerOfTwo._256,
                      AtlasQuadDivision(
                        AtlasQuadNode(
                          PowerOfTwo._128,
                          AtlasQuadDivision(
                            c,
                            AtlasQuadEmpty(PowerOfTwo._64),
                            AtlasQuadEmpty(PowerOfTwo._64),
                            AtlasQuadEmpty(PowerOfTwo._64)
                          )
                        ),
                        d,
                        AtlasQuadEmpty(PowerOfTwo._128),
                        AtlasQuadEmpty(PowerOfTwo._128)
                      )
                    ),
                    AtlasQuadEmpty(PowerOfTwo._256),
                    AtlasQuadEmpty(PowerOfTwo._256),
                    AtlasQuadEmpty(PowerOfTwo._256)
                  )
                ),
                AtlasQuadEmpty(PowerOfTwo._512),
                AtlasQuadEmpty(PowerOfTwo._512)
              )
            ),
            AtlasQuadEmpty(PowerOfTwo._1024),
            AtlasQuadEmpty(PowerOfTwo._1024)
          )
        )

      val max = PowerOfTwo._4096

      TextureAtlasFunctions.mergeTrees(aPlusB, c, max) match {
        case Some(aPlusBPlusC) => TextureAtlasFunctions.mergeTrees(aPlusBPlusC, d, max) shouldEqual Some(expected)
        case None => fail("Unexpected None...")
      }

    }

    it("should merge two trees where one is empty") {

      val max = PowerOfTwo._4096

      TextureAtlasFunctions.mergeTrees(a, AtlasQuadEmpty(PowerOfTwo._128), max) shouldEqual Some(a)
      TextureAtlasFunctions.mergeTrees(AtlasQuadEmpty(PowerOfTwo._128), b, max) shouldEqual Some(b)

    }

    it("should not merge tree B into empty tree A which cannot accommodate") {

      val a = AtlasQuadNode(PowerOfTwo._4, AtlasQuadDivision.empty(PowerOfTwo._2))
      val b = AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef("b", 128, 128)))

      TextureAtlasFunctions.mergeTreeBIntoA(a, b) shouldEqual None

    }

    it("should be able to merge tree B into empty tree A which can accommodate") {

      val a = AtlasQuadNode(PowerOfTwo._256, AtlasQuadDivision.empty(PowerOfTwo._128))
      val b = AtlasQuadNode(PowerOfTwo._128, AtlasTexture(ImageRef("b", 128, 128)))

      val expected = Some(
        AtlasQuadNode(
          PowerOfTwo._256,
          AtlasQuadDivision(
            b,
            AtlasQuadEmpty(PowerOfTwo._128),
            AtlasQuadEmpty(PowerOfTwo._128),
            AtlasQuadEmpty(PowerOfTwo._128)
          )
        )
      )

      TextureAtlasFunctions.mergeTreeBIntoA(a, b) shouldEqual expected

    }

    it("should not merge two trees that would result in a texture too large") {

      val max = PowerOfTwo._1024

      TextureAtlasFunctions.mergeTrees(a, b, max) shouldEqual None

    }

    it("should be able to report if it can accomodate another tree of size") {

      aPlusB.canAccommodate(PowerOfTwo._1024) shouldEqual true

    }

  }

}
