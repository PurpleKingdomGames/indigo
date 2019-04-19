package indigo.renderer

import indigo.gameengine.scenegraph.datatypes.Point
import indigo.shared.ClearColor
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLTexture
import indigo.gameengine.display.Vector2

final class ContextAndCanvas(val context: raw.WebGLRenderingContext, val canvas: html.Canvas, val width: Int,val  height: Int, val aspect: Float, val magnification: Int)

final class RendererConfig(val viewport: Viewport, val clearColor: ClearColor, val magnification: Int)
final class Viewport(val width: Int, val height: Int)

final class TextureRefAndOffset(val atlasName: String, val atlasSize: Vector2, val offset: Point)
final class AssetMapping(val mappings: Map[String, TextureRefAndOffset])
final class TextureLookupResult(val name: String, val texture: WebGLTexture)
final class LoadedTextureAsset(val name: String, val data: raw.ImageData)
