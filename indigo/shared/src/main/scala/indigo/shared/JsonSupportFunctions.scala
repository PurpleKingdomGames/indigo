package indigo.shared

import indigo.shared.config.GameConfig
import indigo.shared.formats.{Aseprite, TiledMap}
import indigo.shared.assets.AssetList

trait JsonSupportFunctions {

  def assetListFromJson(json: String): Either[String, AssetList]

  def gameConfigFromJson(json: String): Either[String, GameConfig]

  def gameDefinitionFromJson(json: String): Either[String, GameDefinition]

  def asepriteFromJson(json: String): Option[Aseprite]

  def tiledMapFromJson(json: String): Option[TiledMap]

}
