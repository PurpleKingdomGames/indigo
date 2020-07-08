package indigo.shared.formats

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
)

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
