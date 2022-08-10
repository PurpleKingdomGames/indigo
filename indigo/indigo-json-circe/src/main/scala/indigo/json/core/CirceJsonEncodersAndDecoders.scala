package indigo.json.core

import indigo.shared.formats.Aseprite
import indigo.shared.formats.AsepriteFrame
import indigo.shared.formats.AsepriteFrameTag
import indigo.shared.formats.AsepriteMeta
import indigo.shared.formats.AsepriteRectangle
import indigo.shared.formats.AsepriteSize
import indigo.shared.formats.TileSet
import indigo.shared.formats.TiledLayer
import indigo.shared.formats.TiledMap
import indigo.shared.formats.TiledTerrain
import indigo.shared.formats.TiledTerrainCorner
import io.circe.Decoder
import io.circe.HCursor

object CirceJsonEncodersAndDecoders {

  implicit val decodeAsepriteFrame: Decoder[AsepriteFrame] =
    new Decoder[AsepriteFrame] {
      final def apply(c: HCursor): Decoder.Result[AsepriteFrame] =
        for {
          filename         <- c.downField("filename").as[String]
          frame            <- c.downField("frame").as[AsepriteRectangle]
          rotated          <- c.downField("rotated").as[Boolean]
          trimmed          <- c.downField("trimmed").as[Boolean]
          spriteSourceSize <- c.downField("spriteSourceSize").as[AsepriteRectangle]
          sourceSize       <- c.downField("sourceSize").as[AsepriteSize]
          duration         <- c.downField("duration").as[Int]
        } yield AsepriteFrame(filename, frame, rotated, trimmed, spriteSourceSize, sourceSize, duration)
    }

  implicit val decodeAsepriteRectangle: Decoder[AsepriteRectangle] =
    new Decoder[AsepriteRectangle] {
      final def apply(c: HCursor): Decoder.Result[AsepriteRectangle] =
        for {
          x <- c.downField("x").as[Int]
          y <- c.downField("y").as[Int]
          w <- c.downField("w").as[Int]
          h <- c.downField("h").as[Int]
        } yield AsepriteRectangle(x, y, w, h)
    }

  implicit val decodeAsepriteMeta: Decoder[AsepriteMeta] =
    new Decoder[AsepriteMeta] {
      final def apply(c: HCursor): Decoder.Result[AsepriteMeta] =
        for {
          app       <- c.downField("app").as[String]
          version   <- c.downField("version").as[String]
          format    <- c.downField("format").as[String]
          size      <- c.downField("size").as[AsepriteSize]
          scale     <- c.downField("scale").as[String]
          frameTags <- c.downField("frameTags").as[List[AsepriteFrameTag]]
        } yield AsepriteMeta(app, version, format, size, scale, frameTags)
    }

  implicit val decodeAsepriteSize: Decoder[AsepriteSize] =
    new Decoder[AsepriteSize] {
      final def apply(c: HCursor): Decoder.Result[AsepriteSize] =
        for {
          w <- c.downField("w").as[Int]
          h <- c.downField("h").as[Int]
        } yield AsepriteSize(w, h)
    }

  implicit val decodeAsepriteFrameTag: Decoder[AsepriteFrameTag] =
    new Decoder[AsepriteFrameTag] {
      final def apply(c: HCursor): Decoder.Result[AsepriteFrameTag] =
        for {
          name      <- c.downField("name").as[String]
          from      <- c.downField("from").as[Int]
          to        <- c.downField("to").as[Int]
          direction <- c.downField("direction").as[String]
        } yield AsepriteFrameTag(name, from, to, direction)
    }

  implicit val decodeAseprite: Decoder[Aseprite] =
    new Decoder[Aseprite] {
      final def apply(c: HCursor): Decoder.Result[Aseprite] =
        for {
          frames <- c.downField("frames").as[List[AsepriteFrame]]
          meta   <- c.downField("meta").as[AsepriteMeta]
        } yield Aseprite(frames, meta)
    }

  implicit val decodeTiledTerrain: Decoder[TiledTerrain] =
    new Decoder[TiledTerrain] {
      final def apply(c: HCursor): Decoder.Result[TiledTerrain] =
        for {
          name <- c.downField("name").as[String]
          tile <- c.downField("tile").as[Int]
        } yield TiledTerrain(name, tile)
    }

  implicit val decodeTiledTerrainCorner: Decoder[TiledTerrainCorner] =
    new Decoder[TiledTerrainCorner] {
      final def apply(c: HCursor): Decoder.Result[TiledTerrainCorner] =
        for {
          terrain <- c.downField("terrain").as[List[Int]]
        } yield TiledTerrainCorner(terrain)
    }

  implicit val decodeTileSet: Decoder[TileSet] =
    new Decoder[TileSet] {
      final def apply(c: HCursor): Decoder.Result[TileSet] =
        for {
          columns     <- c.downField("columns").as[Option[Int]]
          firstgid    <- c.downField("firstgid").as[Int]
          image       <- c.downField("image").as[Option[String]]
          imageheight <- c.downField("imageheight").as[Option[Int]]
          imagewidth  <- c.downField("imagewidth").as[Option[Int]]
          margin      <- c.downField("margin").as[Option[Int]]
          name        <- c.downField("name").as[Option[String]]
          spacing     <- c.downField("spacing").as[Option[Int]]
          terrains    <- c.downField("terrains").as[Option[List[TiledTerrain]]]
          tilecount   <- c.downField("tilecount").as[Option[Int]]
          tileheight  <- c.downField("tileheight").as[Option[Int]]
          tiles       <- c.downField("tiles").as[Option[Map[String, TiledTerrainCorner]]]
          tilewidth   <- c.downField("tilewidth").as[Option[Int]]
          source      <- c.downField("source").as[Option[String]]
        } yield TileSet(
          columns,
          firstgid,
          image,
          imageheight,
          imagewidth,
          margin,
          name,
          spacing,
          terrains,
          tilecount,
          tileheight,
          tiles,
          tilewidth,
          source
        )
    }

  implicit val decodeTiledLayer: Decoder[TiledLayer] =
    new Decoder[TiledLayer] {
      final def apply(c: HCursor): Decoder.Result[TiledLayer] =
        for {
          name    <- c.downField("name").as[String]
          data    <- c.downField("data").as[List[Int]]
          x       <- c.downField("x").as[Int]
          y       <- c.downField("y").as[Int]
          width   <- c.downField("width").as[Int]
          height  <- c.downField("height").as[Int]
          opacity <- c.downField("opacity").as[Double]
          typ     <- c.downField("type").as[String]
          visible <- c.downField("visible").as[Boolean]
        } yield TiledLayer(
          name,
          data,
          x,
          y,
          width,
          height,
          opacity,
          typ,
          visible
        )
    }

  implicit val decodeTiledMap: Decoder[TiledMap] =
    new Decoder[TiledMap] {
      final def apply(c: HCursor): Decoder.Result[TiledMap] =
        for {
          width           <- c.downField("width").as[Int]
          height          <- c.downField("height").as[Int]
          infinite        <- c.downField("infinite").as[Boolean]
          layers          <- c.downField("layers").as[List[TiledLayer]]
          nextobjectid    <- c.downField("nextobjectid").as[Int]
          orientation     <- c.downField("orientation").as[String]
          renderorder     <- c.downField("renderorder").as[String]
          tiledversion    <- c.downField("tiledversion").as[String]
          tilewidth       <- c.downField("tilewidth").as[Int]
          tileheight      <- c.downField("tileheight").as[Int]
          tilesets        <- c.downField("tilesets").as[List[TileSet]]
          `type`          <- c.downField("type").as[String]
          hexsidelength   <- c.downField("hexsidelength").as[Option[Int]]
          staggeraxis     <- c.downField("staggeraxis").as[Option[String]]
          staggerindex    <- c.downField("staggerindex").as[Option[String]]
          backgroundcolor <- c.downField("backgroundcolor").as[Option[String]]
        } yield TiledMap(
          width,
          height,
          infinite,
          layers,
          nextobjectid,
          orientation,
          renderorder,
          tiledversion,
          tilewidth,
          tileheight,
          tilesets,
          `type`,
          hexsidelength,
          staggeraxis,
          staggerindex,
          backgroundcolor
        )
    }

  implicit val decodeGlyphWrapper: Decoder[GlyphWrapper] =
    new Decoder[GlyphWrapper] {
      final def apply(c: HCursor): Decoder.Result[GlyphWrapper] =
        for {
          glyphs <- c.downField("glyphs").as[List[Glyph]]
        } yield GlyphWrapper(glyphs)
    }

  implicit val decodeGlyph: Decoder[Glyph] =
    new Decoder[Glyph] {
      final def apply(c: HCursor): Decoder.Result[Glyph] =
        for {
          char <- c.downField("char").as[String]
          x    <- c.downField("x").as[Int]
          y    <- c.downField("y").as[Int]
          w    <- c.downField("w").as[Int]
          h    <- c.downField("h").as[Int]
        } yield Glyph(char, x, y, w, h)
    }

}
