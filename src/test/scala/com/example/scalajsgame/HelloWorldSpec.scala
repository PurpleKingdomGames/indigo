package com.example.scalajsgame

import org.scalatest.FunSpec

class HelloWorldSpec extends FunSpec {
  describe("Adding 1 to 1") {
    it("should equals 2"){
      assert(1+1 == 2)
    }
  }
}

