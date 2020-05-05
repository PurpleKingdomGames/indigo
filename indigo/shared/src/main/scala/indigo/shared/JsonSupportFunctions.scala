package indigo.shared

import indigo.shared.formats.{Aseprite, TiledMap}

trait JsonSupportFunctions {

  def asepriteFromJson(json: String): Option[Aseprite]

  def tiledMapFromJson(json: String): Option[TiledMap]

}
