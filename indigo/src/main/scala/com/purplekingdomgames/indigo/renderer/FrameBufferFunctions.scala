package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.shared.ClearColor
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

  def switchToFramebuffer(cNc: ContextAndCanvas, frameBuffer: WebGLFramebuffer, clearColor: ClearColor): Unit = {
    val gl = cNc.context

    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    cNc.context.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
    cNc.context.clear(COLOR_BUFFER_BIT)
  }

  def switchToCanvas(cNc: ContextAndCanvas, clearColor: ClearColor): Unit = {
    val gl = cNc.context

    gl.bindFramebuffer(FRAMEBUFFER, null)

    cNc.context.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
    cNc.context.clear(COLOR_BUFFER_BIT)
  }
}

case class FrameBufferComponents(frameBuffer: WebGLFramebuffer, texture: WebGLTexture)
