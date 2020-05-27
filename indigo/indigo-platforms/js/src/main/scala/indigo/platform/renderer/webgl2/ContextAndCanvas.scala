package indigo.platform.renderer.webgl2

import org.scalajs.dom.{html, raw}

final class ContextAndCanvas(val context: raw.WebGLRenderingContext, val canvas: html.Canvas, val magnification: Int)
