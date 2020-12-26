package indigo.shared

import indigo.shared.formats.{Aseprite, TiledMap}
import indigo.shared.datatypes.FontChar

trait JsonSupportFunctions {

  def asepriteFromJson(json: String): Option[Aseprite]

  def tiledMapFromJson(json: String): Option[TiledMap]

  def readFontToolJson(json: String): Option[List[FontChar]]
}
