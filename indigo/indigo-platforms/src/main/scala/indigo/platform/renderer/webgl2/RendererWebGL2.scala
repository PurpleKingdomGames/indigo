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
import indigo.platform.renderer.shared.WebGLHelper
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.platform.events.GlobalEventStream
import indigo.shared.events.ViewportResize
import indigo.shared.config.GameViewport

import scala.collection.mutable
import indigo.shared.shader.ShaderId
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.shader.RawShaderCode
import scalajs.js.JSConverters._
import indigo.shared.time.Seconds
import indigo.shared.scenegraph.Blend
import indigo.shared.scenegraph.BlendFactor

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

  private val vertexAndTextureCoordsBuffer: WebGLBuffer =
    gl.createBuffer()
  private val projectionUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val frameDataUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  private val vao = gl2.createVertexArray()

  private val customShaders: mutable.HashMap[ShaderId, WebGLProgram] =
    new mutable.HashMap()

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastHeight: Int = 0

  // This is the default project, using global magnification
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrix: CheapMatrix4 = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var defaultLayerProjectionMatrixJS: scalajs.js.Array[Float] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixNoMag: Array[Float] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var canvasMergeProjectionMatrixNoMagJS: scalajs.js.Array[Float] = null

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastHeight

  def init(shaders: Set[RawShaderCode]): Unit = {

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
    new LayerRenderer(gl2, textureLocations, config.maxBatchSize, frameDataUBOBuffer, projectionUBOBuffer)
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

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendEq: String = "add"
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendFactors: (BlendFactor, BlendFactor) = (Blend.Normal.src, Blend.Normal.dst)

  def setBlendMode(blend: Blend): Unit = {
    if (blend.op != currentBlendEq) {
      currentBlendEq = blend.op

      blend match {
        case Blend.Add(_, _) =>
          WebGLHelper.setBlendAdd(gl)

        case Blend.Subtract(_, _) =>
          WebGLHelper.setBlendSubtract(gl)

        case Blend.ReverseSubtract(_, _) =>
          WebGLHelper.setBlendReverseSubtract(gl)

        case Blend.Min(_, _) =>
          WebGLHelper.setBlendMin(gl2)

        case Blend.Max(_, _) =>
          WebGLHelper.setBlendMax(gl2)

        case Blend.Lighten(_, _) =>
          WebGLHelper.setBlendLighten(gl2)

        case Blend.Darken(_, _) =>
          WebGLHelper.setBlendDarken(gl2)
      }
    }

    val nextBlendPair = (blend.src, blend.dst)
    if (currentBlendFactors != nextBlendPair) {
      currentBlendFactors = nextBlendPair
      WebGLHelper.setBlendFunc(gl, blend.src, blend.dst)
    }
  }

  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {

    gl2.bindVertexArray(vao)

    resize(cNc.canvas, cNc.magnification)

    WebGLHelper.attachUBOData(gl2, orthographicProjectionMatrixNoMag, projectionUBOBuffer)

    WebGLHelper.attachUBOData(gl2, Array[Float](runningTime.value.toFloat), frameDataUBOBuffer)

    // Clear down the back buffer
    FrameBufferFunctions.switchToFramebuffer(gl2, backFrameBuffer.frameBuffer, RGBA.Black.makeTransparent, true)

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var currentBlend: Blend = Blend.Normal

    sceneData.layers.foreach { layer =>
      // Set the entity blend mode
      if (currentBlend != layer.entityBlend) {
        currentBlend = layer.entityBlend
        setBlendMode(currentBlend)
      }

      // Draw the entities onto the layer buffer
      layerRenderInstance.drawLayer(
        sceneData.cloneBlankDisplayObjects,
        layer.entities,
        layerFrameBuffer,
        RGBA.Black.makeTransparent,
        defaultShaderProgram,
        customShaders
      )

      val projection =
        layer.magnification match {
          case None =>
            defaultLayerProjectionMatrixJS

          case Some(m) =>
            CheapMatrix4.orthographic(cNc.canvas.width.toDouble / m.toDouble, cNc.canvas.height.toDouble / m.toDouble).mat.toJSArray.map(_.toFloat)
        }

      // Set the layer blend mode
      if (currentBlend != layer.layerBlend) {
        currentBlend = layer.layerBlend
        setBlendMode(currentBlend)
      }

      // Merge the layer buffer onto the back buffer
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
      canvasMergeProjectionMatrixNoMagJS,
      backFrameBuffer,
      None,
      lastWidth,
      lastHeight,
      config.clearColor
    )

  }

  def resize(canvas: html.Canvas, magnification: Int): Unit = {
    val actualWidth  = canvas.width
    val actualHeight = canvas.height

    if (!resizeRun || (lastWidth != actualWidth) || (lastHeight != actualHeight)) {
      resizeRun = true
      lastWidth = actualWidth
      lastHeight = actualHeight

      orthographicProjectionMatrix = CheapMatrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification)
      defaultLayerProjectionMatrixJS = orthographicProjectionMatrix.mat.map(_.toFloat).toJSArray
      orthographicProjectionMatrixNoMag = CheapMatrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble).mat.map(_.toFloat)
      canvasMergeProjectionMatrixNoMagJS = orthographicProjectionMatrixNoMag.toJSArray

      layerFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      backFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      globalEventStream.pushGlobalEvent(ViewportResize(GameViewport(actualWidth, actualHeight)))

      ()
    }
  }

}
