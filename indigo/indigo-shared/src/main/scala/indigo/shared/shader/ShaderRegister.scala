package indigo.shared.shader

import indigo.shared.QuickCache
import indigo.shared.CacheKey

final class ShaderRegister {

  implicit private val cache: QuickCache[Shader] = QuickCache.empty

  def register(shader: CustomShader.Source): Unit = {
    QuickCache(shader.id.value) {
      Shader.fromCustomShader(shader)
    }
    ()
  }

  def findByFontKey(shaderId: ShaderId): Option[Shader] =
    cache.fetch(CacheKey(shaderId.value))

  def toSet: Set[Shader] =
    cache.all.map(_._2).toSet

  def clearRegister(): Unit = {
    cache.purgeAll()
    ()
  }

}
