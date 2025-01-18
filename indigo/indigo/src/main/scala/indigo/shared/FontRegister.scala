package indigo.shared

import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey

import scala.annotation.nowarn

final class FontRegister {

  implicit private val cache: QuickCache[FontInfo] = QuickCache.empty

  def kill(): Unit =
    clearRegister()

  @nowarn("msg=unused")
  def register(fontInfo: FontInfo): Unit = {
    QuickCache(fontInfo.fontKey.toString)(fontInfo)
    ()
  }

  def findByFontKey(fontKey: FontKey): Option[FontInfo] =
    cache.fetch(CacheKey(fontKey.toString))

  @nowarn("msg=unused")
  def clearRegister(): Unit = {
    cache.purgeAll()
    ()
  }

}
