package indigoplugin.datatypes

import io.circe._

// format: off
final case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta) {
  def render: String =
    s"""Aseprite(${frames.map(_.render).mkString("List(", ",", ")")}, ${meta.render})"""
}
object Aseprite{

  implicit val decodeAseprite: Decoder[Aseprite] =
    new Decoder[Aseprite] {
      final def apply(c: HCursor): Decoder.Result[Aseprite] =
        for {
          frames <- c.downField("frames").as[List[AsepriteFrame]]
          meta   <- c.downField("meta").as[AsepriteMeta]
        } yield Aseprite(frames, meta)
    }
}

final case class AsepriteFrame(
    filename: String,
    frame: AsepriteRectangle,
    rotated: Boolean,
    trimmed: Boolean,
    spriteSourceSize: AsepriteRectangle,
    sourceSize: AsepriteSize,
    duration: Int
) {
  def render: String =
    s"""AsepriteFrame("$filename", ${frame.render}, $rotated, $trimmed, ${spriteSourceSize.render}, ${sourceSize.render}, $duration)"""
}
object AsepriteFrame {

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

}

final case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int) {
  def render: String =
    s"""AsepriteRectangle($x, $y, $w, $h)"""
}
object AsepriteRectangle {

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

}

final case class AsepriteMeta(
    app: String,
    version: String,
    format: String,
    size: AsepriteSize,
    scale: String,
    frameTags: List[AsepriteFrameTag]
) {
  def render: String =
    s"""AsepriteMeta("$app", "$version", "$format", ${size.render}, "$scale", ${frameTags.map(_.render).mkString("List(", ",", ")")})"""
}
object AsepriteMeta {

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

}

final case class AsepriteSize(w: Int, h: Int) {
  def render: String =
    s"""AsepriteSize($w, $h)"""
}
object AsepriteSize {

  implicit val decodeAsepriteSize: Decoder[AsepriteSize] =
    new Decoder[AsepriteSize] {
      final def apply(c: HCursor): Decoder.Result[AsepriteSize] =
        for {
          w <- c.downField("w").as[Int]
          h <- c.downField("h").as[Int]
        } yield AsepriteSize(w, h)
    }

}

final case class AsepriteFrameTag(name: String, from: Int, to: Int, direction: String) {
  def render: String =
    s"""AsepriteFrameTag("$name", $from, $to, "$direction")"""
}
object AsepriteFrameTag {

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

}
// format: on
