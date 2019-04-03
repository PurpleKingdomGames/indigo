package indigoexts.formats

import indigo.gameengine.scenegraph.datatypes._
import indigo.gameengine.scenegraph.{Graphic, Group}
import indigo.runtime.IndigoLogger
import io.circe.generic.auto._
import io.circe.parser._

import indigo.shared.EqualTo._

/*
Full spec is here:
http://doc.mapeditor.org/reference/tmx-map-format/

This is not a full implementation. No doubt I'll be adding and tweaking as I go based on requirements.
 */

object TiledHelper {

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference", "org.wartremover.warts.Nothing"))
  def fromJson(json: String): Option[TiledMap] =
    decode[TiledMap](json) match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into TiledMap: " + e.getMessage)
        None
    }

  def fromIndex(index: Int, gridWidth: Int): Point =
    Point(
      x = index % gridWidth,
      y = index / gridWidth
    )

  // Lots of the available metadata has simply not been used here...
  def toSceneGraphNodeBranch(tiledMap: TiledMap, depth: Depth, imageAssetRef: String, tileSheetColumnCount: Int): Group = {
    val tileSize: Point = Point(tiledMap.tilewidth, tiledMap.tileheight)

    val layers = tiledMap.layers.map { layer =>
      val tilesInUse: Map[Int, Graphic] =
        layer.data.toSet.foldLeft(Map.empty[Int, Graphic]) { (tiles, i) =>
          tiles ++ Map(
            i ->
              Graphic(Rectangle(Point.zero, tileSize), depth.zIndex, imageAssetRef)
                .withCrop(
                  Rectangle(fromIndex(i - 1, tileSheetColumnCount) * tileSize, tileSize)
                )
          )
        }

      Group(
        layer.data.zipWithIndex.flatMap {
          case (tileIndex, poitionIndex) =>
            if (tileIndex === 0) Nil
            else
              tilesInUse
                .get(tileIndex)
                .map(g => List(g.moveTo(fromIndex(poitionIndex, tiledMap.width) * tileSize)))
                .getOrElse(Nil)
        }
      )
    }

    Group(layers)
  }

}

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
    staggerindex: Option[String], // For staggered and hexagonal maps, determines whether the "even" or "odd" indexes along the staggered axis are shifted.
    backgroundcolor: Option[String], // #AARRGGBB
    version: Int
)

final case class TiledLayer(name: String,
                            data: List[Int],
                            x: Int,
                            y: Int,
                            width: Int,
                            height: Int,
                            opacity: Double,
                            `type`: String, // tilelayer, objectgroup, or imagelayer
                            visible: Boolean)

final case class TileSet(columns: Option[Int],
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
                         source: Option[String])

final case class TiledTerrain(name: String, tile: Int)
final case class TiledTerrainCorner(terrain: List[Int])
