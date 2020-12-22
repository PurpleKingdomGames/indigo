package indigo.shared.display

import indigo.shared.datatypes.{Vector3, Vector4, Matrix4}

class Vector4Tests extends munit.FunSuite {

  test("Multiplying a Vector4 by a Matrix4.should work with identity") {

    val v = Vector4(1, 2, 3, 4)

    val m = Matrix4.identity

    assertEquals(v.applyMatrix4(m), v)
    assertEquals(m.transform(v), Vector3(1, 2, 3))
  }

  test("Multiplying a Vector4 by a Matrix4.should translate...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.translate(Vector3(5, 10, 20))

    assertEquals(v.applyMatrix4(m), Vector4.position(15, 20, 30))
    assertEquals(m.transform(v), Vector3(15, 20, 30))
  }

  test("Multiplying a Vector4 by a Matrix4.should scale...") {

    val v = Vector4.position(10, 10, 1)

    val m = Matrix4.identity.scale(Vector3(2, 2, 2))

    assertEquals(v.applyMatrix4(m), Vector4.position(20, 20, 2))
    assertEquals(m.transform(v), Vector3(20, 20, 2))
  }

  test("Multiplying a Vector4 by a Matrix4.should translate then scale...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.translate(Vector3(10, 10, 10)).scale(Vector3(2, 2, 2))

    assertEquals(v.applyMatrix4(m), Vector4.position(40, 40, 40))
    assertEquals(m.transform(v), Vector3(40, 40, 40))
  }

  test("Multiplying a Vector4 by a Matrix4.should scale then translate...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.scale(Vector3(2, 2, 2)).translate(Vector3(10, 10, 10))

    assertEquals(v.applyMatrix4(m), Vector4.position(30, 30, 30))
    assertEquals(m.transform(v), Vector3(30, 30, 30))
  }

  test("approx equal") {
    assert(Vector4(5.0, 5.0, 5.0, 5.0) ~== Vector4(4.999999, 5.00001, 5.0, 4.9999))
    assert(!(Vector4(5.0, 5.0, 5.0, 5.0) ~== Vector4(-4.999999, 5.00001, 5.0, 5.0)))
  }

}
