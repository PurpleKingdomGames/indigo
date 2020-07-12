package indigo.shared.formats

import utest._
import indigo.shared.formats.TiledMap
import indigo.shared.formats.TileSet
import indigo.shared.formats.TiledLayer

object TiledMapTests extends TestSuite {

  val tests: Tests =
    Tests {

      "should be able to convert to a 2D list" - {

        "identity (Int)" - {
          val actual =
            TiledSamples.tiledMap.toList2D[Int](identity[Int])

          //TODO: This isn't right, this is just one Layer's worth, there can be may layers.

          val expected: List[List[Int]] =
            List(
              List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(7, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 19),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 36, 36),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(20, 0, 0, 0, 69, 70, 70, 62, 2, 2, 2, 2, 61, 71, 0, 0, 0, 0, 69),
              List(20, 0, 0, 0, 0, 0, 0, 35, 36, 36, 8, 7, 37, 0, 0, 0, 0, 0, 0),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 20, 0, 0, 0, 0, 0, 0, 0)
            )

          actual ==> expected

        }

        "with mapping" - {
          val asInt: List[List[Int]] =
            List(
              List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(7, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 19),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 36, 36),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
              List(20, 0, 0, 0, 69, 70, 70, 62, 2, 2, 2, 2, 61, 71, 0, 0, 0, 0, 69),
              List(20, 0, 0, 0, 0, 0, 0, 35, 36, 36, 8, 7, 37, 0, 0, 0, 0, 0, 0),
              List(20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 20, 0, 0, 0, 0, 0, 0, 0)
            )

          val actual: List[List[TileTypes]] =
            TiledSamples.tiledMap.toList2D[TileTypes](TiledSamples.mapping)

          asInt.zip(actual).forall { row =>
            row._1.zip(row._2).forall {
              case (0, TileTypes.Empty)           => true
              case (i, TileTypes.Solid) if i != 0 => true
              case (i, TileTypes.Empty) if i != 0 => false
              case (i, TileTypes.Solid) if i == 0 => false
            }
          } ==> true
        }
      }

      "should be able to convert to a list" - {

        "identity (Int)" - {
          val actual =
            TiledSamples.tiledMap.toList2D[Int](identity[Int])

          val expected: List[Int] =
            List(
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 37, 0, 0, 0,
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 19, 20, 0, 0, 0, 0, 0, 0, 0,
              0, 0, 0, 0, 0, 0, 0, 0, 35, 36, 36, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 69, 70, 70, 62, 2, 2, 2, 2, 61, 71, 0, 0, 0, 0, 69, 20, 0, 0, 0, 0, 0, 0, 35,
              36, 36, 8, 7, 37, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 20, 0, 0, 0, 0, 0, 0, 0
            )

          actual ==> expected

        }

        "with mapping" - {
          val asInt: List[Int] =
            List(
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 37, 0, 0, 0,
              0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 19, 20, 0, 0, 0, 0, 0, 0, 0,
              0, 0, 0, 0, 0, 0, 0, 0, 35, 36, 36, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 69, 70, 70, 62, 2, 2, 2, 2, 61, 71, 0, 0, 0, 0, 69, 20, 0, 0, 0, 0, 0, 0, 35,
              36, 36, 8, 7, 37, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 20, 0, 0, 0, 0, 0, 0, 0
            )

          val actual =
            TiledSamples.tiledMap.toList[TileTypes](TiledSamples.mapping)

          asInt.zip(actual).forall {
            case (0, TileTypes.Empty)           => true
            case (i, TileTypes.Solid) if i != 0 => true
            case (i, TileTypes.Empty) if i != 0 => false
            case (i, TileTypes.Solid) if i == 0 => false
          } ==> true

        }
      }

      "should be able to convert to a Group of graphics" - {

        1 ==> 2
      }

      "should be able to convert to a Group of clones" - {
        1 ==> 2
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

  val tiledMap: TiledMap =
    TiledMap(
      19,
      11,
      false,
      List(
        TiledLayer(
          "Tile Layer 1",
          List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 37, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 19, 20, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 35, 36, 36, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 69, 70, 70, 62, 2, 2, 2, 2, 61, 71, 0, 0, 0, 0, 69, 20, 0, 0, 0, 0, 0, 0, 35, 36,
            36, 8, 7, 37, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 20, 0, 0, 0, 0, 0, 0, 0),
          0,
          0,
          19,
          11,
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
