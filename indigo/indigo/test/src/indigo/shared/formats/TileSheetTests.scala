package indigo.shared.formats

import indigo.*

class TileSheetTests extends munit.FunSuite:
  private val tileWidth  = 16
  private val tileHeight = 16
  private val margin     = 1
  private val rows       = 32
  private val columns    = 32
  private val width      = (tileWidth + margin) * rows - margin
  private val height     = (tileHeight + margin) * columns - margin
  private val testFixture = TileSheet(
    AssetName("ignored"),
    Size(width, height),
    Size(tileWidth, tileHeight),
    margin
  )

  test("negative index") {
    assert(testFixture.rectangleForIndex(-1).isEmpty)
  }

  test("index too big") {
    assert(testFixture.rectangleForIndex(rows * columns).isEmpty)
  }

  test("index 0") {
    assert(
      testFixture
        .rectangleForIndex(0)
        .contains(Rectangle(0, 0, tileWidth, tileHeight))
    )
  }

  test("index 1") {
    assert(
      testFixture
        .rectangleForIndex(1)
        .contains(Rectangle(tileWidth + margin, 0, tileWidth, tileHeight))
    )
  }

  test(s"index $columns (first column second row)") {
    assert(
      testFixture
        .rectangleForIndex(columns)
        .contains(Rectangle(0, tileHeight + margin, tileWidth, tileHeight))
    )
  }

  test("coords -1, 0") {
    assert(testFixture.rectangleForCoords(-1, 0).isEmpty)
  }

  test("coords 0, -1") {
    assert(testFixture.rectangleForCoords(0, -1).isEmpty)
  }

  test("coords 0, 0") {
    assert(
      testFixture
        .rectangleForCoords(0, 0)
        .contains(Rectangle(0, 0, tileWidth, tileHeight))
    )
  }

  test("coords 1, 0") {
    assert(
      testFixture
        .rectangleForCoords(1, 0)
        .contains(Rectangle(tileWidth + margin, 0, tileWidth, tileHeight))
    )
  }

  test("coords 0, 1") {
    assert(
      testFixture
        .rectangleForCoords(0, 1)
        .contains(Rectangle(0, tileHeight + margin, tileWidth, tileHeight))
    )
  }
