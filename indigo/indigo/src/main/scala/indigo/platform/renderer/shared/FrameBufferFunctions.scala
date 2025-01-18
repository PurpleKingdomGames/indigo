package indigo.platform.renderer.shared

import indigo.facades.ColorAttachments
import indigo.shared.datatypes.RGBA
import org.scalajs.dom.WebGLFramebuffer
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.*
import org.scalajs.dom.WebGLTexture

object FrameBufferFunctions {

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def createAndSetupTexture(gl: WebGLRenderingContext, width: Int, height: Int): WebGLTexture = {
    val texture = WebGLHelper.createAndBindTexture(gl)

    gl.texImage2D(
      TEXTURE_2D,
      0,
      WebGLRenderingContext.RGBA,
      width,
      height,
      0,
      WebGLRenderingContext.RGBA,
      UNSIGNED_BYTE,
      null
    )

    texture
  }

  def createFrameBufferSingle(
      gl: WebGLRenderingContext,
      width: Int,
      height: Int
  ): FrameBufferComponents.SingleOutput = {
    import ColorAttachments._

    val frameBuffer: WebGLFramebuffer = gl.createFramebuffer()

    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    val diffuse = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, diffuse, 0)

    FrameBufferComponents.SingleOutput(
      frameBuffer,
      diffuse
    )
  }

  def createFrameBufferMulti(gl: WebGLRenderingContext, width: Int, height: Int): FrameBufferComponents.MultiOutput = {
    import ColorAttachments._
    // val minTextureCount: Int          = Math.max(0, textureCount)
    val frameBuffer: WebGLFramebuffer = gl.createFramebuffer()

    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    val albedo = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT0, TEXTURE_2D, albedo, 0)

    val emissive = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT1, TEXTURE_2D, emissive, 0)

    val normal = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT2, TEXTURE_2D, normal, 0)

    val specular = createAndSetupTexture(gl, width, height)
    gl.framebufferTexture2D(FRAMEBUFFER, COLOR_ATTACHMENT3, TEXTURE_2D, specular, 0)

    FrameBufferComponents.MultiOutput(
      frameBuffer,
      albedo,
      emissive,
      normal,
      specular
    )
  }

  def switchToFramebuffer(
      gl: WebGLRenderingContext,
      frameBuffer: WebGLFramebuffer,
      clearColor: RGBA,
      clear: Boolean
  ): Unit = {
    gl.bindFramebuffer(FRAMEBUFFER, frameBuffer)

    if (clear) {
      gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
      gl.clear(COLOR_BUFFER_BIT)
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def switchToCanvas(gl: WebGLRenderingContext, clearColor: RGBA): Unit = {
    gl.bindFramebuffer(FRAMEBUFFER, null)
    gl.clearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
    gl.clear(COLOR_BUFFER_BIT)
  }
}

sealed trait FrameBufferComponents {
  val frameBuffer: WebGLFramebuffer
  val colorAttachments: scalajs.js.Array[Int]
}

object FrameBufferComponents {

  final class MultiOutput(
      val frameBuffer: WebGLFramebuffer,
      val albedo: WebGLTexture,
      val emissive: WebGLTexture,
      val normal: WebGLTexture,
      val specular: WebGLTexture
  ) extends FrameBufferComponents {
    val colorAttachments: scalajs.js.Array[Int] =
      scalajs.js.Array[Int](
        ColorAttachments.COLOR_ATTACHMENT0,
        ColorAttachments.COLOR_ATTACHMENT1,
        ColorAttachments.COLOR_ATTACHMENT2,
        ColorAttachments.COLOR_ATTACHMENT3
      )
  }
  object MultiOutput {
    def apply(
        frameBuffer: WebGLFramebuffer,
        albedo: WebGLTexture,
        emissive: WebGLTexture,
        normal: WebGLTexture,
        specular: WebGLTexture
    ): MultiOutput =
      new MultiOutput(frameBuffer, albedo, emissive, normal, specular)
  }

  final class SingleOutput(val frameBuffer: WebGLFramebuffer, val diffuse: WebGLTexture) extends FrameBufferComponents {
    val colorAttachments: scalajs.js.Array[Int] =
      scalajs.js.Array[Int](ColorAttachments.COLOR_ATTACHMENT0)
  }
  object SingleOutput {
    def apply(frameBuffer: WebGLFramebuffer, diffuse: WebGLTexture): SingleOutput =
      new SingleOutput(frameBuffer, diffuse)
  }

}
