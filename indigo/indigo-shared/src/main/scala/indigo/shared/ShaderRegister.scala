package indigo.shared

import indigo.shared.display.{Shader, ShaderId}

final class ShaderRegister {

  implicit private val cache: QuickCache[Shader] = QuickCache.empty

  def register(shader: Shader): Unit = {
    QuickCache(shader.id.value)(shader)
    ()
  }

  def findByFontKey(shaderId: ShaderId): Option[Shader] =
    cache.fetch(CacheKey(shaderId.value))

  def clearRegister(): Unit = {
    cache.purgeAll()
    ()
  }

}
