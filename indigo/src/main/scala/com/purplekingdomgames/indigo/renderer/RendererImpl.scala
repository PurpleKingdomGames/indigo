package com.purplekingdomgames.indigo.renderer

import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLTexture}

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

  private val mergeShaderProgram = mergeShaderProgramSetup(cNc.context)

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
    4. New render stage to compose the three textures onto the canvas - done
    5. New shader that accepts three images as input - half done, needs to combine them correctly
    6. Draw one displayObject that fills the screen with the combined texture - done

    Ref:
    https://webglfundamentals.org/webgl/lessons/webgl-2-textures.html
    https://webglfundamentals.org/webgl/lessons/webgl-image-processing-continued.html

     */

    drawLayerToTexture(displayable.game, gameFrameBuffer, (d: DisplayObject) => (d.z, d.imageRef))
    drawLayerToTexture(displayable.lighting, lightingFrameBuffer, (d: DisplayObject) => d.imageRef)
    drawLayerToTexture(displayable.ui, uiFrameBuffer, (d: DisplayObject) => (d.z, d.imageRef))

    renderToCanvas(screenDisplayObject)
  }

  private def drawLayerToTexture[B](displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, sortingFunction: DisplayObject => B)(implicit ord: Ordering[B]): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer)

    // Draw as normal
    drawLayer(displayLayer, sortingFunction)

  }

  private def drawLayer[B](displayLayer: DisplayLayer, sortingFunction: DisplayObject => B)(implicit ord: Ordering[B]): Unit =
    displayLayer.displayObjects.sortBy(sortingFunction).foreach { displayObject =>
      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>
        renderToFrameBuffer(displayObject, textureLookup.texture, cNc.magnification)
      }
    }

  private def renderToFrameBuffer(displayObject: DisplayObject, texture: WebGLTexture, magnification: Int): Unit = {
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

  private def renderToCanvas(displayObject: DisplayObject): Unit = {

    // Switch to canvas
    FrameBufferFunctions.switchToCanvas(cNc, config)

    // Use Program
    cNc.context.useProgram(mergeShaderProgram)

    // Setup attributes
    bindShaderToBuffer(cNc, mergeShaderProgram, vertexBuffer, textureBuffer)

    // Setup Uniforms
    setupVertexShader(cNc, mergeShaderProgram, displayObject, 1)
    setupMergeFragmentShader(cNc.context, mergeShaderProgram, gameFrameBuffer.texture, lightingFrameBuffer.texture, uiFrameBuffer.texture, displayObject)

    // Draw
    cNc.context.drawArrays(Rectangle2D.mode, 0, Rectangle2D.vertexCount)

  }

}
