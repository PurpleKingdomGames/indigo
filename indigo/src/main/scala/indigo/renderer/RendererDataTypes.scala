package indigo.renderer

import indigo.gameengine.scenegraph.datatypes.Point
import indigo.shared.ClearColor
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLTexture
import indigo.gameengine.display.Vector2

//TODO: Replace case classes with classes
final case class ContextAndCanvas(context: raw.WebGLRenderingContext, canvas: html.Canvas, width: Int, height: Int, aspect: Float, magnification: Int)

final case class RendererConfig(viewport: Viewport, clearColor: ClearColor, magnification: Int)
final case class Viewport(width: Int, height: Int)

final case class TextureRefAndOffset(atlasName: String, atlasSize: Vector2, offset: Point)
final case class AssetMapping(mappings: Map[String, TextureRefAndOffset])
final case class TextureLookupResult(name: String, texture: WebGLTexture)
final case class LoadedTextureAsset(name: String, data: raw.ImageData)
