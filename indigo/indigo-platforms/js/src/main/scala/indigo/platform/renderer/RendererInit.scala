package indigo.platform.renderer

import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.{Element, html, raw}
import scala.scalajs.js.Dynamic
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister

object RendererInit {

  def setup(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas, boundaryLocator: BoundaryLocator, animationsRegister: AnimationsRegister, fontRegister: FontRegister): Renderer = {
    val cNc = setupContextAndCanvas(canvas, config.magnification, config.antiAliasing)
    val r   = new RendererImpl(config, loadedTextureAssets, cNc, boundaryLocator, animationsRegister, fontRegister)
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

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def getContext(canvas: html.Canvas, antiAliasing: Boolean): WebGLRenderingContext = {
    val args =
      Dynamic.literal("premultipliedAlpha" -> false, "alpha" -> false, "antialias" -> antiAliasing)

    (canvas.getContext("webgl2", args)).asInstanceOf[raw.WebGLRenderingContext]
  }

  private def setupContextAndCanvas(canvas: html.Canvas, magnification: Int, antiAliasing: Boolean): ContextAndCanvas =
    new ContextAndCanvas(
      context = getContext(canvas, antiAliasing),
      canvas = canvas,
      magnification = magnification
    )

}
