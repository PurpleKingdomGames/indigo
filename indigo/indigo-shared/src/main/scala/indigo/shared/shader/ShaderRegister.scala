package indigo.shared.shader

import indigo.shared.QuickCache
import indigo.shared.CacheKey

final class ShaderRegister {

  implicit private val cache: QuickCache[RawShaderCode] = QuickCache.empty

  def register(shader: Shader): Unit =
    shader match {
      case s: EntityShader.Source =>
        registerEntityShader(s)

      case _: EntityShader.External =>
        ()

      case s: BlendShader.Source =>
        registerBlendShader(s)

      case _: BlendShader.External =>
        ()
    }

  def registerEntityShader(shader: EntityShader.Source): Unit = {
    QuickCache(shader.id.value) {
      RawShaderCode.fromEntityShader(shader)
    }
    ()
  }

  def registerBlendShader(shader: BlendShader.Source): Unit = {
    QuickCache(shader.id.value) {
      RawShaderCode.fromBlendShader(shader)
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
