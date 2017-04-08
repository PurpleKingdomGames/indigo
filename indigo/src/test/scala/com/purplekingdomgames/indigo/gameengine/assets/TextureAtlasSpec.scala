package com.purplekingdomgames.indigo.gameengine.assets

import org.scalatest.{FunSpec, Matchers}

class TextureAtlasSpec extends FunSpec with Matchers {

  describe("The texture atlas functions") {

    it("should be able to pick the right bucket for my image size") {
      TextureAtlasFunctions.pickPowerOfTwoSizeFor(TextureAtlas.supportedSizes, 116, 24) shouldEqual 128
    }



  }

}
