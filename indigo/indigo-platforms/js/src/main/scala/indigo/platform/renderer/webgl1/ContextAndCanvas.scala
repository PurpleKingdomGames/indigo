package indigo.platform.renderer.webgl1

import org.scalajs.dom.{html, raw}

final class ContextAndCanvas(val context: raw.WebGLRenderingContext, val canvas: html.Canvas, val width: Int, val height: Int, val aspect: Float, val magnification: Int)
