package indigoexts.formats

import indigo.shared.datatypes._
import indigo.shared.scenegraph.{Graphic, Group}
import indigo.shared.formats.{TiledMap}

import indigo.shared.EqualTo._
import indigo.shared.assets.AssetName

/*
Full spec is here:
http://doc.mapeditor.org/reference/tmx-map-format/

This is not a full implementation. No doubt I'll be adding and tweaking as I go based on requirements.
 */

object TiledConverter {

  def fromIndex(index: Int, gridWidth: Int): Point =
    Point(
      x = index % gridWidth,
      y = index / gridWidth
    )

  // Lots of the available metadata has simply not been used here...
  def toSceneGraphNodeBranch(tiledMap: TiledMap, depth: Depth, assetName: AssetName, tileSheetColumnCount: Int): Group = {
    val tileSize: Point = Point(tiledMap.tilewidth, tiledMap.tileheight)

    val layers = tiledMap.layers.map { layer =>
      val tilesInUse: Map[Int, Graphic] =
        layer.data.toSet.foldLeft(Map.empty[Int, Graphic]) { (tiles, i) =>
          tiles ++ Map(
            i ->
              Graphic(Rectangle(Point.zero, tileSize), depth.zIndex, assetName)
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
