package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.gameengine.scenegraph.AmbientLight
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._

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
  private val lightingShaderProgram = lightingShaderProgramSetup(cNc.context)
  private val mergeShaderProgram = mergeShaderProgramSetup(cNc.context)

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
    4. New render stage to compose the three textures onto the canvas - done
    5. New shader that accepts three images as input - half done, needs to combine them correctly
    6. Draw one displayObject that fills the screen with the combined texture - done

    Ref:
    https://webglfundamentals.org/webgl/lessons/webgl-2-textures.html
    https://webglfundamentals.org/webgl/lessons/webgl-image-processing-continued.html

     */

    drawLayerToTexture(displayable.game, gameFrameBuffer, ClearColor(1, 1, 1, 0))
    drawLightingLayerToTexture(displayable.lighting, lightingFrameBuffer, ClearColor(0, 0, 0, 0))
    drawLayerToTexture(displayable.ui, uiFrameBuffer, ClearColor(1, 1, 1, 0))

    renderToCanvas(screenDisplayObject, displayable.lighting.ambientLight)
  }

  private def drawLightingLayerToTexture[B](displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Draw as normal
    displayLayer.displayObjects.sortBy(d => d.imageRef).foreach { displayObject =>
      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>
        // Use Program
        cNc.context.useProgram(lightingShaderProgram)

        // Setup attributes
        bindShaderToBuffer(cNc, lightingShaderProgram, vertexBuffer, textureBuffer)

        // Setup Uniforms
        setupVertexShader(cNc, lightingShaderProgram, displayObject, cNc.magnification)
        setupLightingFragmentShader(cNc.context, lightingShaderProgram, textureLookup.texture, /*lightingFrameBuffer.texture,*/ displayObject)

        // Draw
        cNc.context.drawArrays(Rectangle2D.mode, 0, Rectangle2D.vertexCount)
      }
    }

  }

  private def drawLayerToTexture[B](displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Use Program
    cNc.context.useProgram(shaderProgram)

    // Setup attributes
    bindShaderToBuffer(cNc, shaderProgram, vertexBuffer, textureBuffer)

    // Draw as normal
    displayLayer.displayObjects.sortBy(d => (d.z, d.imageRef)).foreach { displayObject =>
      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>

        // Setup Uniforms
        setupVertexShader(cNc, shaderProgram, displayObject, cNc.magnification)
        setupFragmentShader(cNc.context, shaderProgram, textureLookup.texture, displayObject)

        // Draw
        cNc.context.drawArrays(Rectangle2D.mode, 0, Rectangle2D.vertexCount)

      }
    }

  }

  private def renderToCanvas(displayObject: DisplayObject, ambientLight: AmbientLight): Unit = {

    // Switch to canvas
    FrameBufferFunctions.switchToCanvas(cNc, config.clearColor)

    // Use Program
    cNc.context.useProgram(mergeShaderProgram)

    // Setup attributes
    bindShaderToBuffer(cNc, mergeShaderProgram, vertexBuffer, textureBuffer)

    // Setup Uniforms
    setupVertexShader(cNc, mergeShaderProgram, displayObject, 1)
    setupMergeFragmentShader(cNc.context, mergeShaderProgram, gameFrameBuffer.texture, lightingFrameBuffer.texture, uiFrameBuffer.texture, displayObject, ambientLight)

    // Draw
    cNc.context.drawArrays(Rectangle2D.mode, 0, Rectangle2D.vertexCount)

  }

}
