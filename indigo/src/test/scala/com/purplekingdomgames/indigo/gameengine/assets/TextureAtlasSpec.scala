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

    it("should be able to merge two trees together") {

      val expected =
        AtlasQuadNode(
          PowerOfTwo._2048,
          AtlasQuadDivision(
            a,
            AtlasQuadNode(
              PowerOfTwo._1024,
              AtlasQuadDivision(
                b,
                AtlasQuadEmpty,
                AtlasQuadEmpty,
                AtlasQuadEmpty
              )
            ),
            AtlasQuadEmpty,
            AtlasQuadEmpty
          )
        )

      val max = PowerOfTwo._4096

      TextureAtlasFunctions.mergeTrees(a, b, max) shouldEqual Some(expected)

    }

    it("should merge two trees where one is empty") {

      val max = PowerOfTwo._4096

      TextureAtlasFunctions.mergeTrees(a, AtlasQuadEmpty, max) shouldEqual Some(a)
      TextureAtlasFunctions.mergeTrees(AtlasQuadEmpty, b, max) shouldEqual Some(b)

    }

    it("should not merge two trees that would result in a texture too large") {

      val max = PowerOfTwo._1024

      TextureAtlasFunctions.mergeTrees(a, b, max) shouldEqual None

    }

  }

}
