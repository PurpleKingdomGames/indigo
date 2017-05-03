package com.purplekingdomgames.indigo.renderer

import org.scalatest.{FunSpec, Matchers}

class Vector4Spec extends FunSpec with Matchers {

  describe("Multiplying a Vector4 by a Matrix4") {

    it("should work with identity") {

      val v = Vector4(1, 2, 3, 4)

      val m = Matrix4.identity

      v.applyMatrix4(m) shouldEqual v

    }

    it("should translate...") {

      val v = Vector4.position(10, 10, 10)

      val m = Matrix4.identity.translate(5, 10, 20)

      v.applyMatrix4(m) shouldEqual Vector4.position(15, 20, 30)

    }

    it("should scale...") {

      val v = Vector4.position(10, 10, 1)

      val m = Matrix4.identity.scale(2, 2, 2)

      v.applyMatrix4(m) shouldEqual Vector4.position(20, 20, 2)

    }

    it("should translate then scale...") {

      val v = Vector4.position(10, 10, 10)

      val m = Matrix4.identity.translate(10, 10, 10).scale(2, 2, 2)

      v.applyMatrix4(m) shouldEqual Vector4.position(40, 40, 40)

    }

    it("should scale then translate...") {

      val v = Vector4.position(10, 10, 10)

      val m = Matrix4.identity.scale(2, 2, 2).translate(10, 10, 10)

      v.applyMatrix4(m) shouldEqual Vector4.position(30, 30, 30)

    }

  }

}
