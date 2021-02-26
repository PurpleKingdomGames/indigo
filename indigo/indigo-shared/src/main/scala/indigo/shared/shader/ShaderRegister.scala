package indigo.shared.shader

import indigo.shared.QuickCache
import indigo.shared.CacheKey

final class ShaderRegister {

  implicit private val cache: QuickCache[RawShaderCode] = QuickCache.empty

  def register(shader: Shader.Source): Unit = {
    QuickCache(shader.id.value) {
      RawShaderCode.fromCustomShader(shader)
    }
    ()
  }

  def findByFontKey(shaderId: ShaderId): Option[RawShaderCode] =
    cache.fetch(CacheKey(shaderId.value))

  def toSet: Set[RawShaderCode] =
    cache.all.map(_._2).toSet

  def clearRegister(): Unit = {
    cache.purgeAll()
    ()
  }

}
