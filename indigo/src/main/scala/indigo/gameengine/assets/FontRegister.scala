package indigo.gameengine.assets

import indigo.gameengine.scenegraph.datatypes.{FontInfo, FontKey}

import scala.collection.mutable

object FontRegister {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val fontRegistry: mutable.HashMap[FontKey, FontInfo] = mutable.HashMap()

  private[gameengine] def register(fontInfo: FontInfo): Unit = {
    fontRegistry.update(fontInfo.fontKey, fontInfo)
    ()
  }

  def findByFontKey(fontKey: FontKey): Option[FontInfo] =
    fontRegistry.get(fontKey)

}
