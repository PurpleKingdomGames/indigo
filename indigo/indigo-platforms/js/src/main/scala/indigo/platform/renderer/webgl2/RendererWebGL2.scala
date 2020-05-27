package indigo.platform.renderer.webgl2

import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import org.scalajs.dom.raw.WebGLRenderingContext
import scala.scalajs.js.typedarray.Float32Array
import indigo.facades.WebGL2RenderingContext
import indigo.platform.shaders._
import indigo.shared.datatypes.Matrix4
import org.scalajs.dom.html
import indigo.shared.EqualTo._
import indigo.shared.platform.ProcessedSceneData
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.TextureLookupResult

@SuppressWarnings(Array("org.wartremover.warts.Null"))
final class RendererWebGL2(
    config: RendererConfig,
    loadedTextureAssets: List[LoadedTextureAsset],
    cNc: ContextAndCanvas
) extends Renderer {

  private val gl: WebGLRenderingContext =
    cNc.context

  private val gl2: WebGL2RenderingContext =
    gl.asInstanceOf[WebGL2RenderingContext]

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, RendererFunctions.organiseImage(gl, li.data))
    }

  private val mergeRenderer: RendererMerge =
    new RendererMerge(gl2)

  private val lightsRenderer: RendererLights =
    new RendererLights(gl2)

  private val layerRenderer: RendererLayer =
    new RendererLayer(gl2, textureLocations, config.maxBatchSize)

  private val vertexAndTextureCoordsBuffer: WebGLBuffer = gl.createBuffer()

  private val vao = gl2.createVertexArray()

  private val standardShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Pixel", WebGL2StandardPixelArt)
  private val lightingShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Lighting", WebGL2StandardLightingPixelArt)
  private val distortionShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Lighting", WebGL2StandardDistortionPixelArt)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameFrameBuffer: FrameBufferComponents.MultiOutput =
    FrameBufferFunctions.createFrameBufferMulti(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lightsFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lightingFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var distortionFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var uiFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var lastHeight: Int = 0
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixJS: scalajs.js.Array[Double] = RendererFunctions.mat4ToJsArray(Matrix4.identity)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixNoMagJS: scalajs.js.Array[Float] = RendererFunctions.mat4ToJsArray(Matrix4.identity).map(_.toFloat)

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastWidth
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrix: Matrix4 = Matrix4.identity

  def init(): Unit = {

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

  def drawScene(sceneData: ProcessedSceneData): Unit = {

    gl2.bindVertexArray(vao)

    resize(cNc.canvas, cNc.magnification)

    // Game layer
    RendererFunctions.setNormalBlend(gl)
    layerRenderer.drawLayer(
      RendererFunctions.mat4ToJsArray(sceneData.gameProjection),
      sceneData.cloneBlankDisplayObjects,
      sceneData.gameLayerDisplayObjects,
      gameFrameBuffer,
      ClearColor.Black.forceTransparent,
      standardShaderProgram
    )

    // Dynamic lighting
    RendererFunctions.setLightsBlend(gl)
    lightsRenderer.drawLayer(
      sceneData.lights,
      orthographicProjectionMatrixNoMagJS,
      lightsFrameBuffer,
      gameFrameBuffer,
      cNc.canvas.width,
      cNc.canvas.height,
      cNc.magnification
    )

    // Image based lighting
    RendererFunctions.setLightingBlend(gl)
    layerRenderer.drawLayer(
      RendererFunctions.mat4ToJsArray(sceneData.lightingProjection),
      sceneData.cloneBlankDisplayObjects,
      sceneData.lightingLayerDisplayObjects,
      lightingFrameBuffer,
      sceneData.clearColor,
      lightingShaderProgram
    )

    // Distortion
    RendererFunctions.setDistortionBlend(gl)
    layerRenderer.drawLayer(
      RendererFunctions.mat4ToJsArray(sceneData.lightingProjection),
      sceneData.cloneBlankDisplayObjects,
      sceneData.distortionLayerDisplayObjects,
      distortionFrameBuffer,
      ClearColor(0.5, 0.5, 1.0, 1.0),
      distortionShaderProgram
    )

    // UI
    RendererFunctions.setNormalBlend(gl)
    layerRenderer.drawLayer(
      RendererFunctions.mat4ToJsArray(sceneData.uiProjection),
      sceneData.cloneBlankDisplayObjects,
      sceneData.uiLayerDisplayObjects,
      uiFrameBuffer,
      ClearColor.Black.forceTransparent,
      standardShaderProgram
    )

    // Merge
    RendererFunctions.setNormalBlend(gl2)
    mergeRenderer.drawLayer(
      orthographicProjectionMatrixNoMagJS,
      gameFrameBuffer,
      lightsFrameBuffer,
      lightingFrameBuffer,
      distortionFrameBuffer,
      uiFrameBuffer,
      cNc.canvas.width,
      cNc.canvas.height,
      config.clearColor,
      sceneData.gameLayerColorOverlay,
      sceneData.uiLayerColorOverlay,
      sceneData.gameLayerTint,
      sceneData.lightingLayerTint,
      sceneData.uiLayerTint,
      sceneData.gameLayerSaturation,
      sceneData.lightingLayerSaturation,
      sceneData.uiLayerSaturation
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
      orthographicProjectionMatrixJS = RendererFunctions.mat4ToJsArray(orthographicProjectionMatrix)
      orthographicProjectionMatrixNoMagJS = RendererFunctions.mat4ToJsArray(Matrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble)).map(_.toFloat)

      gameFrameBuffer = FrameBufferFunctions.createFrameBufferMulti(gl, actualWidth, actualHeight)
      lightsFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      lightingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      distortionFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      uiFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      ()
    }
  }

}
