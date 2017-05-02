package com.purplekingdomgames.indigo.renderer

import org.scalatest.{FunSpec, Matchers}

class Vector4Spec extends FunSpec with Matchers {

  describe("Multiplying a Vector4 by a Matrix4") {

//    it("should work with identity") {
//
//      val v = Vector4(1, 2, 3)
//
//      val m = Matrix4.identity
//
//      v.applyMatrix4(m) shouldEqual v
//
//    }

    it("should translate...") {

      val v = Vector4(10, 10, 1)

      val m = Matrix4.identity.translate(5, 10, 20)

      v.applyMatrix4(m) shouldEqual Vector4(15, 20, 21)

    }

//    it("should scale...") {
//
//      val v = Vector4(10, 10, 1)
//
//      val m = Matrix4.identity.scale(2, 2, 2)
//
//      v.applyMatrix4(m) shouldEqual Vector4(20, 20, 2)
//
//    }
//
//    it("should translate then scale...") {
//
//      val v = Vector4(10, 10, 1)
//
//      val m = Matrix4.identity.translate(10, 10, 10).scale(2, 2, 2)
//
//      v.applyMatrix4(m) shouldEqual Vector4(40, 40, 22)
//
//    }
//
//    it("should scale then translate...") {
//
//      val v = Vector4(10, 10, 1)
//
//      val m = Matrix4.identity.scale(2, 2, 2).translate(10, 10, 10)
//
//      v.applyMatrix4(m) shouldEqual Vector4(30, 30, 12)
//
//    }

  }

}
