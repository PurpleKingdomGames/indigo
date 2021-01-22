package indigo.facades.worker

import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontSpriteSheet
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontKey

import scala.scalajs.js
import scalajs.js.JSConverters._

object FontInfoConversion {

  def toJS(fontInfo: FontInfo): js.Any =
    js.Dynamic.literal(
      fontKey = fontInfo.fontKey.key,
      fontSpriteSheet = FontSpriteSheetConversion.toJS(fontInfo.fontSpriteSheet),
      unknownChar = FontCharConversion.toJS(fontInfo.unknownChar),
      fontChars = fontInfo.fontChars.map(FontCharConversion.toJS).toJSArray,
      caseSensitive = fontInfo.caseSensitive
    )

  def fromJS(obj: js.Any): FontInfo =
    fromFontInfoJS(obj.asInstanceOf[FontInfoJS])

  def fromFontInfoJS(res: FontInfoJS): FontInfo =
    FontInfo(
      fontKey = FontKey(res.fontKey),
      fontSpriteSheet = FontSpriteSheetConversion.fromFontSpriteSheetJS(res.fontSpriteSheet),
      unknownChar = FontCharConversion.fromFontCharJS(res.unknownChar),
      fontChars = res.fontChars.toList.map(FontCharConversion.fromFontCharJS),
      caseSensitive = res.caseSensitive
    )

  object FontSpriteSheetConversion {

    def toJS(fontSpriteSheet: FontSpriteSheet): js.Any =
      js.Dynamic.literal(
        material = MaterialConversion.toJS(fontSpriteSheet.material),
        size = PointConversion.toJS(fontSpriteSheet.size)
      )

    def fromJS(obj: js.Any): FontSpriteSheet =
      fromFontSpriteSheetJS(obj.asInstanceOf[FontSpriteSheetJS])

    def fromFontSpriteSheetJS(res: FontSpriteSheetJS): FontSpriteSheet =
      FontSpriteSheet(
        material = MaterialConversion.fromMaterialJS(res.material),
        size = PointConversion.fromPointJS(res.size)
      )

  }

  object FontCharConversion {

    def toJS(fontChar: FontChar): js.Any =
      js.Dynamic.literal(
        character = fontChar.character,
        bounds = RectangleConversion.toJS(fontChar.bounds)
      )

    def fromJS(obj: js.Any): FontChar =
      fromFontCharJS(obj.asInstanceOf[FontCharJS])

    def fromFontCharJS(res: FontCharJS): FontChar =
      FontChar(
        character = res.character,
        bounds = RectangleConversion.fromRectangleJS(res.bounds)
      )

  }

}
