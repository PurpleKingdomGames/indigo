package indigo

import indigo.shared.config.GameConfig
import indigo.shared.{AssetList, GameDefinition, JsonSupportFunctions}
import indigo.shared.formats.{Aseprite, TiledMap}

package object json extends JsonSupportFunctions {

  def assetListFromJson(json: String): Either[String, AssetList] =
    Circe9.assetListFromJson(json)

  def gameConfigFromJson(json: String): Either[String, GameConfig] =
    Circe9.gameConfigFromJson(json)

  def gameDefinitionFromJson(json: String): Either[String, GameDefinition] =
    Circe9.gameDefinitionFromJson(json)

  def asepriteFromJson(json: String): Option[Aseprite] =
    Circe9.asepriteFromJson(json)

  def tiledMapFromJson(json: String): Option[TiledMap] =
    Circe9.tiledMapFromJson(json)

}
