package indigo.renderer

import indigo.runtime.metrics._
import indigo.gameengine.display.Displayable

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.{Element, html, raw}

trait Renderer {
  def init(): Unit
  def drawScene(displayable: Displayable, metrics: Metrics): Unit
}

object Renderer {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var renderer: Option[Renderer] = None

  def apply(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): Renderer =
    renderer match {
      case Some(r) => r
      case None =>
        val cNc = setupContextAndCanvas(canvas, config.magnification)

        val r = new RendererImpl(config, loadedTextureAssets, cNc)
        r.init()

        renderer = Some(r)
        r
    }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.AsInstanceOf"))
  def createCanvas(width: Int, height: Int, parent: Element): html.Canvas = {
    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
    parent.appendChild(canvas)
    canvas.width = width
    canvas.height = height

    canvas
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def getContext(canvas: html.Canvas): WebGLRenderingContext =
    (canvas.getContext("webgl") || canvas.getContext("experimental-webgl")).asInstanceOf[raw.WebGLRenderingContext]

  private def setupContextAndCanvas(canvas: html.Canvas, magnification: Int): ContextAndCanvas =
    new ContextAndCanvas(
      context = getContext(canvas),
      canvas = canvas,
      width = canvas.clientWidth,
      height = canvas.clientHeight,
      aspect = canvas.clientWidth.toFloat / canvas.clientHeight.toFloat,
      magnification = magnification
    )

}
