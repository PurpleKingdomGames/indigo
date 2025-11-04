package indigo.shared.shader

import indigo.shared.CacheKey
import indigo.shared.QuickCache

import scala.annotation.nowarn

final class ShaderRegister:

  private given cache: QuickCache[RawShaderCode] = QuickCache.empty

  def kill(): Unit =
    clearRegister()

  def register(shader: ShaderProgram): Unit =
    shader match
      case s: EntityShader.Source =>
        registerEntityShader(s)

      case _: EntityShader.External =>
        ()

      case s: BlendShader.Source =>
        registerBlendShader(s)

      case _: BlendShader.External =>
        ()

      case s: UltravioletShader =>
        registerUVShader(s)

  @nowarn("msg=discarded")
  def remove(id: ShaderId): Unit =
    cache.purge(CacheKey(id.toString))

  @nowarn("msg=unused")
  def registerEntityShader(shader: EntityShader.Source): Unit =
    QuickCache(shader.id.toString) {
      RawShaderCode.fromEntityShader(shader)
    }
    ()

  @nowarn("msg=unused")
  def registerBlendShader(shader: BlendShader.Source): Unit =
    QuickCache(shader.id.toString) {
      RawShaderCode.fromBlendShader(shader)
    }
    ()

  @nowarn("msg=unused")
  def registerUVShader(shader: UltravioletShader): Unit =
    QuickCache(shader.id.toString) {
      RawShaderCode.fromUltravioletShader(shader)
    }
    ()

  def toSet: Set[RawShaderCode] =
    cache.all.map(_._2).toSet

  @nowarn("msg=unused")
  def clearRegister(): Unit =
    cache.purgeAll()
    ()
