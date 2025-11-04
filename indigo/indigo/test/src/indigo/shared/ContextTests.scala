package indigo.shared

class ContextTests extends munit.FunSuite:

  test("Should be able to change the start up data type of a context") {
    val actual =
      Context.initial
        .withStartUpData(10)
        .withStartUpData("Hello")
        .startUpData

    val expected =
      "Hello"

    assertEquals(actual, expected)
  }
