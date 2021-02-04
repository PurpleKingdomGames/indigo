package indigo.platform.renderer.webgl2

import indigo.shared.datatypes.RGBA
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.platform.renderer.Renderer
import indigo.shared.platform.RendererConfig
import org.scalajs.dom.raw.WebGLRenderingContext
import scala.scalajs.js.typedarray.Float32Array
import indigo.facades.WebGL2RenderingContext
import indigo.shaders._
import indigo.shared.datatypes.mutable.CheapMatrix4
import org.scalajs.dom.html

import indigo.shared.platform.ProcessedSceneData
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.ContextAndCanvas
import indigo.platform.renderer.shared.RendererHelper
import indigo.platform.renderer.shared.WebGLHelper
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.platform.events.GlobalEventStream
import indigo.shared.events.ViewportResize
import indigo.shared.config.GameViewport

import scala.collection.mutable
import indigo.shared.shader.ShaderId
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.shader.Shader
import scalajs.js.JSConverters._
import indigo.shared.time.Seconds

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
final class RendererWebGL2(
    config: RendererConfig,
    loadedTextureAssets: List[LoadedTextureAsset],
    cNc: ContextAndCanvas,
    globalEventStream: GlobalEventStream
) extends Renderer {

  private val gl: WebGLRenderingContext =
    cNc.context

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  private val gl2: WebGL2RenderingContext =
    gl.asInstanceOf[WebGL2RenderingContext]

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, WebGLHelper.organiseImage(gl, li.data))
    }

  // private val mergeRenderer: RendererMerge =
  //   new RendererMerge(gl2)

  // private val lightsRenderer: RendererLights =
  //   new RendererLights(gl2)

  // private val layerRenderer: RendererLayer =
  //   new RendererLayer(gl2, textureLocations, config.maxBatchSize)

  private val vertexAndTextureCoordsBuffer: WebGLBuffer = gl.createBuffer()

  private val vao = gl2.createVertexArray()

  // private val standardShaderProgram =
  //   WebGLHelper.shaderProgramSetup(gl, "Pixel", WebGL2StandardPixelArt)
  // private val lightingShaderProgram =
  //   WebGLHelper.shaderProgramSetup(gl, "Lighting", WebGL2StandardLightingPixelArt)
  // private val distortionShaderProgram =
  // WebGLHelper.shaderProgramSetup(gl, "Lighting", WebGL2StandardDistortionPixelArt)
  private val customShaders: mutable.HashMap[ShaderId, WebGLProgram] =
    new mutable.HashMap()

  // @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  // private var gameFrameBuffer: FrameBufferComponents.MultiOutput =
  //   FrameBufferFunctions.createFrameBufferMulti(gl, cNc.canvas.width, cNc.canvas.height)
  // @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  // private var lightsFrameBuffer: FrameBufferComponents.SingleOutput =
  //   FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  // @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  // private var lightingFrameBuffer: FrameBufferComponents.SingleOutput =
  //   FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  // @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  // private var distortionFrameBuffer: FrameBufferComponents.SingleOutput =
  //   FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  // @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  // private var uiFrameBuffer: FrameBufferComponents.SingleOutput =
  //   FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastHeight: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixJS: scalajs.js.Array[Double] = RendererHelper.mat4ToJsArray(CheapMatrix4.identity)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixNoMagJS: scalajs.js.Array[Float] = RendererHelper.mat4ToJsArray(CheapMatrix4.identity).map(_.toFloat)

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastHeight

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

  def init(shaders: Set[Shader]): Unit = {

    shaders.foreach { shader =>
      if (!customShaders.contains(shader.id))
        customShaders.put(
          shader.id,
          WebGLHelper.shaderProgramSetup(gl, shader.id.value, shader)
        )
    }

    val verticesAndTextureCoords: scalajs.js.Array[Float] = {
      val vert0 = scalajs.js.Array[Float](-0.5f, -0.5f, 0.0f, 1.0f)
      val vert1 = scalajs.js.Array[Float](-0.5f, 0.5f, 0.0f, 0.0f)
      val vert2 = scalajs.js.Array[Float](0.5f, -0.5f, 1.0f, 1.0f)
      val vert3 = scalajs.js.Array[Float](0.5f, 0.5f, 1.0f, 0.0f)

      vert0 ++ vert1 ++ vert2 ++ vert3
    }

    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)

    gl2.bindVertexArray(vao)

    // Vertex
    gl.bindBuffer(ARRAY_BUFFER, vertexAndTextureCoordsBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(verticesAndTextureCoords), STATIC_DRAW)
    gl.enableVertexAttribArray(0)
    gl.vertexAttribPointer(
      indx = 0,
      size = 4,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    gl2.bindVertexArray(null)
  }

  private val layerRenderInstance: LayerRenderer =
    new LayerRenderer(gl2, textureLocations, config.maxBatchSize)
  private val layerMergeRenderInstance: LayerMergeRenderer =
    new LayerMergeRenderer(gl2)

  private val defaultShaderProgram =
    WebGLHelper.shaderProgramSetup(gl, "Default", WebGL2Base)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var layerFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var backFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)

  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {

    gl2.bindVertexArray(vao)

    resize(cNc.canvas, cNc.magnification)

    /*
    Currently the renderers take a lump of stuff and draw it.

    What we need to do is...
    Take a layer's worth of display entities.
    Draw them one at a time to a buffer.
    Then merge the buffer to the canvas
    Repeat.
     */

    // Clear down the back buffer
    FrameBufferFunctions.switchToFramebuffer(gl2, backFrameBuffer.frameBuffer, RGBA.Black.makeTransparent, true)

    sceneData.layers.foreach { layer =>
      // Draw the layer
      WebGLHelper.setNormalBlend(gl)
      layerRenderInstance.drawLayer(
        orthographicProjectionMatrixNoMagJS.map(_.toDouble),
        sceneData.cloneBlankDisplayObjects,
        layer.entities,
        layerFrameBuffer,
        RGBA.Black.makeTransparent,
        defaultShaderProgram,
        customShaders,
        runningTime.value
      )

      val projection =
        layer.magnification match {
          case None =>
            orthographicProjectionMatrixJS.map(_.toFloat)

          case Some(m) =>
            CheapMatrix4.orthographic(cNc.canvas.width.toDouble / m.toDouble, cNc.canvas.height.toDouble / m.toDouble).mat.toJSArray.map(_.toFloat)
        }

      // Merge it onto the back buffer
      WebGLHelper.setNormalBlend(gl)
      layerMergeRenderInstance.merge(
        projection,
        layerFrameBuffer,
        Some(backFrameBuffer),
        lastWidth,
        lastHeight,
        RGBA.Black.makeTransparent
      )
    }

    // transfer the back buffer to the canvas
    WebGLHelper.setNormalBlend(gl)
    layerMergeRenderInstance.merge(
      orthographicProjectionMatrixNoMagJS,
      backFrameBuffer,
      None,
      lastWidth,
      lastHeight,
      config.clearColor
    )

    // // Game layer
    // WebGLHelper.setNormalBlend(gl)
    // layerRenderer.drawLayer(
    //   RendererHelper.mat4ToJsArray(sceneData.gameProjection),
    //   sceneData.cloneBlankDisplayObjects,
    //   sceneData.gameLayerDisplayObjects,
    //   gameFrameBuffer,
    //   RGBA.Black.makeTransparent,
    //   standardShaderProgram,
    //   customShaders
    // )

    // // Dynamic lighting
    // WebGLHelper.setLightsBlend(gl)
    // lightsRenderer.drawLayer(
    //   sceneData.lights,
    //   orthographicProjectionMatrixNoMagJS,
    //   lightsFrameBuffer,
    //   gameFrameBuffer,
    //   cNc.canvas.width,
    //   cNc.canvas.height,
    //   cNc.magnification
    // )

    // // Image based lighting
    // WebGLHelper.setLightingBlend(gl)
    // layerRenderer.drawLayer(
    //   RendererHelper.mat4ToJsArray(sceneData.lightingProjection),
    //   sceneData.cloneBlankDisplayObjects,
    //   sceneData.lightingLayerDisplayObjects,
    //   lightingFrameBuffer,
    //   sceneData.clearColor,
    //   lightingShaderProgram,
    //   customShaders
    // )

    // // Distortion
    // WebGLHelper.setDistortionBlend(gl)
    // layerRenderer.drawLayer(
    //   RendererHelper.mat4ToJsArray(sceneData.lightingProjection),
    //   sceneData.cloneBlankDisplayObjects,
    //   sceneData.distortionLayerDisplayObjects,
    //   distortionFrameBuffer,
    //   RGBA(0.5, 0.5, 1.0, 1.0),
    //   distortionShaderProgram,
    //   customShaders
    // )

    // // UI
    // WebGLHelper.setNormalBlend(gl)
    // layerRenderer.drawLayer(
    //   RendererHelper.mat4ToJsArray(sceneData.uiProjection),
    //   sceneData.cloneBlankDisplayObjects,
    //   sceneData.uiLayerDisplayObjects,
    //   uiFrameBuffer,
    //   RGBA.Black.makeTransparent,
    //   standardShaderProgram,
    //   customShaders
    // )

    // // Merge
    // WebGLHelper.setNormalBlend(gl2)
    // mergeRenderer.drawLayer(
    //   orthographicProjectionMatrixNoMagJS,
    //   gameFrameBuffer,
    //   lightsFrameBuffer,
    //   lightingFrameBuffer,
    //   distortionFrameBuffer,
    //   uiFrameBuffer,
    //   lastWidth,
    //   lastHeight,
    //   config.clearColor,
    //   sceneData.gameLayerColorOverlay,
    //   sceneData.uiLayerColorOverlay,
    //   sceneData.gameLayerTint,
    //   sceneData.lightingLayerTint,
    //   sceneData.uiLayerTint,
    //   sceneData.gameLayerSaturation,
    //   sceneData.lightingLayerSaturation,
    //   sceneData.uiLayerSaturation
    // )

  }

  def resize(canvas: html.Canvas, magnification: Int): Unit = {
    val actualWidth  = canvas.width
    val actualHeight = canvas.height

    if (!resizeRun || (lastWidth != actualWidth) || (lastHeight != actualHeight)) {
      resizeRun = true
      lastWidth = actualWidth
      lastHeight = actualHeight

      orthographicProjectionMatrix = CheapMatrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification)
      orthographicProjectionMatrixJS = RendererHelper.mat4ToJsArray(orthographicProjectionMatrix)
      orthographicProjectionMatrixNoMagJS = RendererHelper.mat4ToJsArray(CheapMatrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble)).map(_.toFloat)

      layerFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      backFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      // gameFrameBuffer = FrameBufferFunctions.createFrameBufferMulti(gl, actualWidth, actualHeight)
      // lightsFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      // lightingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      // distortionFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      // uiFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      globalEventStream.pushGlobalEvent(ViewportResize(GameViewport(actualWidth, actualHeight)))

      ()
    }
  }

}
