package indigo.json

import indigo.shared.JsonSupportFunctions
import indigo.shared.formats.{Aseprite, TiledMap}
import indigo.shared.IndigoLogger

import upickle.default._
import upickle.default.{ReadWriter => RW, macroRW}
import indigo.shared.formats.AsepriteFrame
import indigo.shared.formats.AsepriteRectangle
import indigo.shared.formats.AsepriteSize
import indigo.shared.formats.AsepriteMeta
import indigo.shared.formats.AsepriteFrameTag
import scala.util.Try
import indigo.shared.formats.TiledLayer
import indigo.shared.formats.TileSet
import indigo.shared.formats.TiledTerrain
import indigo.shared.formats.TiledTerrainCorner
import indigo.shared.datatypes.FontChar

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Equals",
    "org.wartremover.warts.Throw",
    "org.wartremover.warts.Null",
    "org.wartremover.warts.Equals",
    "org.wartremover.warts.ToString",
    "org.wartremover.warts.Var"
  )
)
object Json extends JsonSupportFunctions {

  implicit val asepriteSizeRW: RW[AsepriteSize]           = macroRW
  implicit val asepriteRectangleRW: RW[AsepriteRectangle] = macroRW
  implicit val asepriteFrameRW: RW[AsepriteFrame]         = macroRW
  implicit val asepriteMetaRW: RW[AsepriteMeta]           = macroRW
  implicit val asepriteFrameTagRW: RW[AsepriteFrameTag]   = macroRW
  implicit val asepriteRW: RW[Aseprite]                   = macroRW

  def asepriteFromJson(json: String): Option[Aseprite] =
    Try(read[Aseprite](json)).toEither match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into Aseprite: " + e.getMessage)
        None
    }

  implicit val tiledLayerRW: RW[TiledLayer]                 = macroRW
  implicit val tiledTerrainCornerRW: RW[TiledTerrainCorner] = macroRW
  implicit val tiledTerrainRW: RW[TiledTerrain]             = macroRW
  implicit val tileSetRW: RW[TileSet]                       = macroRW
  implicit val tiledMapRW: RW[TiledMap]                     = macroRW

  def tiledMapFromJson(json: String): Option[TiledMap] =
    Try(read[TiledMap](json)).toEither match {
      case Right(s) => Some(s)
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into TiledMap: " + e.getMessage)
        None
    }

  implicit val glyphRW: RW[Glyph]               = macroRW
  implicit val glyphWrapperRW: RW[GlyphWrapper] = macroRW

  def readFontToolJson(json: String): Option[List[FontChar]] =
    Try(read[GlyphWrapper](json)).toEither match {
      case Right(s) => Some(s.glyphs.map(_.toFontChar))
      case Left(e) =>
        IndigoLogger.info("Failed to deserialise json into a list of glyphs: " + e.getMessage)
        None
    }

  final case class GlyphWrapper(glyphs: List[Glyph])
  final case class Glyph(char: String, x: Int, y: Int, w: Int, h: Int) {
    def toFontChar: FontChar =
      FontChar(char, x, y, w, h)
  }
}
