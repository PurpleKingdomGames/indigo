package indigo.shared.display

import indigo.shared.EqualTo._
import indigo.shared.datatypes.{Vector4, Matrix4}

class Vector4Tests extends munit.FunSuite {

  test("Multiplying a Vector4 by a Matrix4.should work with identity") {

    val v = Vector4(1, 2, 3, 4)

    val m = Matrix4.identity

    assertEquals(v.applyMatrix4(m) === v, true)

  }

  test("Multiplying a Vector4 by a Matrix4.should translate...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.translate(5, 10, 20)

    assertEquals(v.applyMatrix4(m) === Vector4.position(15, 20, 30), true)

  }

  test("Multiplying a Vector4 by a Matrix4.should scale...") {

    val v = Vector4.position(10, 10, 1)

    val m = Matrix4.identity.scale(2, 2, 2)

    assertEquals(v.applyMatrix4(m) === Vector4.position(20, 20, 2), true)

  }

  test("Multiplying a Vector4 by a Matrix4.should translate then scale...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.translate(10, 10, 10).scale(2, 2, 2)

    assertEquals(v.applyMatrix4(m) === Vector4.position(40, 40, 40), true)

  }

  test("Multiplying a Vector4 by a Matrix4.should scale then translate...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.scale(2, 2, 2).translate(10, 10, 10)

    assertEquals(v.applyMatrix4(m) === Vector4.position(30, 30, 30), true)

  }

}
