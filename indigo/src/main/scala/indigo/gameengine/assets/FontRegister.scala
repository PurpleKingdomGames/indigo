package indigo.gameengine.assets

import indigo.gameengine.scenegraph.datatypes.{FontInfo, FontKey}

import scala.collection.mutable

final class FontRegister(val fonts: Map[FontKey, FontInfo])
object FontRegister {

  def apply(fonts: Map[FontKey, FontInfo]): FontRegister =
    new FontRegister(fonts)

  def fromSet(fonts: Set[FontInfo]): FontRegister =
    new FontRegister(
      fonts.foldLeft(Map.empty[FontKey, FontInfo])((acc, n) => acc + (n.fontKey -> n))
    )

  def findByFontKey(register: FontRegister, fontKey: FontKey): Option[FontInfo] =
    register.fonts.get(fontKey)

}
