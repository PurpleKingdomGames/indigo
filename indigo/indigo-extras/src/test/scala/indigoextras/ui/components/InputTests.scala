package indigoextras.ui.components

class InputTests extends munit.FunSuite:

  test("Input.findCursorPosition 1:1") {
    val text     = "Hello, world!"
    val actual   = Input.findCursorPosition(3, text, (str: String) => str.length)
    val expected = 3

    assertEquals(actual, expected)
    assertEquals(text.take(actual), "Hel")
  }

  test("Input.findCursorPosition, chars have irregular widths - click < 50% across a char") {
    val text                   = "Indigo"
    val charLengths: List[Int] = List(11, 14, 12, 13, 15, 10)
    val midCharPosition        = 11 + 14 + ((12 / 4) - 1)

    val calculateLineLength: (String) => Int = (str: String) => str.toList.zip(charLengths).map(_._2).sum

    val actual   = Input.findCursorPosition(midCharPosition, text, calculateLineLength)
    val expected = 2

    assertEquals(text.take(actual), "In")
    assertEquals(actual, expected)
  }

  test("Input.findCursorPosition, chars have irregular widths - click >= 50% across a char") {
    val text                   = "Indigo"
    val charLengths: List[Int] = List(11, 14, 12, 13, 15, 10)
    val midCharPosition        = 11 + 14 + ((12 / 2) + 2)

    val calculateLineLength: (String) => Int = (str: String) => str.toList.zip(charLengths).map(_._2).sum

    val actual   = Input.findCursorPosition(midCharPosition, text, calculateLineLength)
    val expected = 3

    assertEquals(text.take(actual), "Ind")
    assertEquals(actual, expected)
  }
