package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.util.metrics._
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._

import scala.language.implicitConversions

trait IRenderer {
  def init(): Unit
  def drawScene(displayable: Displayable)(implicit metrics: IMetrics): Unit
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

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      TextureLookupResult(li.name, organiseImage(cNc.context, li.data))
    }

  private val shaderProgram = shaderProgramSetup(cNc.context)
  private val lightingShaderProgram = lightingShaderProgramSetup(cNc.context)
  private val mergeShaderProgram = mergeShaderProgramSetup(cNc.context)

  private val gameFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    cNc.context.disable(DEPTH_TEST)
    cNc.context.viewport(0, 0, cNc.width, cNc.height)
    cNc.context.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
    cNc.context.enable(BLEND)
  }

  def drawScene(displayable: Displayable)(implicit metrics: IMetrics): Unit = {

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight)

    metrics.record(DrawGameLayerStartMetric)
    drawLayerToTexture(displayable.game, gameFrameBuffer, config.clearColor, drawBg = false)
    metrics.record(DrawGameLayerEndMetric)

    metrics.record(DrawLightingLayerStartMetric)
    drawLightingLayerToTexture(displayable.lighting, lightingFrameBuffer, displayable.lighting.ambientLight)
    metrics.record(DrawLightingLayerEndMetric)

    metrics.record(DrawUiLayerStartMetric)
    drawLayerToTexture(displayable.ui, uiFrameBuffer, ClearColor.Black.forceTransparent, drawBg = false)
    metrics.record(DrawUiLayerEndMetric)

    metrics.record(RenderToConvasStartMetric)
    renderToCanvas(screenDisplayObject)
    metrics.record(RenderToConvasEndMetric)
  }

  private def drawLightingLayerToTexture[B](displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor)(implicit metrics: IMetrics): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Use Program
    cNc.context.useProgram(lightingShaderProgram)

    // Draw as normal
    displayLayer.displayObjects.foreach { displayObject =>

      val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.vertices)
      val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.textureCoordinates)
      val effectsBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.effectValues)

      // Setup attributes
      bindShaderToBuffer(cNc, lightingShaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>

        // Setup Uniforms
        setupVertexShader(cNc, lightingShaderProgram, displayObject, cNc.magnification)
        setupLightingFragmentShader(cNc.context, lightingShaderProgram, textureLookup.texture, displayObject)

        // Draw
        cNc.context.drawArrays(displayObject.mode, 0, displayObject.vertexCount)
        metrics.record(LightingDrawCallMetric)
      }
    }

  }

  private def drawLayerToTexture[B](displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor, drawBg: Boolean)(implicit metrics: IMetrics): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Use Program
    cNc.context.useProgram(shaderProgram)

    // Draw as normal
    displayLayer.displayObjects.sortBy(d => d.z).foreach { displayObject =>

      val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.vertices)
      val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.textureCoordinates)
      val effectsBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.effectValues)

      // Setup attributes
      bindShaderToBuffer(cNc, shaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>

        // Setup Uniforms
        setupVertexShader(cNc, shaderProgram, displayObject, cNc.magnification)
        setupFragmentShader(cNc.context, shaderProgram, textureLookup.texture, displayObject)

        // Draw
        cNc.context.drawArrays(displayObject.mode, 0, displayObject.vertexCount)
        metrics.record(NormalLayerDrawCallMetric)

      }
    }

  }

  private def renderToCanvas(displayObject: DisplayObject)(implicit metrics: IMetrics): Unit = {

    val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.vertices)
    val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.textureCoordinates)
    val effectsBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.effectValues)

    // Switch to canvas
    FrameBufferFunctions.switchToCanvas(cNc, config.clearColor)

    // Use Program
    cNc.context.useProgram(mergeShaderProgram)

    // Setup attributes
    bindShaderToBuffer(cNc, mergeShaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

    // Setup Uniforms
    setupVertexShader(cNc, mergeShaderProgram, displayObject, 1)
    setupMergeFragmentShader(cNc.context, mergeShaderProgram, gameFrameBuffer.texture, lightingFrameBuffer.texture, uiFrameBuffer.texture, displayObject)

    // Draw
    cNc.context.drawArrays(displayObject.mode, 0, displayObject.vertexCount)
    metrics.record(ToCanvasDrawCallMetric)

  }

}
