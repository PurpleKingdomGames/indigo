package pirate.scenes.level.model

import indigo.shared.formats.TiledGridCell
import pirate.core.TileType
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex
import indigo.shared.collections.Batch
import indigo.syntax.*

class PlatformTests extends munit.FunSuite {

  test("filter down to only tiles that could be platforms") {

    /*
        ...#.
        #....
        ###..
        ..#.#
        .....
     */
    val map: List[TiledGridCell[TileType]] =
      List[TiledGridCell[TileType]](
        TiledGridCell(0, 0, TileType.Empty),
        TiledGridCell(1, 0, TileType.Empty),
        TiledGridCell(2, 0, TileType.Empty),
        TiledGridCell(3, 0, TileType.Solid),
        TiledGridCell(4, 0, TileType.Empty)
      ) ++
        List[TiledGridCell[TileType]](
          TiledGridCell(0, 1, TileType.Solid),
          TiledGridCell(1, 1, TileType.Empty),
          TiledGridCell(2, 1, TileType.Empty),
          TiledGridCell(3, 1, TileType.Empty),
          TiledGridCell(4, 1, TileType.Empty)
        ) ++
        List[TiledGridCell[TileType]](
          TiledGridCell(0, 2, TileType.Solid),
          TiledGridCell(1, 2, TileType.Solid),
          TiledGridCell(2, 2, TileType.Solid),
          TiledGridCell(3, 2, TileType.Empty),
          TiledGridCell(4, 2, TileType.Empty)
        ) ++
        List[TiledGridCell[TileType]](
          TiledGridCell(0, 3, TileType.Empty),
          TiledGridCell(1, 3, TileType.Empty),
          TiledGridCell(2, 3, TileType.Solid),
          TiledGridCell(3, 3, TileType.Empty),
          TiledGridCell(4, 3, TileType.Solid)
        ) ++
        List[TiledGridCell[TileType]](
          TiledGridCell(0, 4, TileType.Empty),
          TiledGridCell(1, 4, TileType.Empty),
          TiledGridCell(2, 4, TileType.Empty),
          TiledGridCell(3, 4, TileType.Empty),
          TiledGridCell(4, 4, TileType.Empty)
        )

    val actual: Batch[indigo.TiledGridCell[TileType]] =
      Platform.filterPlatformTiles(map.toBatch)

    val expected: Batch[indigo.TiledGridCell[TileType]] =
      Batch(
        TiledGridCell(0, 1, TileType.Solid),
        TiledGridCell(1, 2, TileType.Solid),
        TiledGridCell(2, 2, TileType.Solid),
        TiledGridCell(4, 3, TileType.Solid)
      )

    assertEquals(actual, expected)
  }

  test("convert cells to bounding boxes") {
    /* original map
        ...#.
        #....
        ###..
        ..#.#
        .....
     */

    /* Filtered down to
        .....
        #....
        .##..
        ....#
        .....
     */

    val cells: List[TiledGridCell[TileType]] =
      List(
        TiledGridCell(0, 1, TileType.Solid),
        TiledGridCell(1, 2, TileType.Solid),
        TiledGridCell(2, 2, TileType.Solid),
        TiledGridCell(4, 3, TileType.Solid)
      )

    val actual =
      Platform.convertCellsToBoundingBoxes(cells.toBatch)

    val expected: Batch[BoundingBox] =
      Batch(
        BoundingBox(0, 1, 1, 1),
        BoundingBox(1, 2, 1, 1),
        BoundingBox(2, 2, 1, 1),
        BoundingBox(4, 3, 1, 1)
      )

    assertEquals(actual, expected)
  }

}
