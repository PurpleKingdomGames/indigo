package indigo.platform.renderer

import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.ContextAndCanvas

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.{Element, html, raw}
import scala.scalajs.js.Dynamic
import indigo.platform.renderer.RenderingTechnology.WebGL1
import indigo.platform.renderer.RenderingTechnology.WebGL2
import indigo.platform.renderer.webgl1.RendererWebGL1
import indigo.platform.renderer.webgl2.RendererWebGL2

object RendererInitialiser {

  private val tech: RenderingTechnology = RenderingTechnology.WebGL1

  def setup(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): Renderer = {
    val cNc = setupContextAndCanvas(canvas, config.magnification, config.antiAliasing)
    val r =
      tech match {
        case RenderingTechnology.WebGL1 =>
          new RendererWebGL1(config, loadedTextureAssets, cNc)

        case RenderingTechnology.WebGL2 =>
          new RendererWebGL2(config, loadedTextureAssets, cNc)
      }

    r.init()
    r
  }

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.NonUnitStatements",
      "org.wartremover.warts.AsInstanceOf",
      "org.wartremover.warts.Null",
      "org.wartremover.warts.Equals",
      "org.wartremover.warts.Var"
    )
  )
  def createCanvas(width: Int, height: Int, parent: Element): html.Canvas = {
    var canvas: html.Canvas = dom.document.getElementById("indigo").asInstanceOf[html.Canvas]

    if (canvas == null) {
      canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
      parent.appendChild(canvas)
      canvas.id = "indigo"
      canvas.width = width
      canvas.height = height
    }

    canvas
  }

  private def setupContextAndCanvas(canvas: html.Canvas, magnification: Int, antiAliasing: Boolean): ContextAndCanvas =
    new ContextAndCanvas(
      context = getContext(canvas, antiAliasing),
      canvas = canvas,
      magnification = magnification
    )

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def getContext(canvas: html.Canvas, antiAliasing: Boolean): WebGLRenderingContext = {
    val args =
      Dynamic.literal("premultipliedAlpha" -> false, "alpha" -> false, "antialias" -> antiAliasing)

    tech match {
      case WebGL1 =>
        (canvas.getContext("webgl", args) || canvas.getContext("experimental-webgl", args)).asInstanceOf[raw.WebGLRenderingContext]

      case WebGL2 =>
        (canvas.getContext("webgl2", args)).asInstanceOf[raw.WebGLRenderingContext]
    }

  }

}
