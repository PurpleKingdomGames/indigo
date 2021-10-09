package indigo.platform.renderer.shared

import org.scalajs.dom.html
import org.scalajs.dom.raw

final class ContextAndCanvas(val context: raw.WebGLRenderingContext, val canvas: html.Canvas, val magnification: Int)
