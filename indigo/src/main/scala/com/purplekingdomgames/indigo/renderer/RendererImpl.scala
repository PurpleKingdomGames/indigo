package com.purplekingdomgames.indigo.renderer

import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLFramebuffer, WebGLTexture}

trait IRenderer {
  def init(): Unit
  def drawScene(displayable: Displayable): Unit
}

final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends IRenderer {

  import RendererFunctions._

  private val screenDisplayObject: DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = cNc.width,
      height = cNc.height,
      imageRef = "",
      alpha = 1,
      tintR = 1,
      tintG = 1,
      tintB = 1,
      flipHorizontal = false,
      flipVertical = true,
      frame = SpriteSheetFrame.defaultOffset
    )

  private val textureLocations: List[TextureLookup] =
    loadedTextureAssets.map { li =>
      TextureLookup(li.name, organiseImage(cNc.context, li.data))
    }

  private val shaderProgram = shaderProgramSetup(cNc.context)
  private val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, Rectangle2D.vertices)
  private val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context, Rectangle2D.textureCoordinates)

  private val gameFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    cNc.context.disable(DEPTH_TEST)
    cNc.context.viewport(0, 0, cNc.width, cNc.height)
    cNc.context.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
    cNc.context.enable(BLEND)
  }

  def drawScene(displayable: Displayable): Unit = {

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight)

    /*
    How to make this work, I think
    ------------------------------

    Initially:
     Just try and get the diffuse layer to render to a framebuffer, then to the canvas.

    Final todo list
    1. Change logic below to render each layer in order - done
    2. Setup three fixed frame buffers - done
    3. Switch to frame buffer before drawing each layer - done
    4. New render stage to compose the three textures onto the canvas
    5. New shader that accepts three images as input
    6. Draw one displayObject that fills the screen with the combined texture

    Ref:
    https://webglfundamentals.org/webgl/lessons/webgl-2-textures.html
    https://webglfundamentals.org/webgl/lessons/webgl-image-processing-continued.html

     */

    drawLayerToTexture(displayable.game, gameFrameBuffer)
    drawLayerToTexture(displayable.lighting, lightingFrameBuffer)
    drawLayerToTexture(displayable.ui, uiFrameBuffer)

    // Switch to canvas
    FrameBufferFunctions.switchToCanvas(cNc, config)

    // Render to canvas
    // TODO: This doesn't work, and it doesn't matter because it's not the right idea anyway! :-P
    render(screenDisplayObject, gameFrameBuffer.texture, 1)
    render(screenDisplayObject, lightingFrameBuffer.texture, 1)
    render(screenDisplayObject, uiFrameBuffer.texture, 1)

  }

  private def drawLayerToTexture(displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer)

    // Draw as normal
    drawLayer(displayLayer)

  }

  private def drawLayer(displayLayer: DisplayLayer): Unit =
    displayLayer.displayObjects.sortBy(d => (d.z, d.imageRef)).foreach { displayObject =>
      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>
        render(displayObject, textureLookup.texture, cNc.magnification)
      }
    }

  private def render(displayObject: DisplayObject, texture: WebGLTexture, magnification: Int): Unit = {
    // Use Program
    cNc.context.useProgram(shaderProgram)

    // Setup attributes
    bindShaderToBuffer(cNc, shaderProgram, vertexBuffer, textureBuffer)

    // Setup Uniforms
    setupVertexShader(cNc, shaderProgram, displayObject, magnification)
    setupFragmentShader(cNc.context, shaderProgram, texture, displayObject)

    // Draw
    cNc.context.drawArrays(Rectangle2D.mode, 0, Rectangle2D.vertexCount)
  }

}

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
