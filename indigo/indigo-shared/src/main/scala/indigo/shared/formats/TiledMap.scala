package indigo.shared.formats

import indigo.shared.datatypes.Point
import indigo.shared.scenegraph.Group
import indigo.shared.assets.AssetName
import indigo.shared.scenegraph.Graphic
import indigo.shared.datatypes.Material
import indigo.shared.datatypes.Rectangle

import scala.annotation.tailrec
import indigo.shared.collections.NonEmptyList

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
    nextobjectid: Int,   // Stores the next available ID for new objects. This number is stored to prevent reuse of the same ID after objects have been removed.
    orientation: String, // orthogonal, isometric, staggered and hexagonal
    renderorder: String, // right-down (the default), right-up, left-down and left-up. In all cases, the map is drawn row-by-row.
    tiledversion: String,
    tilewidth: Int,
    tileheight: Int,
    tilesets: List[TileSet],
    `type`: String, // "map"
    hexsidelength: Option[Int],
    staggeraxis: Option[String],    // For staggered and hexagonal maps, determines which axis ("x" or "y") is staggered
    staggerindex: Option[String],   // For staggered and hexagonal maps, determines whether the "even" or "odd" indexes along the staggered axis are shifted.
    backgroundcolor: Option[String] // #AARRGGBB
) {

  def toGrid[A](mapper: Int => A): Option[TiledGridMap[A]] = {

    def toGridLayer(tiledLayer: TiledLayer): TiledGridLayer[A] =
      TiledGridLayer(
        rec(tiledLayer.data.map(mapper).zipWithIndex, tiledLayer.width, Nil),
        tiledLayer.width,
        tiledLayer.height
      )

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
)

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
)

final case class TiledTerrain(name: String, tile: Int)
final case class TiledTerrainCorner(terrain: List[Int])

object TiledMap {

  private def fromIndex(index: Int, gridWidth: Int): Point =
    Point(
      x = index % gridWidth,
      y = index / gridWidth
    )

  def toGroup(tiledMap: TiledMap, assetName: AssetName): Option[Group] =
    tiledMap.tilesets.headOption.flatMap(_.columns).map { tileSheetColumnCount =>
      val tileSize: Point = Point(tiledMap.tilewidth, tiledMap.tileheight)

      val layers = tiledMap.layers.map { layer =>
        val tilesInUse: Map[Int, Graphic] =
          layer.data.toSet.foldLeft(Map.empty[Int, Graphic]) { (tiles, i) =>
            tiles ++ Map(
              i ->
                Graphic(Rectangle(Point.zero, tileSize), 1, Material.Basic(assetName, 1.0))
                  .withCrop(
                    Rectangle(fromIndex(i - 1, tileSheetColumnCount) * tileSize, tileSize)
                  )
            )
          }

        Group(
          layer.data.zipWithIndex.flatMap {
            case (tileIndex, positionIndex) =>
              if (tileIndex == 0) Nil
              else
                tilesInUse
                  .get(tileIndex)
                  .map(g => List(g.moveTo(fromIndex(positionIndex, tiledMap.width) * tileSize)))
                  .getOrElse(Nil)
          }
        )
      }

      Group(layers)
    }

}

final case class TiledGridMap[A](layers: NonEmptyList[TiledGridLayer[A]]) {

  lazy val toListPerLayer: NonEmptyList[List[TiledGridCell[A]]] =
    layers.map(_.grid)

  lazy val toList2DPerLayer: NonEmptyList[List[List[TiledGridCell[A]]]] = {
    @tailrec
    def rec(remaining: List[TiledGridCell[A]], columnCount: Int, current: List[TiledGridCell[A]], acc: List[List[TiledGridCell[A]]]): List[List[TiledGridCell[A]]] =
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
final case class TiledGridLayer[A](grid: List[TiledGridCell[A]], columnCount: Int, rowCount: Int)
final case class TiledGridCell[A](column: Int, row: Int, tile: A) {
  lazy val x: Int = column
  lazy val y: Int = row
}
