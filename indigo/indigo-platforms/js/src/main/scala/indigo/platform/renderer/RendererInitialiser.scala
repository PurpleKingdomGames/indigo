package indigo.platform.renderer

import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import indigo.platform.renderer.shared.LoadedTextureAsset

import org.scalajs.dom.{Element, html}
import indigo.platform.renderer.RenderingTechnology.WebGL1
import indigo.platform.renderer.RenderingTechnology.WebGL2

object RendererInitialiser {

  private val tech: RenderingTechnology = RenderingTechnology.WebGL1

  def setup(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): Renderer =
    tech match {
      case WebGL1 =>
        webgl1.RendererInit.setup(config, loadedTextureAssets, canvas)

      case WebGL2 =>
        webgl2.RendererInit.setup(config, loadedTextureAssets, canvas)
    }

  def createCanvas(width: Int, height: Int, parent: Element): html.Canvas =
    tech match {
      case WebGL1 =>
        webgl1.RendererInit.createCanvas(width, height, parent)

      case WebGL2 =>
        webgl2.RendererInit.createCanvas(width, height, parent)
    }

}

sealed trait RenderingTechnology
object RenderingTechnology {
  case object WebGL1 extends RenderingTechnology
  case object WebGL2 extends RenderingTechnology
}
