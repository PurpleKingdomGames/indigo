package indigo.shared

import indigo.shared.datatypes.FontChar
import indigo.shared.formats.Aseprite
import indigo.shared.formats.TiledMap

trait JsonSupportFunctions {

  def asepriteFromJson(json: String): Option[Aseprite]

  def tiledMapFromJson(json: String): Option[TiledMap]

  def readFontToolJson(json: String): Option[List[FontChar]]
}
