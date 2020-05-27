package indigo.platform.renderer.webgl1

import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.{Element, html, raw}
import scala.scalajs.js.Dynamic
import indigo.platform.renderer.shared.LoadedTextureAsset

object RendererInit {

  def setup(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): Renderer = {
    val cNc = setupContextAndCanvas(canvas, config.magnification, config.antiAliasing)
    val r   = new RendererWebGL1(config, loadedTextureAssets, cNc)
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
      width = canvas.clientWidth,
      height = canvas.clientHeight,
      aspect = canvas.clientWidth.toFloat / canvas.clientHeight.toFloat,
      magnification = magnification
    )

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def getContext(canvas: html.Canvas, antiAliasing: Boolean): WebGLRenderingContext = {
    val args =
      Dynamic.literal("premultipliedAlpha" -> false, "alpha" -> false, "antialias" -> antiAliasing)

    (canvas.getContext("webgl", args) || canvas.getContext("experimental-webgl", args)).asInstanceOf[raw.WebGLRenderingContext]
  }

}
