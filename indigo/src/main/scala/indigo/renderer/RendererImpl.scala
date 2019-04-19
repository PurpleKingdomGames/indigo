package indigo.renderer

import indigo.gameengine.scenegraph.datatypes.AmbientLight
import indigo.runtime.metrics._
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._

import indigo.gameengine.display.{DisplayObject, Displayable, DisplayLayer}

import indigo.shared.EqualTo._

final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends Renderer {

  import RendererFunctions._

  private def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
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
      new TextureLookupResult(li.name, organiseImage(cNc.context, li.data))
    }

  private val vertexBuffer: WebGLBuffer  = createVertexBuffer(cNc.context)
  private val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context)
  private val effectsBuffer: WebGLBuffer = createVertexBuffer(cNc.context)

  private val shaderProgram         = shaderProgramSetup(cNc.context)
  private val lightingShaderProgram = lightingShaderProgramSetup(cNc.context)
  private val mergeShaderProgram    = mergeShaderProgramSetup(cNc.context)

  private val gameFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    cNc.context.disable(DEPTH_TEST)
    cNc.context.viewport(0, 0, cNc.width.toDouble, cNc.height.toDouble)
    cNc.context.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
    cNc.context.enable(BLEND)
  }

  def drawScene(displayable: Displayable, metrics: Metrics): Unit = {

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight, cNc.magnification)

    metrics.record(DrawGameLayerStartMetric)
    drawLayerToTexture(displayable.game, gameFrameBuffer, config.clearColor, metrics)
    metrics.record(DrawGameLayerEndMetric)

    metrics.record(DrawLightingLayerStartMetric)
    drawLightingLayerToTexture(displayable.lighting, lightingFrameBuffer, AmbientLight.toClearColor(displayable.ambientLight), metrics)
    metrics.record(DrawLightingLayerEndMetric)

    metrics.record(DrawUiLayerStartMetric)
    drawLayerToTexture(displayable.ui, uiFrameBuffer, ClearColor.Black.forceTransparent, metrics)
    metrics.record(DrawUiLayerEndMetric)

    metrics.record(RenderToWindowStartMetric)
    renderToWindow(screenDisplayObject(cNc.width, cNc.height), metrics)
    metrics.record(RenderToWindowEndMetric)
  }

  private def drawLightingLayerToTexture(displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor, metrics: Metrics): Unit = {

    /*

    TODO: Fix the light combining approach

    What's happening now is that it's working like a normal draw layer, i.e. each thing just gets added
    on top of the last with no regard for what's underneath it.. which nearly works.

    What we should be doing is reading the texture+effects as we are now, AND also the currently rendered
    framebuffer so far, and combining the values together opaquely so that you get a real combined light
    value. It shouldn't matter whether your combining white with yellow or yellow with white, the result
    should be the same.

    In this model, ordering is not important so we can gain a bit of speed by not sorting the lighting layer
    with `compress` not `sortAndCompress`.

     */

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Use Program
    cNc.context.useProgram(lightingShaderProgram)
    setupVertexShader(cNc, lightingShaderProgram, RendererFunctions.orthographicProjectionMatrix)

    // Draw as normal
    CompressedDisplayObject.sortAndCompress(displayLayer.displayObjects).foreach { displayObject =>
      metrics.record(LightingDrawCallLengthStartMetric)

      bindToBuffer(cNc.context, vertexBuffer, displayObject.vertices)
      bindToBuffer(cNc.context, textureBuffer, displayObject.textureCoordinates)
      bindToBuffer(cNc.context, effectsBuffer, displayObject.effectValues)

      // Setup attributes
      bindShaderToBuffer(cNc, lightingShaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

      textureLocations.find(t => t.name === displayObject.imageRef).foreach { textureLookup =>
        // Setup Uniforms
        setupLightingFragmentShader(cNc.context, lightingShaderProgram, textureLookup.texture, displayObject.imageRef)

        // Draw
        cNc.context.drawArrays(displayObject.mode, 0, displayObject.vertexCount)
        metrics.record(LightingDrawCallMetric)
      }

      metrics.record(LightingDrawCallLengthEndMetric)

    }

  }

  private def drawLayerToTexture(displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor, metrics: Metrics): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Use Program
    cNc.context.useProgram(shaderProgram)
    setupVertexShader(cNc, shaderProgram, RendererFunctions.orthographicProjectionMatrix)

    // Draw as normal
    val compressed = CompressedDisplayObject.sortAndCompress(displayLayer.displayObjects)

    compressed.foreach { displayObject =>
      metrics.record(NormalDrawCallLengthStartMetric)

      bindToBuffer(cNc.context, vertexBuffer, displayObject.vertices)
      bindToBuffer(cNc.context, textureBuffer, displayObject.textureCoordinates)
      bindToBuffer(cNc.context, effectsBuffer, displayObject.effectValues)

      // Setup attributes
      bindShaderToBuffer(cNc, shaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

      textureLocations.find(t => t.name === displayObject.imageRef).foreach { textureLookup =>
        // Setup Uniforms
        setupFragmentShader(cNc.context, shaderProgram, textureLookup.texture, displayObject.imageRef)

        // Draw
        cNc.context.drawArrays(displayObject.mode, 0, displayObject.vertexCount)
        metrics.record(NormalLayerDrawCallMetric)

      }

      metrics.record(NormalDrawCallLengthEndMetric)
    }

  }

  private def renderToWindow(displayObject: DisplayObject, metrics: Metrics): Unit = {

    val compressed = CompressedDisplayObject.compressSingle(displayObject)

    metrics.record(ToWindowDrawCallLengthStartMetric)

    bindToBuffer(cNc.context, vertexBuffer, compressed.vertices)
    bindToBuffer(cNc.context, textureBuffer, compressed.textureCoordinates)
    bindToBuffer(cNc.context, effectsBuffer, compressed.effectValues)

    // Switch to canvas
    FrameBufferFunctions.switchToCanvas(cNc, config.clearColor)

    // Use Program
    cNc.context.useProgram(mergeShaderProgram)
    setupVertexShader(cNc, mergeShaderProgram, RendererFunctions.orthographicProjectionMatrixNoMag)

    // Setup attributes
    bindShaderToBuffer(cNc, mergeShaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

    // Setup Uniforms
    setupMergeFragmentShader(cNc.context, mergeShaderProgram, gameFrameBuffer.texture, lightingFrameBuffer.texture, uiFrameBuffer.texture)

    // Draw
    cNc.context.drawArrays(compressed.mode, 0, compressed.vertexCount)

    metrics.record(ToWindowDrawCallMetric)

    metrics.record(ToWindowDrawCallLengthEndMetric)

  }

}
