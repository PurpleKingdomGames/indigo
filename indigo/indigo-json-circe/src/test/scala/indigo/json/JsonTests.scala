package indigo.json

import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.Rectangle
import indigo.shared.formats.*

class JsonTests extends munit.FunSuite {

  test("Create an Aseprite asset.should be able to parse the json definition") {
    assertEquals(Json.asepriteFromJson(AsepriteSampleData.json), AsepriteSampleData.aseprite)
  }

  test("Create an Aseprite asset.should fail to parse a bad json definition") {
    assertEquals(Json.asepriteFromJson("nonsense"), None)
  }

  test("Fonts.should be able to parse the json definition") {
    val actual = Json.readFontToolJson(FontToolSampleData.json).get

    val expected = FontToolSampleData.sample

    assertEquals(actual.length, expected.length)
    assertEquals(actual.forall(c => expected.contains(c)), true)
  }

  test("Fonts.should fail to parse a bad json definition") {
    assertEquals(Json.readFontToolJson("nonsense"), None)
  }

  test("Tiled.should be able to parse Tiled json") {

    val actual = Json.tiledMapFromJson(TiledSampleData.sampleMap)

    assertEquals(actual.isDefined, true)

  }

}

object FontToolSampleData {

  val json: String =
    """
{
    "name": "fontname",
    "size": 16,
    "padding": 1,
    "glyphs": [
        {
            "unicode": 100,
            "char": "d",
            "x": 1,
            "y": 1,
            "w": 10,
            "h": 25
        },
        {
            "unicode": 98,
            "char": "b",
            "x": 13,
            "y": 1,
            "w": 10,
            "h": 25
        },
        {
            "unicode": 99,
            "char": "c",
            "x": 25,
            "y": 1,
            "w": 9,
            "h": 25
        },
        {
            "unicode": 97,
            "char": "a",
            "x": 36,
            "y": 1,
            "w": 10,
            "h": 25
        }
    ]
}
    """

  val sample: List[FontChar] =
    List(
      FontChar("a", Rectangle(36, 1, 10, 25)),
      FontChar("b", Rectangle(13, 1, 10, 25)),
      FontChar("c", Rectangle(25, 1, 9, 25)),
      FontChar("d", Rectangle(1, 1, 10, 25))
    )

}

object AsepriteSampleData {

  val json: String =
    """
      |{ "frames": [
      |   {
      |    "filename": "trafficlights 0.ase",
      |    "frame": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "rotated": false,
      |    "trimmed": false,
      |    "spriteSourceSize": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "sourceSize": { "w": 64, "h": 64 },
      |    "duration": 100
      |   },
      |   {
      |    "filename": "trafficlights 1.ase",
      |    "frame": { "x": 64, "y": 0, "w": 64, "h": 64 },
      |    "rotated": false,
      |    "trimmed": false,
      |    "spriteSourceSize": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "sourceSize": { "w": 64, "h": 64 },
      |    "duration": 100
      |   },
      |   {
      |    "filename": "trafficlights 2.ase",
      |    "frame": { "x": 0, "y": 64, "w": 64, "h": 64 },
      |    "rotated": false,
      |    "trimmed": false,
      |    "spriteSourceSize": { "x": 0, "y": 0, "w": 64, "h": 64 },
      |    "sourceSize": { "w": 64, "h": 64 },
      |    "duration": 100
      |   }
      | ],
      | "meta": {
      |  "app": "http://www.aseprite.org/",
      |  "version": "1.1.13",
      |  "image": "/Users/dave/repos/indigo/sandbox/trafficlights.png",
      |  "format": "RGBA8888",
      |  "size": { "w": 128, "h": 128 },
      |  "scale": "1",
      |  "frameTags": [
      |   { "name": "lights", "from": 0, "to": 2, "direction": "forward" }
      |  ]
      | }
      |}
    """.stripMargin

  val aseprite: Option[Aseprite] = Option {
    Aseprite(
      frames = List(
        AsepriteFrame(
          filename = "trafficlights 0.ase",
          frame = AsepriteRectangle(0, 0, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        ),
        AsepriteFrame(
          filename = "trafficlights 1.ase",
          frame = AsepriteRectangle(64, 0, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        ),
        AsepriteFrame(
          filename = "trafficlights 2.ase",
          frame = AsepriteRectangle(0, 64, 64, 64),
          rotated = false,
          trimmed = false,
          spriteSourceSize = AsepriteRectangle(0, 0, 64, 64),
          sourceSize = AsepriteSize(64, 64),
          duration = 100
        )
      ),
      meta = AsepriteMeta(
        app = "http://www.aseprite.org/",
        version = "1.1.13",
        format = "RGBA8888",
        size = AsepriteSize(128, 128),
        scale = "1",
        frameTags = List(
          AsepriteFrameTag(
            name = "lights",
            from = 0,
            to = 2,
            direction = "forward"
          )
        )
      )
    )
  }

}

object TiledSampleData {

  val sampleMap: String =
    """
{
  "compressionlevel": 0,
  "editorsettings": {
    "export": {
      "format": "json",
      "target": "level.json"
    }
  },
  "height": 11,
  "infinite": false,
  "layers": [{
    "data": [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 19, 19, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 36, 36, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 69, 70, 70, 62, 2, 2, 2, 2, 61, 71, 0, 0, 0, 0, 69, 20, 0, 0, 0, 0, 0, 0, 35, 36, 36, 8, 7, 37, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 20, 0, 0, 0, 0, 0, 0, 0],
    "height": 11,
    "id": 1,
    "name": "Tile Layer 1",
    "opacity": 1,
    "type": "tilelayer",
    "visible": true,
    "width": 19,
    "x": 0,
    "y": 0
  }],
  "nextlayerid": 2,
  "nextobjectid": 1,
  "orientation": "orthogonal",
  "renderorder": "right-down",
  "tiledversion": "1.3.2",
  "tileheight": 32,
  "tilesets": [{
    "columns": 17,
    "firstgid": 1,
    "image": "..\/Graphics\/Palm Tree Island\/Sprites\/Terrain\/Terrain (32x32).png",
    "imageheight": 160,
    "imagewidth": 544,
    "margin": 0,
    "name": "Palm Island",
    "spacing": 0,
    "tilecount": 85,
    "tileheight": 32,
    "tilewidth": 32
  }],
  "tilewidth": 32,
  "type": "map",
  "version": 1.2,
  "width": 19
}
    """

}
