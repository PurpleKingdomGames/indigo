package com.purplekingdomgames.shared

import org.scalatest.{FunSpec, Matchers}

class ClearColorSpec extends FunSpec with Matchers {

  describe("Creating clear color instances") {

    it("Should convert from RGBA") {
      ClearColor.fromRGBA(0, 0, 0, 0) shouldEqual ClearColor.Black.withA(0)
      ClearColor.fromRGBA(255, 255, 255, 0) shouldEqual ClearColor.White.withA(0)
      ClearColor.fromRGBA(255, 0, 0, 255) shouldEqual ClearColor.Red
      ClearColor.fromRGBA(0, 255, 0, 255) shouldEqual ClearColor.Green
      ClearColor.fromRGBA(0, 0, 255, 255) shouldEqual ClearColor.Blue

      val transparent = ClearColor.fromRGBA(255, 255, 255, 127)
      (transparent.a > 0.48 && transparent.a < 0.52) shouldEqual true
    }

    it("should convert from RGB") {
      ClearColor.fromRGB(0, 0, 0) shouldEqual ClearColor.Black
      ClearColor.fromRGB(255, 255, 255) shouldEqual ClearColor.White
      ClearColor.fromRGB(255, 0, 0) shouldEqual ClearColor.Red
      ClearColor.fromRGB(0, 255, 0) shouldEqual ClearColor.Green
      ClearColor.fromRGB(0, 0, 255) shouldEqual ClearColor.Blue
    }

    it("should convert from Hexadecimal") {
      ClearColor.fromHexString("0xFF0000") shouldEqual ClearColor.Red
      ClearColor.fromHexString("FF0000") shouldEqual ClearColor.Red
      ClearColor.fromHexString("00FF00") shouldEqual ClearColor.Green
      ClearColor.fromHexString("0000FF") shouldEqual ClearColor.Blue
    }

  }

}
