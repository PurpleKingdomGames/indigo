package indigo.shared.shader

import indigo.shared.CacheKey
import indigo.shared.QuickCache

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

      case _ =>
        ()
    }

  def remove(id: ShaderId): Unit =
    cache.purge(CacheKey(id.toString))

  def registerEntityShader(shader: EntityShader.Source): Unit = {
    QuickCache(shader.id.toString) {
      RawShaderCode.fromEntityShader(shader)
    }
    ()
  }

  def registerBlendShader(shader: BlendShader.Source): Unit = {
    QuickCache(shader.id.toString) {
      RawShaderCode.fromBlendShader(shader)
    }
    ()
  }

  def toSet: Set[RawShaderCode] =
    cache.all.map(_._2).toSet

  def clearRegister(): Unit = {
    cache.purgeAll()
    ()
  }

}
