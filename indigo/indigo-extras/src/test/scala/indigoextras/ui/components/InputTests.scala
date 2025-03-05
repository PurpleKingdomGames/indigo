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

  test("Input.findCursorPosition, clicked beyond the length of the string") {
    val text     = "Go!"
    val actual   = Input.findCursorPosition(6, text, (str: String) => str.length)
    val expected = 3

    assertEquals(actual, expected)
    assertEquals(text.take(actual), "Go!")
  }

  test("Input.findCursorPosition, click into an empty string") {
    val text     = ""
    val actual   = Input.findCursorPosition(3, text, (str: String) => str.length)
    val expected = 0

    assertEquals(actual, expected)
    assertEquals(text.take(actual), "")
  }

  test("Input.deleteAt, delete at the start of the string") {
    val text     = "Hello, world!"
    val actual   = Input.deleteAt(text, 0)
    val expected = InputTextModified("ello, world!", 0)

    assertEquals(actual, expected)
  }

  test("Input.deleteAt, delete past the end of the string") {
    val text     = "Hello, world!"
    val actual   = Input.deleteAt(text, text.length) // Is beyond the end of the string
    val expected = InputTextModified("Hello, world!", text.length)

    assertEquals(actual, expected)
  }

  test("Input.deleteAt, delete in the middle of the string") {
    val text     = "Hello, world!"
    val actual   = Input.deleteAt(text, 3)
    val expected = InputTextModified("Helo, world!", 3)

    assertEquals(actual, expected)
  }

  test("Input.deleteAt, delete at the end of the string") {
    val text     = "Hello, world!"
    val actual   = Input.deleteAt(text, text.length - 1)
    val expected = InputTextModified("Hello, world", text.length - 1)

    assertEquals(actual, expected)
  }

  test("Input.backspaceAt, backspace at the start of the string") {
    val text     = "Hello, world!"
    val actual   = Input.backspaceAt(text, 0)
    val expected = InputTextModified("Hello, world!", 0)

    assertEquals(actual, expected)
  }

  test("Input.backspaceAt, backspace past the end of the string") {
    val text     = "Hello, world!"
    val actual   = Input.backspaceAt(text, text.length + 5) // Is beyond the end of the string
    val expected = InputTextModified("Hello, world", text.length - 1)

    assertEquals(actual, expected)
  }

  test("Input.backspaceAt, backspace in the middle of the string") {
    val text     = "Hello, world!"
    val actual   = Input.backspaceAt(text, 5)
    val expected = InputTextModified("Hell, world!", 4)

    assertEquals(actual, expected)
  }

  test("Input.backspaceAt, backspace at the end of the string") {
    val text     = "Hello, world!"
    val actual   = Input.backspaceAt(text, text.length)
    val expected = InputTextModified("Hello, world", text.length - 1)

    assertEquals(actual, expected)
  }

  test("Input.addTextAt, add text at the start of the string") {
    val text     = "Hello, world!"
    val actual   = Input.addTextAt(text, 0, "Hey, ", 100)
    val expected = InputTextModified("Hey, Hello, world!", 5)

    assertEquals(actual, expected)
  }

  test("Input.addTextAt, add text past the end of the string") {
    val text     = "Hello, world!"
    val actual   = Input.addTextAt(text, text.length + 5, "Hey, ", 100)
    val expected = InputTextModified("Hello, world!Hey, ", text.length + 5)

    assertEquals(actual, expected)
  }

  test("Input.addTextAt, add text in the middle of the string") {
    val text     = "Hello, world!"
    val actual   = Input.addTextAt(text, 7, "Hey, ", 100)
    val expected = InputTextModified("Hello, Hey, world!", 12)

    assertEquals(actual, expected)
  }

  test("Input.addTextAt, add text at the end of the string") {
    val text     = "Hello, world!"
    val actual   = Input.addTextAt(text, text.length, "Hey, ", 100)
    val expected = InputTextModified("Hello, world!Hey, ", text.length + 5)

    assertEquals(actual, expected)
  }
