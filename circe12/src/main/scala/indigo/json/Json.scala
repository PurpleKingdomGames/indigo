package indigo.json

import indigo.shared.config.GameConfig
import indigo.shared.{GameDefinition, AssetList, JsonSupportFunctions}
import indigo.shared.formats.{Aseprite, TiledMap}
import indigo.shared.IndigoLogger

import io.circe.generic.auto._
import io.circe.parser._

object Json extends JsonSupportFunctions {

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  def assetListFromJson(json: String): Either[String, AssetList] =
    decode[AssetList](json) match {
      case Right(al) =>
        Right[String, AssetList](al)

      case Left(e) =>
        Left[String, AssetList]("Failed to deserialise json into AssetList: " + e.getMessage)
    }

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  def gameConfigFromJson(json: String): Either[String, GameConfig] =
    decode[GameConfig](json) match {
      case Right(c) =>
        Right[String, GameConfig](c)

      case Left(e) =>
        Left[String, GameConfig]("Failed to deserialise json into GameConfig: " + e.getMessage)
    }

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference", "org.wartremover.warts.Nothing"))
  def gameDefinitionFromJson(json: String): Either[String, GameDefinition] =
    decode[GameDefinition](json) match {
      case Right(gd) =>
        Right[String, GameDefinition](gd)

      case Left(e) =>
        Left[String, GameDefinition]("Failed to deserialise json into GameDefinition: " + e.getMessage)
    }

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference", "org.wartremover.warts.Nothing"))
  def asepriteFromJson(json: String): Option[Aseprite] =
    decode[Aseprite](json) match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into Aseprite: " + e.getMessage)
        None
    }

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference", "org.wartremover.warts.Nothing"))
  def tiledMapFromJson(json: String): Option[TiledMap] =
    decode[TiledMap](json) match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into TiledMap: " + e.getMessage)
        None
    }

}
