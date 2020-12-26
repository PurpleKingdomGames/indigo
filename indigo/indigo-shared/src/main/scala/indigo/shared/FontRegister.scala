package indigo.shared

import indigo.shared.datatypes.{FontInfo, FontKey}

final class FontRegister {

  implicit private val cache: QuickCache[FontInfo] = QuickCache.empty

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def register(fontInfo: FontInfo): Unit = {
    QuickCache(fontInfo.fontKey.key)(fontInfo)
    ()
  }

  def findByFontKey(fontKey: FontKey): Option[FontInfo] =
    cache.fetch(CacheKey(fontKey.key))

  // @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def clearRegister(): Unit = {
    cache.purgeAll()
    ()
  }

}
