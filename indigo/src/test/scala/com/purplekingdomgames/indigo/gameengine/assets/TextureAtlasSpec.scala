package com.purplekingdomgames.indigo.gameengine.assets

import org.scalatest.{FunSpec, Matchers}

class TextureAtlasSpec extends FunSpec with Matchers {

  describe("The texture atlas functions") {

    it("should be able to generate a list of power 2 values up to a max") {
      TextureAtlasFunctions.generateSupportedSizes(256) shouldEqual Set(1,2,4,8,16,32,64,128,256)
    }

    it("should be able to pick the right bucket for my image size") {
      TextureAtlasFunctions.pickPowerOfTwoSizeFor(TextureAtlasFunctions.generateSupportedSizes(256), 116, 24) shouldEqual 128
    }

  }

}
