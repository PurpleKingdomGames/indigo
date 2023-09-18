package indigoplugin.generators

import indigoplugin.IndigoGenerators

class GeneratorAcceptanceTests extends munit.FunSuite {

  val sourceCSV = os.pwd / "test-assets" / "data" / "stats.csv"
  val sourceMD  = os.pwd / "test-assets" / "data" / "stats.md"

  val targetDir = os.pwd / "out" / "indigo-plugin-generator-acceptance-test-output"

  override def beforeAll(): Unit = {
    if (os.exists(targetDir)) {
      os.remove.all(target = targetDir)
    }

    os.makeDir.all(targetDir)
  }

  test("Can generate an enum from a CSV file") {

    val files =
      IndigoGenerators
        .default(targetDir, "com.example.test")
        .embedCSV
        .asEnum("StatsEnum", sourceCSV)
        .toSources

    files.headOption match {
      case None =>
        fail("No file was generated")

      case Some(src) =>
        assert(clue(src) == clue(targetDir / "indigo-compile-codegen-output" / "StatsEnum.scala"))

        val actual = os.read(src)

        val expected =
          """
          |package com.example.test
          |
          |// DO NOT EDIT: Generated by Indigo.
          |
          |enum StatsEnum(val level: Int, val bonus: Int):
          |  case Intelligence extends StatsEnum(2, 4)
          |  case Strength extends StatsEnum(10, 0)
          |  case Fortitude extends StatsEnum(4, 1)
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

  test("Can generate a map from a markdown table file") {

    val files =
      IndigoGenerators
        .default(targetDir, "com.example.test")
        .embedMarkdownTable
        .asMap("StatsMap", sourceMD)
        .toSources

    files.headOption match {
      case None =>
        fail("No file was generated")

      case Some(src) =>
        assert(clue(src) == clue(targetDir / "indigo-compile-codegen-output" / "StatsMap.scala"))

        val actual = os.read(src)

        val expected =
          """
          |package com.example.test
          |
          |// DO NOT EDIT: Generated by Indigo.
          |
          |final case class StatsMap(val level: Int, val bonus: Int)
          |object StatsMap:
          |  val data: Map[String, StatsMap] =
          |    Map(
          |      "intelligence" -> StatsMap(2, 4),
          |      "strength" -> StatsMap(10, 0),
          |      "fortitude" -> StatsMap(4, 1)
          |    )
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

}
