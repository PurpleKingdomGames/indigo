package pirate.scenes.level.model

import indigo.shared.formats.TiledGridCell
import pirate.core.TileType
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

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

    val actual: List[indigo.TiledGridCell[TileType]] =
      Platform.filterPlatformTiles(map)

    val expected: List[indigo.TiledGridCell[TileType]] =
      List(
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
      Platform.convertCellsToBoundingBoxes(cells)

    val expected: List[BoundingBox] =
      List(
        BoundingBox(0, 1, 1, 1),
        BoundingBox(1, 2, 1, 1),
        BoundingBox(2, 2, 1, 1),
        BoundingBox(4, 3, 1, 1)
      )

    assertEquals(actual, expected)
  }

  test("weld bounding boxes together.simple example") {
    val boxes: List[BoundingBox] =
      List(
        BoundingBox(0, 1, 1, 1),
        BoundingBox(1, 2, 1, 1),
        BoundingBox(2, 2, 1, 1),
        BoundingBox(4, 3, 1, 1)
      )

    val actual =
      Platform.weldBoundingBoxes(boxes)

    val expected =
      List(
        BoundingBox(0, 1, 1, 1),
        BoundingBox(1, 2, 2, 1),
        BoundingBox(4, 3, 1, 1)
      ).reverse

    assertEquals(actual, expected)
  }

  test("weld bounding boxes together.full example") {
    val boxes: List[BoundingBox] =
      List(
        BoundingBox(Vertex(0, 3), Vertex(1, 1)),
        BoundingBox(Vertex(1, 3), Vertex(1, 1)),
        BoundingBox(Vertex(17, 5), Vertex(1, 1)),
        BoundingBox(Vertex(18, 5), Vertex(1, 1)),
        BoundingBox(Vertex(19, 5), Vertex(1, 1)),
        BoundingBox(Vertex(4, 9), Vertex(1, 1)),
        BoundingBox(Vertex(5, 9), Vertex(1, 1)),
        BoundingBox(Vertex(6, 9), Vertex(1, 1)),
        BoundingBox(Vertex(7, 9), Vertex(1, 1)),
        BoundingBox(Vertex(8, 9), Vertex(1, 1)),
        BoundingBox(Vertex(9, 9), Vertex(1, 1)),
        BoundingBox(Vertex(10, 9), Vertex(1, 1)),
        BoundingBox(Vertex(11, 9), Vertex(1, 1)),
        BoundingBox(Vertex(12, 9), Vertex(1, 1)),
        BoundingBox(Vertex(13, 9), Vertex(1, 1)),
        BoundingBox(Vertex(14, 9), Vertex(1, 1)),
        BoundingBox(Vertex(18, 9), Vertex(1, 1)),
        BoundingBox(Vertex(19, 9), Vertex(1, 1))
      )

    val actual =
      Platform.weldBoundingBoxes(boxes)

    val expected =
      List(
        BoundingBox(Vertex(18, 9), Vertex(2, 1)),
        BoundingBox(Vertex(4, 9), Vertex(11, 1)),
        BoundingBox(Vertex(17, 5), Vertex(3, 1)),
        BoundingBox(Vertex(0, 3), Vertex(2, 1))
      )

    assertEquals(actual.length, expected.length)
    assertEquals(actual.forall(expected.contains), true)
    assertEquals(expected.forall(actual.contains), true)
  }

}
