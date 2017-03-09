package com.purplekingdomgames.indigo.gameengine.scenegraph

/*
Full spec is here:
http://doc.mapeditor.org/reference/tmx-map-format/

This is not a full implementation. No doubt I'll be adding and tweaking as I go based on requirements.
 */

object TiledHelper {

}

case class TiledMap(width: Int,
                    height: Int,
                    layers: List[TiledLayer],
                    nextobjectid: Int, // Stores the next available ID for new objects. This number is stored to prevent reuse of the same ID after objects have been removed.
                    orientation: String, // orthogonal, isometric, staggered and hexagonal
                    renderorder: String, // right-down (the default), right-up, left-down and left-up. In all cases, the map is drawn row-by-row.
                    tilewidth: Int,
                    tileheight: Int,
                    tilesets: List[TileSet],
                    hexsidelength: Option[Int],
                    staggeraxis: Option[String], // For staggered and hexagonal maps, determines which axis ("x" or "y") is staggered
                    staggerindex: Option[String], // For staggered and hexagonal maps, determines whether the "even" or "odd" indexes along the staggered axis are shifted.
                    backgroundcolor: Option[String], // #AARRGGBB
                    version: Int)

case class TiledLayer(name: String,
                      data: List[Int],
                      x: Int,
                      y: Int,
                      width: Int,
                      height: Int,
                      opacity: Double,
                      `type`: String, // tilelayer, objectgroup, or imagelayer
                      visible: Boolean)

case class TileSet(columns: Int,
                   firstgid: Int,
                   image: String,
                   imageheight: Int,
                   imagewidth: Int,
                   margin: Int,
                   name: String,
                   spacing: Int,
                   terrains: List[TiledTerrain],
                   tilecount: Int,
                   tileheight: Int,
                   tiles: Map[String, TiledTerrainCorner],
                   tilewidth: Int)

case class TiledTerrain(name: String, tile: Int)
case class TiledTerrainCorner(terrain: List[Int])
