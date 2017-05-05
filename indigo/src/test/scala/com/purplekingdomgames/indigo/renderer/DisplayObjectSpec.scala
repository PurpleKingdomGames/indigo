package com.purplekingdomgames.indigo.renderer

import org.scalatest.{FunSpec, Matchers}

class DisplayObjectSpec extends FunSpec with Matchers {

  describe("Texture coordinate transformations") {

    it("should be able to transform texture coordinates") {

      val base = List(
        Vector2(0, 0),
        Vector2(0, 1),
        Vector2(1, 0)
      )

      val expected = List(
        Vector2(10, 10),
        Vector2(10, 12),
        Vector2(12, 10)
      )

      val actual = DisplayObject.transformTextureCoords(
        baseCoords = base,
        translate = Vector2(10, 10),
        scale = Vector2(2, 2)
      )

      actual shouldEqual expected

    }

    it("should be able to convert the texture coord list to a js array") {

      val base = List(
        Vector2(1, 2),
        Vector2(3, 4),
        Vector2(5, 6)
      )

      val expected = scalajs.js.Array[Int](1, 2, 3, 4, 5, 6)

      val actual = DisplayObject.convertTextureCoordsToJsArray(base)

      actual.toList shouldEqual expected.toList

    }

  }

  describe("Vertex coordinate transformations") {

    val base = List(
      Vector4.position(0, 0, 0),
      Vector4.position(0, 1, 0),
      Vector4.position(1, 0, 0)
    )

    it("should be able to transform vertex coordinates with an identity matrix") {

      val matrix4 = Matrix4.identity

      val expected = List(
        Vector4.position(0, 0, 0),
        Vector4.position(0, 1, 0),
        Vector4.position(1, 0, 0)
      )

      DisplayObject.transformVertexCoords(base, matrix4) shouldEqual expected

    }

    it("should be able to transform vertex coordinates with a translation") {

      val matrix4 = Matrix4.identity.translate(10, 20, 30)

      val expected = List(
        Vector4.position(10, 20, 30),
        Vector4.position(10, 21, 30),
        Vector4.position(11, 20, 30)
      )

      DisplayObject.transformVertexCoords(base, matrix4) shouldEqual expected

    }

    it("should be able to transform vertex coordinates with a scale") {

      val matrix4 = Matrix4.identity.scale(2, 4, 1)

      val expected = List(
        Vector4.position(0, 0, 0),
        Vector4.position(0, 4, 0),
        Vector4.position(2, 0, 0)
      )

      DisplayObject.transformVertexCoords(base, matrix4) shouldEqual expected

    }

    it("should be able to transform vertex coordinates with a translation and a scale") {

      val matrix4 = Matrix4.identity.translate(10, 10, 10).scale(2, 2, 2)

      val expected = List(
        Vector4.position(20, 20, 20),
        Vector4.position(20, 22, 20),
        Vector4.position(22, 20, 20)
      )

      DisplayObject.transformVertexCoords(base, matrix4) shouldEqual expected

    }

    it("should be able to transform vertex coordinates with a scale and a translation") {

      val matrix4 = Matrix4.identity.scale(2, 2, 2).translate(10, 10, 10)

      val expected = List(
        Vector4.position(10, 10, 10),
        Vector4.position(10, 12, 10),
        Vector4.position(12, 10, 10)
      )

      DisplayObject.transformVertexCoords(base, matrix4) shouldEqual expected

    }

    it("should be able to transform vertex coordinates into non-magnified orthographic projection") {

      Vector4.position(0, 0, 0).applyMatrix4(Matrix4.orthographic(0, 100, 100, 0, -100, 100)) shouldEqual Vector4(-1,1,0,1)
      Vector4.position(1, 1, 1).applyMatrix4(Matrix4.orthographic(0, 100, 100, 0, -100, 100)) shouldEqual Vector4(-0.98,0.98,-0.01,1)
      Vector4.position(50, 50, 0).applyMatrix4(Matrix4.orthographic(0, 100, 100, 0, -100, 100)) shouldEqual Vector4(0,0,0,1)
      Vector4.position(100, 100, 0).applyMatrix4(Matrix4.orthographic(0, 100, 100, 0, -100, 100)) shouldEqual Vector4(1,-1,0,1)

    }

  }

}
