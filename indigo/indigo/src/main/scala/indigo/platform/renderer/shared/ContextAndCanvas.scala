package indigo.platform.renderer.shared

import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html

final class ContextAndCanvas(
    val context: WebGLRenderingContext,
    val canvas: html.Canvas,
    val magnification: Int
) {
  val width  = canvas.width
  val height = canvas.height
}
