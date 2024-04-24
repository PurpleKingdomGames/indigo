package indigoplugin.generators

import indigoplugin.IndigoGenerators

class GeneratorAcceptanceTests extends munit.FunSuite {

  val sourceCSV     = os.pwd / "test-assets" / "data" / "stats.csv"
  val sourceMD      = os.pwd / "test-assets" / "data" / "stats.md"
  val sourceColours = os.pwd / "test-assets" / "data" / "colours.txt"
  val sourceFontTTF = os.pwd / "test-files" / "VCR_OSD_MONO_1.001.ttf"

  val targetDir = os.pwd / "out" / "indigo-plugin-generator-acceptance-test-output"

  private def cleanUp(): Unit = {
    if (os.exists(targetDir)) {
      os.remove.all(target = targetDir)
    }

    os.makeDir.all(targetDir)
  }

  override def beforeAll(): Unit                     = cleanUp()
  override def beforeEach(context: BeforeEach): Unit = cleanUp()

  test("Can generate font bitmap and FontInfo from TTF file") {

    os.makeDir.all(targetDir / Generators.OutputDirName / "images")

    val options: FontOptions =
      FontOptions("my font", 16, CharSet.Alphanumeric)
        .withColor(RGB.Green)
        .withMaxCharactersPerLine(16)
        .noAntiAliasing

    val files =
      IndigoGenerators("com.example.test")
        .embedFont("MyFont", sourceFontTTF, options, targetDir / Generators.OutputDirName / "images")
        .toSourcePaths(targetDir)

    files.toList match {
      case fontInfo :: png :: Nil =>
        assert(os.exists(fontInfo))
        assert(os.exists(png))

      case _ =>
        fail(
          s"Unexpected number of files generated, got ${files.length} files:\n${files.map(_.toString()).mkString("\n")}"
        )
    }
  }

  test("Can generate an enum from a CSV file") {

    val files =
      IndigoGenerators("com.example.test").embedCSV
        .asEnum("StatsEnum", sourceCSV)
        .toSourcePaths(targetDir)

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
          |enum StatsEnum(val level: Int, val bonus: Int, val stackable: Option[Boolean]):
          |  case Intelligence extends StatsEnum(2, 4, Some(true))
          |  case Strength extends StatsEnum(10, 0, None)
          |  case Fortitude extends StatsEnum(4, 1, Some(false))
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

  test("Can generate a map from a markdown table file") {

    val files =
      IndigoGenerators("com.example.test").embedMarkdownTable
        .asMap("StatsMap", sourceMD)
        .toSourcePaths(targetDir)

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
          |final case class StatsMap(level: Int, bonus: Int, code: Option[String])
          |object StatsMap:
          |  val data: Map[String, StatsMap] =
          |    Map(
          |      "intelligence" -> StatsMap(2, 4, Some("i")),
          |      "strength" -> StatsMap(10, 0, None),
          |      "fortitude" -> StatsMap(4, 1, Some("_frt"))
          |    )
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

  test("Can generate a map from a markdown table file (armour, unformatted)") {

    val files =
      IndigoGenerators("com.example.test").embedMarkdownTable
        .asEnum("Armour", os.pwd / "test-assets" / "data" / "armour.md")
        .toSourcePaths(targetDir)

    files.headOption match {
      case None =>
        fail("No file was generated")

      case Some(src) =>
        assert(clue(src) == clue(targetDir / "indigo-compile-codegen-output" / "Armour.scala"))

        val actual = os.read(src)

        val expected =
          """
          |package com.example.test
          |
          |// DO NOT EDIT: Generated by Indigo.
          |
          |enum Armour(val defenseBonus: Int):
          |  case LeatherArmor extends Armour(1)
          |  case ChainMail extends Armour(3)
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

  test("Can generate a custom output from a markdown table file") {

    val files =
      IndigoGenerators("com.example.test").embedMarkdownTable
        .asCustom("StatsMap", sourceMD) { data =>
          s"""/*
          |${data.map(_.map(cell => s"${cell.asString}: ${cell.giveTypeName}").mkString(",")).mkString("\n")}
          |*/""".stripMargin.trim
        }
        .toSourcePaths(targetDir)

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
          |/*
          |"name": String,"level": String,"bonus": String,"code": String
          |"intelligence": String,2: Int,4: Int,Some("i"): Option[String]
          |"strength": String,10: Int,0: Int,None: Option[Any]
          |"fortitude": String,4: Int,1: Int,Some("_frt"): Option[String]
          |*/
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

  test("Can generate Aseprite Data") {

    val jsonFile = os.pwd / "test-assets" / "captain" / "Captain Clown Nose Data.json"

    val files =
      IndigoGenerators("com.example.test")
        .embedAseprite("MyAnimation", jsonFile)
        .toSourcePaths(targetDir)

    files.headOption match {
      case None =>
        fail("No file was generated")

      case Some(src) =>
        assert(clue(src) == clue(targetDir / "indigo-compile-codegen-output" / "MyAnimation.scala"))

        val actual = os.read(src)

        val expected =
          """
          |package com.example.test
          |
          |import indigo.shared.formats.*
          |
          |// DO NOT EDIT: Generated by Indigo.
          |object MyAnimation:
          |
          |  val aseprite: Aseprite =
          |    Aseprite(List(AsepriteFrame("Captain Clown Nose 0.aseprite"
          |""".stripMargin

        assert(clue(actual.trim).startsWith(clue(expected.trim)))
    }

  }

  test("Can embed a txt tile") {

    val files =
      IndigoGenerators("com.example.test")
        .embedText("ColoursText", sourceColours)
        .toSourcePaths(targetDir)

    files.headOption match {
      case None =>
        fail("No file was generated")

      case Some(src) =>
        assert(clue(src) == clue(targetDir / "indigo-compile-codegen-output" / "ColoursText.scala"))

        val actual = os.read(src)

        val expected =
          s"""
          |package com.example.test
          |
          |// DO NOT EDIT: Generated by Indigo.
          |object ColoursText:
          |
          |  val text: String =
          |    ${Generators.TripleQuotes}red
          |green
          |blue
          |${Generators.TripleQuotes}
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

  test("Can embed a txt tile and transform it") {

    val files =
      IndigoGenerators("com.example.test")
        .embed("ColoursList", sourceColours) { text =>
          "val colours: List[String] = " + text.split("\n").map(t => s"""\"$t\"""").mkString("List(", ", ", ")")
        }
        .toSourcePaths(targetDir)

    files.headOption match {
      case None =>
        fail("No file was generated")

      case Some(src) =>
        assert(clue(src) == clue(targetDir / "indigo-compile-codegen-output" / "ColoursList.scala"))

        val actual = os.read(src)

        val expected =
          """
          |package com.example.test
          |
          |// DO NOT EDIT: Generated by Indigo.
          |
          |val colours: List[String] = List("red", "green", "blue")
          |
          |""".stripMargin

        assertEquals(actual.trim, expected.trim)
    }
  }

}
