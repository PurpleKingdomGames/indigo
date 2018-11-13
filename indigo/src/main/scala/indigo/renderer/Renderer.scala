package indigo.renderer

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.{Element, html, raw}

object Renderer {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var renderer: Option[IRenderer] = None

  def apply(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): IRenderer =
    renderer match {
      case Some(r) => r
      case None =>
        val cNc = setupContextAndCanvas(canvas, config.magnification)

        val r = new RendererImpl(config, loadedTextureAssets, cNc)
        r.init()

        renderer = Some(r)
        r
    }

  def createCanvas(width: Int, height: Int, parent: Element): html.Canvas = {
    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
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
    ContextAndCanvas(
      context = getContext(canvas),
      canvas = canvas,
      width = canvas.clientWidth,
      height = canvas.clientHeight,
      aspect = canvas.clientWidth.toFloat / canvas.clientHeight.toFloat,
      magnification = magnification
    )

}
