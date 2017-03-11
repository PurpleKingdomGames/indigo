package com.purplekingdomgames.indigo.renderer

import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLFramebuffer, WebGLTexture}

object FrameBufferFunctions {

  def createAndSetupTexture(cNc: ContextAndCanvas): WebGLTexture = {
    val gl = cNc.context
    val texture = RendererFunctions.createAndBindTexture(gl)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, cNc.width, cNc.height, 0, RGBA, UNSIGNED_BYTE, null)

    texture
  }

  def createFrameBuffer(cNc: ContextAndCanvas, texture: WebGLTexture): FrameBufferComponents = {
    val gl = cNc.context

    val frameBuffer = gl.createFramebuffer()
    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, texture, 0)

    FrameBufferComponents(frameBuffer, texture)
  }

  def switchToFramebuffer(cNc: ContextAndCanvas, frameBuffer: WebGLFramebuffer): Unit = {
    val gl = cNc.context

    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    cNc.context.clear(COLOR_BUFFER_BIT)
    cNc.context.clearColor(1, 1, 1, 0)
  }

  def switchToCanvas(cNc: ContextAndCanvas, config: RendererConfig): Unit = {
    val gl = cNc.context

    gl.bindFramebuffer(FRAMEBUFFER, null)

    cNc.context.clear(COLOR_BUFFER_BIT)
    cNc.context.clearColor(config.clearColor.r, config.clearColor.g, config.clearColor.b, config.clearColor.a)
  }
}

case class FrameBufferComponents(frameBuffer: WebGLFramebuffer, texture: WebGLTexture)
