package indigo.shared.formats

import utest._
import indigo.shared.formats.TiledMap
import indigo.shared.formats.TileSet
import indigo.shared.formats.TiledLayer
import indigo.shared.assets.AssetName
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.Renderable
import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.Point

object TiledMapTests extends TestSuite {

  val tests: Tests =
    Tests {

      "should be able to convert to a TiledGridMap" - {

        "identity (Int)" - {
          val actual =
            TiledSamples.tiledMap
              .toGrid[Int](identity[Int])
              .toListPerLayer
              .head
              .map(_.tile)

          val expected: List[Int] =
            TiledSamples.gridMapInt.layers.head.grid.map(_.tile)

          actual ==> expected

        }

        "with mapping" - {
          val actual =
            TiledSamples.tiledMap
              .toGrid[TileTypes](TiledSamples.mapping)
              .toListPerLayer
              .head
              .map(_.tile)

          TiledSamples.gridMapInt.layers.head.grid
            .map(_.tile)
            .zip(actual)
            .forall {
              case (0, TileTypes.Empty)           => true
              case (i, TileTypes.Solid) if i != 0 => true
              case (i, TileTypes.Empty) if i != 0 => false
              case (i, TileTypes.Solid) if i == 0 => false
            } ==> true

        }

        "to 2D grid (int)" - {
          val actual: List[List[Int]] =
            TiledSamples.tiledMap
              .toGrid[Int](identity[Int])
              .toList2DPerLayer
              .head
              .map(_.map(_.tile))

          val expected: List[List[Int]] =
            TiledSamples.gridMapInt2D

          actual ==> expected
        }

      }

      "should be able to convert to a Group of graphics" - {
        val actual: Group =
          TiledSamples.tiledMap.toGroup(AssetName("test")).get

        actual.children.head match {
          case g: Group =>
            // Only 3 tiles have contents.
            val graphics: List[Graphic] =
              g.children.collect { case graphic: Graphic => graphic }

            graphics.length ==> 3
            graphics(0).position ==> Point(32, 64)
            graphics(1).position ==> Point(64, 64)
            graphics(2).position ==> Point(64, 96)

          case _: Renderable =>
            throw new Exception("failed")
        }
      }

    }

}

sealed trait TileTypes
object TileTypes {
  case object Empty extends TileTypes
  case object Solid extends TileTypes
}

object TiledSamples {

  val mapping: Int => TileTypes = {
    case 0 => TileTypes.Empty
    case _ => TileTypes.Solid
  }

  val gridMapInt: TiledGridMap[Int] =
    TiledGridMap(
      List(
        TiledGridLayer(
          List(
            TiledGridCell(0, 0, 0),
            TiledGridCell(1, 0, 0),
            TiledGridCell(2, 0, 0),
            TiledGridCell(3, 0, 0),
            TiledGridCell(0, 1, 0),
            TiledGridCell(1, 1, 0),
            TiledGridCell(2, 1, 0),
            TiledGridCell(3, 1, 0),
            TiledGridCell(0, 2, 0),
            TiledGridCell(1, 2, 2),
            TiledGridCell(2, 2, 1),
            TiledGridCell(3, 2, 0),
            TiledGridCell(0, 3, 0),
            TiledGridCell(1, 3, 0),
            TiledGridCell(2, 3, 1),
            TiledGridCell(3, 3, 0)
          ),
          4
        )
      )
    )

  val gridMapInt2D: List[List[Int]] =
    List(
      List(
        TiledGridCell(0, 0, 0),
        TiledGridCell(1, 0, 0),
        TiledGridCell(2, 0, 0),
        TiledGridCell(3, 0, 0)
      ).map(_.tile),
      List(
        TiledGridCell(0, 1, 0),
        TiledGridCell(1, 1, 0),
        TiledGridCell(2, 1, 0),
        TiledGridCell(3, 1, 0)
      ).map(_.tile),
      List(
        TiledGridCell(0, 2, 0),
        TiledGridCell(1, 2, 2),
        TiledGridCell(2, 2, 1),
        TiledGridCell(3, 2, 0)
      ).map(_.tile),
      List(
        TiledGridCell(0, 3, 0),
        TiledGridCell(1, 3, 0),
        TiledGridCell(2, 3, 1),
        TiledGridCell(3, 3, 0)
      ).map(_.tile)
    )

  val tiledMap: TiledMap =
    TiledMap(
      4,
      4,
      false,
      List(
        TiledLayer(
          "Tile Layer 1",
          gridMapInt.layers.head.grid.map(_.tile),
          0,
          0,
          4,
          4,
          1,
          "tilelayer",
          true
        )
      ),
      1,
      "orthogonal",
      "right-down",
      "1.3.2",
      32,
      32,
      List(TileSet(Some(17), 1, Some("terrain.png"), Some(160), Some(544), Some(0), Some("Palm Island"), Some(0), None, Some(85), Some(32), None, Some(32), None)),
      "map",
      None,
      None,
      None,
      None
    )

}
