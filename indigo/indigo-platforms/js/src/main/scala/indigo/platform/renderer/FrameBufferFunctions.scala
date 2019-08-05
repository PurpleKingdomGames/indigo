package indigo.platform.renderer

import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLFramebuffer, WebGLTexture}
import indigo.shared.EqualTo._
import scala.annotation.tailrec
import scalajs.js.JSConverters._
import indigo.facades.ColorAttachments

object FrameBufferFunctions {

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  private def createAndSetupTexture(gl: WebGLRenderingContext, width: Int, height: Int): WebGLTexture = {
    val texture = RendererFunctions.createAndBindTexture(gl)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, null)

    texture
  }

  def createFrameBuffer(gl: WebGLRenderingContext, textureCount: Int, width: Int, height: Int): FrameBufferComponents = {
    val minTextureCount: Int = if (textureCount < 0) 0 else textureCount

    val frameBuffer = gl.createFramebuffer()

    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    @tailrec
    def rec(remaining: Int, acc: List[WebGLTexture]): FrameBufferComponents =
      remaining match {
        case i if i === 0 =>
          val texture: WebGLTexture = createAndSetupTexture(gl, width, height)
          gl.framebufferTexture2D(FRAMEBUFFER, ColorAttachments.intToColorAttachment(i), TEXTURE_2D, texture, 0)

          FrameBufferComponents(frameBuffer, texture, acc)

        case i =>
          val texture: WebGLTexture = createAndSetupTexture(gl, width, height)
          gl.framebufferTexture2D(FRAMEBUFFER, ColorAttachments.intToColorAttachment(i), TEXTURE_2D, texture, 0)

          rec(remaining - 1, texture :: acc)
      }

    rec(minTextureCount, Nil)
  }

  def switchToFramebuffer(gl: WebGLRenderingContext, frameBuffer: WebGLFramebuffer, clearColor: ClearColor): Unit = {
    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)
    gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
    gl.clear(COLOR_BUFFER_BIT)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def switchToCanvas(gl: WebGLRenderingContext, clearColor: ClearColor): Unit = {
    gl.bindFramebuffer(FRAMEBUFFER, null)
    gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
    gl.clear(COLOR_BUFFER_BIT)
  }
}

final class FrameBufferComponents(val frameBuffer: WebGLFramebuffer, val texture: WebGLTexture, val textures: List[WebGLTexture]) {

  lazy val colorAttachments: scalajs.js.Array[Int] =
    textures match {
      case Nil =>
        scalajs.js.Array(COLOR_ATTACHMENT0)

      case l =>
        (0 to l.length).map(ColorAttachments.intToColorAttachment).toJSArray
    }

}
object FrameBufferComponents {
  def apply(frameBuffer: WebGLFramebuffer, texture: WebGLTexture, textures: List[WebGLTexture]): FrameBufferComponents =
    new FrameBufferComponents(frameBuffer, texture, textures)
}
