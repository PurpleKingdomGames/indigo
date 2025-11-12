package indigo.shared.shader

import indigo.shared.datatypes.Matrix4
import indigo.shared.datatypes.RGB
import indigo.shared.datatypes.RGBA

class ShaderPrimitiveTests extends munit.FunSuite {

  import ShaderPrimitive._

  test("can make an array of 'float'") {

    val actual =
      array(4, List(float(1), float(2), float(3), float(4))).toArray

    val expected =
      Array[Float](1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make an array of 'Float'") {

    val actual =
      array(4, List(1.0f, 2.0f, 3.0f, 4.0f)).toArray

    val expected =
      Array[Float](1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4, 0, 0, 0)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make an array of 'vec2'") {

    val actual =
      array(4, List(vec2(1), vec2(2), vec2(3), vec2(4))).toArray

    val expected =
      Array[Float](1, 1, 0, 0, 2, 2, 0, 0, 3, 3, 0, 0, 4, 4, 0, 0)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make an array of 'vec3'") {

    val actual =
      array(4, List(vec3(1), vec3(2), vec3(3), vec3(4))).toArray

    val expected =
      Array[Float](1, 1, 1, 0, 2, 2, 2, 0, 3, 3, 3, 0, 4, 4, 4, 0)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make an array of 'vec4'") {

    val actual =
      array(4, List(vec4(1), vec4(2), vec4(3), vec4(4))).toArray

    val expected =
      Array[Float](1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make an array of 'RGB'") {

    val actual =
      array(4, List(RGB(1, 1, 1), RGB(2, 2, 2), RGB(3, 3, 3), RGB(4, 4, 4))).toArray

    val expected =
      Array[Float](1, 1, 1, 0, 2, 2, 2, 0, 3, 3, 3, 0, 4, 4, 4, 0)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make an array of 'RGBA'") {

    val actual =
      array(4, List(RGBA(1, 1, 1, 1), RGBA(2, 2, 2, 2), RGBA(3, 3, 3, 3), RGBA(4, 4, 4, 4))).toArray

    val expected =
      Array[Float](1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make a rawArray") {

    val actual =
      rawArray(
        List[List[Float]](List(1, 1, 1, 1), List(2, 2, 2, 2), List(3, 3, 3, 3), List(4, 4, 4, 4)).flatten
      ).toArray

    val expected =
      Array[Float](1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4)

    assertEquals(actual.toList, expected.toList)

  }

  test("can make an array from 'mat4'") {

    val actual =
      mat4.fromMatrix4(Matrix4.identity).toArray

    val expected =
      List[List[Float]](List(1, 0, 0, 0), List(0, 1, 0, 0), List(0, 0, 1, 0), List(0, 0, 0, 1)).flatten.toArray

    assertEquals(actual.toList, expected.toList)

  }

}
