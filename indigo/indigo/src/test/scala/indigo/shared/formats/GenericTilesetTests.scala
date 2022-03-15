package indigo.shared.formats

import indigo._

class GenericTilesetTests extends munit.FunSuite {
  private val tileWidth  = 16
  private val tileHeight = 16
  private val margin     = 1
  private val rows       = 32
  private val columns    = 32
  private val width      = (tileWidth + margin) * rows - margin
  private val height     = (tileHeight + margin) * columns - margin
  private val testFixture = GenericTileset(
    AssetName("ignored"),
    width,
    height,
    tileWidth,
    tileHeight,
    margin
  )

  test("negative index") {
    assert(testFixture.lookupRectangle(-1).isEmpty)
  }

  test("index too big") {
    assert(testFixture.lookupRectangle(rows * columns).isEmpty)
  }

  test("0, 0") {
    assert(
      testFixture
        .lookupRectangle(0)
        .contains(Rectangle(0, 0, tileWidth, tileHeight))
    )
  }

  test("1, 0") {
    assert(
      testFixture
        .lookupRectangle(1)
        .contains(Rectangle(tileWidth + margin, 0, tileWidth, tileHeight))
    )
  }

  test("0, 1") {
    assert(
      testFixture
        .lookupRectangle(columns)
        .contains(Rectangle(0, tileHeight + margin, tileWidth, tileHeight))
    )
  }
}
