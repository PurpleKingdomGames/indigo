package indigo.json

import indigo.json.core.GlyphWrapper
import indigo.shared.IndigoLogger
import indigo.shared.JsonSupportFunctions
import indigo.shared.datatypes.FontChar
import indigo.shared.formats.Aseprite
import indigo.shared.formats.TiledMap
import io.circe.parser.*

object Json extends JsonSupportFunctions {

  import core.CirceJsonEncodersAndDecoders._

  def asepriteFromJson(json: String): Option[Aseprite] =
    decode[Aseprite](json) match {
      case Right(s) =>
        Some(s)

      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into Aseprite: " + e.getMessage)
        None
    }

  def tiledMapFromJson(json: String): Option[TiledMap] =
    decode[TiledMap](json) match {
      case Right(s) =>
        Some(s)

      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into TiledMap: " + e.getMessage)
        None
    }

  def readFontToolJson(json: String): Option[List[FontChar]] =
    decode[GlyphWrapper](json) match {
      case Right(s) =>
        Some(s.glyphs.map(_.toFontChar))

      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into a list of glyphs: " + e.getMessage)
        None
    }

}
