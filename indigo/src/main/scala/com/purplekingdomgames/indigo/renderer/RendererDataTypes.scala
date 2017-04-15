package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLTexture

final case class ContextAndCanvas(context: raw.WebGLRenderingContext, canvas: html.Canvas, width: Int, height: Int, aspect: Float, magnification: Int)

final case class RendererConfig(viewport: Viewport, clearColor: ClearColor, magnification: Int)
final case class Viewport(width: Int, height: Int)
final case class ClearColor(r: Double, g: Double, b: Double, a: Double) {
  def forceOpaque: ClearColor = this.copy(a = 1d)
  def forceTransparent: ClearColor = this.copy(a = 0d)
  def alphaAsPercent: Int = (a * 100).toInt
  def alphaPercent(percent: Int): ClearColor = this.copy(a = percent.toDouble * 0.01)
  def withR(v: Double): ClearColor = this.copy(r = v)
  def withG(v: Double): ClearColor = this.copy(g = v)
  def withB(v: Double): ClearColor = this.copy(b = v)
  def withA(v: Double): ClearColor = this.copy(a = v)
}

object ClearColor {
  def apply(r: Double, g: Double, b: Double): ClearColor = ClearColor(r, g, b, 1d)

  val Red: ClearColor = ClearColor(1, 0, 0)
  val Green: ClearColor = ClearColor(0, 1, 0)
  val Blue: ClearColor = ClearColor(0, 0, 1)
  val Black: ClearColor = ClearColor(0, 0, 0)
  val White: ClearColor = ClearColor(1, 1, 1)
}

final case class TextureRefAndOffset(atlasName: String, atlasSize: Vector2, offset: Point)
final case class AssetMapping(mappings: Map[String, TextureRefAndOffset])
final case class TextureLookupResult(name: String, texture: WebGLTexture)
final case class LoadedTextureAsset(name: String, data: raw.ImageData)
