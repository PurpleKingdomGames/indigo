package indigo.json

import indigo.shared.JsonSupportFunctions
import indigo.shared.formats.{Aseprite, TiledMap}
import indigo.shared.IndigoLogger

import io.circe.generic.auto._
import io.circe.parser._

object Json extends JsonSupportFunctions {

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
