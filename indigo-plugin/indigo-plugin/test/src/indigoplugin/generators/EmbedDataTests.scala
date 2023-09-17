package indigoplugin.generators

class EmbedDataTests extends munit.FunSuite {

  test("Extract row data - csv - simple") {
    val row = " abc,123, def,456.5 ,ghi789,true "

    val actual =
      EmbedData.extractRowData(row, ",")

    val expected =
      List(
        DataType.StringData("abc"),
        DataType.IntData(123),
        DataType.StringData("def"),
        DataType.DoubleData(456.5),
        DataType.StringData("ghi789"),
        DataType.BooleanData(true)
      )

    assertEquals(actual, expected)
  }

  test("Extract row data - md - simple") {
    val row = "abc | 123| def|456.5 |ghi789|true"

    val actual =
      EmbedData.extractRowData(row, "\\|")

    val expected =
      List(
        DataType.StringData("abc"),
        DataType.IntData(123),
        DataType.StringData("def"),
        DataType.DoubleData(456.5),
        DataType.StringData("ghi789"),
        DataType.BooleanData(true)
      )

    assertEquals(actual, expected)
  }

  test("Extract row data - csv - with quotes") {
    val row = """abc,"123,def",456,ghi789"""

    val actual =
      EmbedData.extractRowData(row, ",")

    val expected =
      List(
        DataType.StringData("abc"),
        DataType.StringData("123,def"),
        DataType.IntData(456),
        DataType.StringData("ghi789")
      )

    assertEquals(actual, expected)
  }

  test("Extract row data - csv - with double quotes and single quotes") {
    val row = """abc,"123,'def'",456,ghi789"""

    val actual =
      EmbedData.extractRowData(row, ",")

    val expected =
      List(
        DataType.StringData("abc"),
        DataType.StringData("123,'def'"),
        DataType.IntData(456),
        DataType.StringData("ghi789")
      )

    assertEquals(actual, expected)
  }

  test("Extract row data - csv - with single quotes and double quotes") {
    val row = """abc,'123,"def"',456,ghi789"""

    val actual =
      EmbedData.extractRowData(row, ",")

    val expected =
      List(
        DataType.StringData("abc"),
        DataType.StringData("123,\"def\""),
        DataType.IntData(456),
        DataType.StringData("ghi789")
      )

    assertEquals(actual, expected)
  }

}
