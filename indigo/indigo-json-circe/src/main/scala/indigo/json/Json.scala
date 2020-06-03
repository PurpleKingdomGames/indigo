package indigo.json

import indigo.shared.JsonSupportFunctions
import indigo.shared.formats.{Aseprite, TiledMap}
import indigo.shared.IndigoLogger

import io.circe.generic.auto._
import io.circe.parser._
import indigo.shared.datatypes.FontChar

object Json extends JsonSupportFunctions {

  def asepriteFromJson(json: String): Option[Aseprite] =
    decode[Aseprite](json) match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into Aseprite: " + e.getMessage)
        None
    }

  def tiledMapFromJson(json: String): Option[TiledMap] =
    decode[TiledMap](json) match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into TiledMap: " + e.getMessage)
        None
    }

  def readFontToolJson(json: String): Option[List[FontChar]] =
    decode[GlyphWrapper](json) match {
      case Right(s) => Some(s.glyphs.map(_.toFontChar))
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into a list of glyphs: " + e.getMessage)
        None
    }

  private final case class GlyphWrapper(glyphs: List[Glyph])
  private final case class Glyph(char: String, x: Int, y: Int, w: Int, h: Int) {
    def toFontChar: FontChar =
      FontChar(char, x, y, w, h)
  }
}
