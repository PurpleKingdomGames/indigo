package indigo.shared.formats

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Group

import scala.annotation.tailrec

/*
Full spec is here:
http://doc.mapeditor.org/reference/tmx-map-format/

This is not a full implementation. No doubt I'll be adding and tweaking as I go based on requirements.
 */

final case class TiledMap(
    width: Int,
    height: Int,
    infinite: Boolean,
    layers: List[TiledLayer],
    nextobjectid: Int, // Stores the next available ID for new objects. This number is stored to prevent reuse of the same ID after objects have been removed.
    orientation: String, // orthogonal, isometric, staggered and hexagonal
    renderorder: String, // right-down (the default), right-up, left-down and left-up. In all cases, the map is drawn row-by-row.
    tiledversion: String,
    tilewidth: Int,
    tileheight: Int,
    tilesets: List[TileSet],
    `type`: String, // "map"
    hexsidelength: Option[Int],
    staggeraxis: Option[String], // For staggered and hexagonal maps, determines which axis ("x" or "y") is staggered
    staggerindex: Option[
      String
    ], // For staggered and hexagonal maps, determines whether the "even" or "odd" indexes along the staggered axis are shifted.
    backgroundcolor: Option[String] // #AARRGGBB
) derives CanEqual {

  def toGrid[A](mapper: Int => A): Option[TiledGridMap[A]] = {

    def toGridLayer(tiledLayer: TiledLayer): TiledGridLayer[A] =
      TiledGridLayer(
        rec(tiledLayer.data.map(mapper).zipWithIndex, tiledLayer.width, Nil),
        tiledLayer.width,
        tiledLayer.height
      )

    given CanEqual[List[(A, Int)], List[(A, Int)]] = CanEqual.derived

    @tailrec
    def rec(remaining: List[(A, Int)], columnCount: Int, acc: List[TiledGridCell[A]]): List[TiledGridCell[A]] =
      remaining match {
        case Nil =>
          acc

        case (a, i) :: xs =>
          rec(xs, columnCount, acc ++ List(TiledGridCell(i % columnCount, i / columnCount, a)))
      }

    layers match {
      case Nil =>
        None

      case l :: ls =>
        Option(
          TiledGridMap[A](
            NonEmptyList(toGridLayer(l), ls.map(toGridLayer))
          )
        )

    }
  }

  def toGroup(assetName: AssetName): Option[Group] =
    TiledMap.toGroup(this, assetName)

}

final case class TiledLayer(
    name: String,
    data: List[Int],
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    opacity: Double,
    `type`: String, // tilelayer, objectgroup, or imagelayer
    visible: Boolean
) derives CanEqual

final case class TileSet(
    columns: Option[Int],
    firstgid: Int,
    image: Option[String],
    imageheight: Option[Int],
    imagewidth: Option[Int],
    margin: Option[Int],
    name: Option[String],
    spacing: Option[Int],
    terrains: Option[List[TiledTerrain]],
    tilecount: Option[Int],
    tileheight: Option[Int],
    tiles: Option[Map[String, TiledTerrainCorner]],
    tilewidth: Option[Int],
    source: Option[String]
) derives CanEqual

final case class TiledTerrain(name: String, tile: Int) derives CanEqual
final case class TiledTerrainCorner(terrain: List[Int]) derives CanEqual

object TiledMap {

  private def fromIndex(index: Int, gridWidth: Int): Point =
    Point(
      x = index % gridWidth,
      y = index / gridWidth
    )

  def toGroup(tiledMap: TiledMap, assetName: AssetName): Option[Group] =
    tiledMap.tilesets.headOption.flatMap(_.columns).map { tileSheetColumnCount =>
      val tileSize: Size = Size(tiledMap.tilewidth, tiledMap.tileheight)

      val layers: Batch[Group] =
        Batch.fromList(tiledMap.layers).map { layer =>
          val tilesInUse: Map[Int, Graphic[Material.Bitmap]] =
            layer.data.toSet.foldLeft(Map.empty[Int, Graphic[Material.Bitmap]]) { (tiles, i) =>
              tiles ++ Map(
                i ->
                  Graphic(Rectangle(Point.zero, tileSize), Material.Bitmap(assetName))
                    .withCrop(
                      Rectangle(fromIndex(i - 1, tileSheetColumnCount) * tileSize.toPoint, tileSize)
                    )
              )
            }

          Group(
            Batch.fromList(layer.data).zipWithIndex.flatMap { case (tileIndex, positionIndex) =>
              if (tileIndex == 0) Batch.empty
              else
                tilesInUse
                  .get(tileIndex)
                  .map(g => Batch(g.moveTo(fromIndex(positionIndex, tiledMap.width) * tileSize.toPoint)))
                  .getOrElse(Batch.empty)
            }
          )
        }

      Group(layers)
    }

}

final case class TiledGridMap[A](layers: NonEmptyList[TiledGridLayer[A]]) derives CanEqual {

  lazy val toListPerLayer: NonEmptyList[List[TiledGridCell[A]]] =
    layers.map(_.grid)

  lazy val toList2DPerLayer: NonEmptyList[List[List[TiledGridCell[A]]]] = {
    given CanEqual[List[TiledGridCell[A]], List[TiledGridCell[A]]] = CanEqual.derived

    @tailrec
    def rec(
        remaining: List[TiledGridCell[A]],
        columnCount: Int,
        current: List[TiledGridCell[A]],
        acc: List[List[TiledGridCell[A]]]
    ): List[List[TiledGridCell[A]]] =
      remaining match {
        case Nil =>
          acc.reverse

        case x :: xs if x.column == columnCount - 1 =>
          rec(xs, columnCount, Nil, (current ++ List(x)) :: acc)

        case x :: xs =>
          rec(xs, columnCount, current ++ List(x), acc)
      }

    layers.map { layer =>
      rec(layer.grid, layer.columnCount, Nil, Nil)
    }
  }

}
final case class TiledGridLayer[A](grid: List[TiledGridCell[A]], columnCount: Int, rowCount: Int) derives CanEqual
final case class TiledGridCell[A](column: Int, row: Int, tile: A) derives CanEqual {
  lazy val x: Int = column
  lazy val y: Int = row
}
