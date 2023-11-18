package indigoplugin.generators

class EmbedDataTests extends munit.FunSuite {

  test("Create a DataFrame") {

    val rows =
      EmbedData.extractRows(
        List(
          "name,game,highscore,allowed",
          "bob,tron,10000.00,true",
          "Fred,tanks,476,false"
        ),
        ","
      )

    val actual =
      DataFrame.fromRows(rows)

    val expectedHeaders =
      List(
        DataType.StringData("name"),
        DataType.StringData("game"),
        DataType.StringData("highscore"),
        DataType.StringData("allowed")
      )

    val expectedRows =
      List(
        List(
          DataType.StringData("bob"),
          DataType.StringData("tron"),
          DataType.DoubleData(10000.0),
          DataType.BooleanData(true)
        ),
        List(
          DataType.StringData("Fred"),
          DataType.StringData("tanks"),
          DataType.DoubleData(476.0),
          DataType.BooleanData(false)
        )
      )

    assertEquals(actual.headers.toList, expectedHeaders)
    assertEquals(actual.rows.toList.map(_.toList), expectedRows)

    val actualEnum =
      actual.renderEnum("GameScores", None)

    val expectedEnum =
      """
      |enum GameScores(val game: String, val highscore: Double, val allowed: Boolean):
      |  case Bob extends GameScores("tron", 10000.0, true)
      |  case Fred extends GameScores("tanks", 476.0, false)
      """.stripMargin

    assertEquals(actualEnum.trim, expectedEnum.trim)

    val actualEnumWithExtends =
      actual.renderEnum("GameScores", Option("ScoreData"))

    val expectedEnumWithExtends =
      """
      |enum GameScores(val game: String, val highscore: Double, val allowed: Boolean) extends ScoreData:
      |  case Bob extends GameScores("tron", 10000.0, true)
      |  case Fred extends GameScores("tanks", 476.0, false)
      """.stripMargin

    assertEquals(actualEnumWithExtends.trim, expectedEnumWithExtends.trim)

    val actualMap =
      actual.renderMap("GameScores")

    val expectedMap =
      """
      |final case class GameScores(val game: String, val highscore: Double, val allowed: Boolean)
      |object GameScores:
      |  val data: Map[String, GameScores] =
      |    Map(
      |      "bob" -> GameScores("tron", 10000.0, true),
      |      "Fred" -> GameScores("tanks", 476.0, false)
      |    )
      """.stripMargin

    assertEquals(actualMap.trim, expectedMap.trim)

  }

  test("Extract row data - csv - simple") {
    val row = " abc,123, def,456.5 ,ghi789,true ,."

    val actual =
      EmbedData.extractRowData(row, ",")

    val expected =
      List(
        DataType.StringData("abc"),
        DataType.IntData(123),
        DataType.StringData("def"),
        DataType.DoubleData(456.5),
        DataType.StringData("ghi789"),
        DataType.BooleanData(true),
        DataType.StringData(".")
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
