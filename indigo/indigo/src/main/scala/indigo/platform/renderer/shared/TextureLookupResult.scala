package indigo.platform.renderer.shared

import indigo.platform.assets.AtlasId

import org.scalajs.dom.raw.WebGLTexture

final case class TextureLookupResult(name: AtlasId, texture: WebGLTexture)
