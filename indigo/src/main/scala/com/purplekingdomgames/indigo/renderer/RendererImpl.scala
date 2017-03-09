package com.purplekingdomgames.indigo.renderer

import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._

trait IRenderer {
  def init(): Unit
  def drawScene(displayable: Displayable): Unit
}

final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends IRenderer {

  import RendererFunctions._

  private val shaderProgram = shaderProgramSetup(cNc.context)
  private val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, Rectangle2D.vertices)
  private val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context, Rectangle2D.textureCoordinates)

  def init(): Unit = {
    cNc.context.disable(DEPTH_TEST)
    cNc.context.viewport(0, 0, cNc.width, cNc.height)
    cNc.context.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
    cNc.context.enable(BLEND)
  }

  def drawScene(displayable: Displayable): Unit = {
    cNc.context.clear(COLOR_BUFFER_BIT)
    cNc.context.clearColor(config.clearColor.r, config.clearColor.g, config.clearColor.b, config.clearColor.a)

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight)

    /*
    How to make this work, I think
    ------------------------------
    1. Change logic below to render each layer in order
    2. Setup three fixed frame buffers
    3. Switch to frame buffer before drawing each layer
    4. Add a new step to compose the three framebuffers together on the canvas

     */

    drawLayer(displayable.game)
    drawLayer(displayable.lighting)
    drawLayer(displayable.ui)

  }

  private def drawLayer(displayLayer: DisplayLayer): Unit =
    displayLayer.displayObjects.sortBy(d => (d.z, d.imageRef)).foreach { displayObject =>

      textureLocations(cNc, loadedTextureAssets).find(t => t.name == displayObject.imageRef).foreach { textureLookup =>

        // Use Program
        cNc.context.useProgram(shaderProgram)

        // Setup attributes
        bindShaderToBuffer(cNc, shaderProgram, vertexBuffer, textureBuffer)

        // Setup Uniforms
        setupVertexShader(cNc, shaderProgram, displayObject)
        setupFragmentShader(cNc.context, shaderProgram, textureLookup.texture, displayObject)

        // Draw
        cNc.context.drawArrays(Rectangle2D.mode, 0, Rectangle2D.vertexCount)
      }

    }

}
