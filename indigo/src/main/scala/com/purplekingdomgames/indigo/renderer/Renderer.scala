package com.purplekingdomgames.indigo.renderer

import org.scalajs.dom
import org.scalajs.dom.{html, raw}

import scala.language.implicitConversions

object Renderer {

  private var renderer: Option[IRenderer] = None

  def apply(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): IRenderer = {
    renderer match {
      case Some(r) => r
      case None =>
        val cNc = setupContextAndCanvas(canvas, config.magnification)

        val r = new RendererImpl(config, loadedTextureAssets, cNc)
        r.init()

        renderer = Some(r)
        renderer.get
    }
  }

  def createCanvas(width: Int, height: Int): html.Canvas = {
    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
    dom.document.body.appendChild(canvas)
    canvas.width = width
    canvas.height = height

    canvas
  }

  private def getContext(canvas: html.Canvas) =
    (canvas.getContext("webgl")|| canvas.getContext("experimental-webgl")).asInstanceOf[raw.WebGLRenderingContext]

  private def setupContextAndCanvas(canvas: html.Canvas, magnification: Int): ContextAndCanvas = {
    ContextAndCanvas(
      context = getContext(canvas),
      canvas = canvas,
      width = canvas.clientWidth,
      height = canvas.clientHeight,
      aspect = canvas.clientWidth.toFloat / canvas.clientHeight.toFloat,
      magnification = magnification
    )
  }

}