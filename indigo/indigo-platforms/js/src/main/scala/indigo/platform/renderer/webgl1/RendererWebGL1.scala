package indigo.platform.renderer.webgl1

import indigo.shared.ClearColor
import indigo.shared.EqualTo._
import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import indigo.shared.datatypes.Matrix4
import indigo.shared.platform.ProcessedSceneData
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.ContextAndCanvas
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents

import org.scalajs.dom.html
import org.scalajs.dom.raw.WebGLRenderingContext
import scala.scalajs.js.typedarray.Float32Array
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.platform.renderer.shared.RendererHelper
import indigo.platform.renderer.shared.WebGLHelper

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
final class RendererWebGL1(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends Renderer {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var lastHeight: Int = 0
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrix: Matrix4 = Matrix4.identity
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixNoMag: Matrix4 = Matrix4.identity
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixJS: scalajs.js.Array[Double] = RendererHelper.mat4ToJsArray(Matrix4.identity)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixNoMagJS: scalajs.js.Array[Double] = RendererHelper.mat4ToJsArray(Matrix4.identity)

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastWidth

  import indigo.platform.shaders._

  private val gl: WebGLRenderingContext =
    cNc.context

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, WebGLHelper.organiseImage(gl, li.data))
    }

  private val layerRenderer: RendererLayerWebGL1 =
    new RendererLayerWebGL1(gl, textureLocations)
  private val mergeRenderer: RendererMergeWebGL1 =
    new RendererMergeWebGL1(gl)

  private val vertexBuffer: WebGLBuffer  = gl.createBuffer()

  private val standardShaderProgram = WebGLHelper.shaderProgramSetup(gl, "Pixel", WebGL1StandardPixelArt)
  private val lightingShaderProgram = WebGLHelper.shaderProgramSetup(gl, "Lighting", WebGL1StandardLightingPixelArt)
  private val mergeShaderProgram    = WebGLHelper.shaderProgramSetup(gl, "Merge", WebGL1StandardMerge)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lightingFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var uiFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)

  def init(): Unit = {

    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)

    // Vertex
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererHelper.verticesAndTextureCoords), STATIC_DRAW)
  }

  def drawScene(sceneData: ProcessedSceneData): Unit = {

    resize(cNc.canvas, cNc.magnification)

    // Game layer
    WebGLHelper.setNormalBlend(gl)
    layerRenderer.drawLayer(
      sceneData.gameLayerDisplayObjects,
      gameFrameBuffer,
      ClearColor.Black.forceTransparent,
      standardShaderProgram,
      RendererHelper.mat4ToJsArray(sceneData.gameProjection)
    )

    // Image based lighting
    WebGLHelper.setLightingBlend(gl)
    layerRenderer.drawLayer(
      sceneData.lightingLayerDisplayObjects,
      lightingFrameBuffer,
      sceneData.clearColor,
      lightingShaderProgram,
      RendererHelper.mat4ToJsArray((sceneData.lightingProjection))
    )

    // UI
    WebGLHelper.setNormalBlend(gl)
    layerRenderer.drawLayer(
      sceneData.uiLayerDisplayObjects,
      uiFrameBuffer,
      ClearColor.Black.forceTransparent,
      standardShaderProgram,
      RendererHelper.mat4ToJsArray((sceneData.uiProjection))
    )

    // Merge
    WebGLHelper.setNormalBlend(gl)
    mergeRenderer.drawLayer(
      config.clearColor,
      lastWidth,
      lastHeight,
      mergeShaderProgram,
      orthographicProjectionMatrixNoMagJS,
      gameFrameBuffer,
      lightingFrameBuffer,
      uiFrameBuffer
    )
  }

  def resize(canvas: html.Canvas, magnification: Int): Unit = {
    val actualWidth  = canvas.width
    val actualHeight = canvas.height

    if (!resizeRun || (lastWidth !== actualWidth) || (lastHeight !== actualHeight)) {
      resizeRun = true
      lastWidth = actualWidth
      lastHeight = actualHeight

      orthographicProjectionMatrix = Matrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification)
      orthographicProjectionMatrixJS = RendererHelper.mat4ToJsArray(orthographicProjectionMatrix)
      orthographicProjectionMatrixNoMagJS = RendererHelper.mat4ToJsArray(Matrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble))

      gameFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      lightingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      uiFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      ()
    }
  }

}
