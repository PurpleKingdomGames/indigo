package indigo.platform.renderer.webgl2

import indigo.facades.WebGL2RenderingContext
import indigo.platform.assets.DynamicText
import indigo.platform.events.GlobalEventStream
import indigo.platform.renderer.Renderer
import indigo.platform.renderer.shared.CameraHelper
import indigo.platform.renderer.shared.ContextAndCanvas
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.WebGLHelper
import indigo.shared.QuickCache
import indigo.shared.config.GameViewport
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.events.ViewportResize
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.platform.RendererConfig
import indigo.shared.scenegraph.Blend
import indigo.shared.scenegraph.BlendFactor
import indigo.shared.scenegraph.Camera
import indigo.shared.shader.RawShaderCode
import indigo.shared.shader.ShaderId
import indigo.shared.shader.StandardShaders
import indigo.shared.time.Seconds
import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext._
import org.scalajs.dom.html
import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLFramebuffer
import org.scalajs.dom.raw.WebGLProgram

import scala.scalajs.js.Dynamic
import scala.scalajs.js.typedarray.Float32Array

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
final class RendererWebGL2(
    config: RendererConfig,
    loadedTextureAssets: scalajs.js.Array[LoadedTextureAsset],
    cNc: ContextAndCanvas,
    globalEventStream: GlobalEventStream,
    dynamicText: DynamicText
) extends Renderer {

  val renderingTechnology: RenderingTechnology = RenderingTechnology.WebGL2

  implicit private val projectionsCache: QuickCache[scalajs.js.Array[Float]] = QuickCache.empty

  private val gl: WebGLRenderingContext =
    cNc.context

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  private val gl2: WebGL2RenderingContext =
    gl.asInstanceOf[WebGL2RenderingContext]

  private val textureLocations: scalajs.js.Array[TextureLookupResult] =
    gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1);
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, WebGLHelper.organiseImage(gl, li.data))
    }

  private val vertexAndTextureCoordsBuffer: WebGLBuffer =
    gl.createBuffer()
  private val projectionUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val frameDataUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val cloneReferenceUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val lightDataUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  private val vao = gl2.createVertexArray()

  private val customShaders: scalajs.js.Dictionary[WebGLProgram] =
    scalajs.js.Dictionary.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastHeight: Int = 0

  // This is the default project, using global magnification
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var defaultLayerProjectionMatrix: scalajs.js.Array[Float] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixNoMag: scalajs.js.Array[Float] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrixNoMagFlipped: scalajs.js.Array[Float] = null

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastHeight

  private val layerRenderInstance: LayerRenderer =
    new LayerRenderer(
      gl2,
      textureLocations,
      config.maxBatchSize,
      projectionUBOBuffer,
      frameDataUBOBuffer,
      cloneReferenceUBOBuffer,
      lightDataUBOBuffer,
      dynamicText,
      WebGLHelper.createAndBindTexture(gl2)
    ).init()
  private val layerMergeRenderInstance: LayerMergeRenderer =
    new LayerMergeRenderer(gl2, frameDataUBOBuffer)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var layerEntityFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var scalingFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var greenDstFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var blueDstFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var emptyFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var greenIsTarget: Boolean = true

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendEq: String = "add"
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentBlendFactors: (BlendFactor, BlendFactor) = (Blend.Normal.src, Blend.Normal.dst)

  private given CanEqual[(BlendFactor, BlendFactor), (BlendFactor, BlendFactor)] = CanEqual.derived

  def init(shaders: Set[RawShaderCode]): Unit = {

    shaders.foreach { shader =>
      if (!customShaders.contains(shader.id.toString))
        customShaders.put(
          shader.id.toString,
          WebGLHelper.shaderProgramSetup(gl, shader.id.toString, shader)
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

  private given CanEqual[Option[Int], Option[Int]] = CanEqual.derived

  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {

    gl2.bindVertexArray(vao)

    resize(cNc.canvas, cNc.magnification)

    val frameData = scalajs.js.Array[Float](runningTime.toFloat, 0.0f, lastWidth.toFloat, lastHeight.toFloat)
    WebGLHelper.attachUBOData(gl2, frameData, frameDataUBOBuffer)

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var currentBlend: Blend = Blend.Normal

    sceneData.layers.foreach { layer =>
      WebGLHelper.attachUBOData(gl2, layer.lightsData, lightDataUBOBuffer)

      val layerProjection: scalajs.js.Array[Float] =
        layer.camera.orElse(sceneData.camera) match
          case None =>
            orthographicProjectionMatrixNoMag

          case Some(c) =>
            CameraHelper
              .calculateCameraMatrix(
                lastWidth.toDouble,
                lastHeight.toDouble,
                1.0d, // Layers aren't magnified
                c.position.x.toDouble,
                c.position.y.toDouble,
                c.zoom.toDouble,
                false, // layers aren't flipped
                c.rotation,
                c.isLookAt
              )
              .toArray

      WebGLHelper.attachUBOData(gl2, layerProjection, projectionUBOBuffer)

      // Set the entity blend mode
      if (currentBlend != layer.entityBlend) {
        currentBlend = layer.entityBlend
        setBlendMode(currentBlend)
      }

      // Draw the entities onto the layer buffer
      layerRenderInstance.drawLayer(
        sceneData.cloneBlankDisplayObjects,
        layer.entities,
        layerEntityFrameBuffer,
        layer.bgColor,
        customShaders
      )

      val projection =
        layer.magnification match
          case None =>
            defaultLayerProjectionMatrix

          case Some(m) =>
            QuickCache(m.toString + lastWidth.toString + lastHeight.toString) {
              CameraHelper
                .calculateCameraMatrix(
                  lastWidth.toDouble,
                  lastHeight.toDouble,
                  m.toDouble,
                  0,
                  0,
                  1,
                  true,
                  Radians.zero,
                  false
                )
                .toArray
            }

      // Clear the blend mode
      if (currentBlend != Blend.Normal) {
        currentBlend = Blend.Normal
        setBlendMode(currentBlend)
      }

      // Merge the layer buffer onto the staging buffer, this clears the magnification
      layerMergeRenderInstance.merge(
        projection,
        layerEntityFrameBuffer,
        emptyFrameBuffer,
        Some(scalingFrameBuffer),
        lastWidth,
        lastHeight,
        RGBA.Black.makeTransparent,
        false,
        customShaders,
        StandardShaders.NormalBlend.id,
        scalajs.js.Array()
      )

      // Set the layer blend mode
      if (currentBlend != layer.layerBlend) {
        currentBlend = layer.layerBlend
        setBlendMode(currentBlend)
      }

      // Flip which buffer is the target.
      if (greenIsTarget) {
        greenIsTarget = false
        blitBuffers(blueDstFrameBuffer.frameBuffer, greenDstFrameBuffer.frameBuffer)
      } else {
        greenIsTarget = true
        blitBuffers(greenDstFrameBuffer.frameBuffer, blueDstFrameBuffer.frameBuffer)
      }

      // Merge the layer buffer onto the back buffer
      layerMergeRenderInstance.merge(
        orthographicProjectionMatrixNoMag,
        scalingFrameBuffer,
        if (!greenIsTarget) blueDstFrameBuffer
        else greenDstFrameBuffer, // Inverted condition, because by now it's flipped.
        None,
        lastWidth,
        lastHeight,
        RGBA.Black.makeTransparent,
        false,
        customShaders,
        layer.shaderId,
        layer.shaderUniformData
      )
    }

    // transfer the back buffer to the canvas
    WebGLHelper.setNormalBlend(gl)
    layerMergeRenderInstance.merge(
      orthographicProjectionMatrixNoMagFlipped,
      if (!greenIsTarget) greenDstFrameBuffer else blueDstFrameBuffer, // Inverted condition, because outside the loop.
      emptyFrameBuffer,                                                // just giving it something to use...
      None,
      lastWidth,
      lastHeight,
      config.clearColor,
      true,
      customShaders,
      sceneData.shaderId,
      sceneData.shaderUniformData
    )

    clearBuffer(blueDstFrameBuffer.frameBuffer)
    clearBuffer(greenDstFrameBuffer.frameBuffer)
    clearBuffer(emptyFrameBuffer.frameBuffer)
  }

  def blitBuffers(from: WebGLFramebuffer, to: WebGLFramebuffer): Unit = {

    gl2.clearColor(0, 0, 0, 0)

    // Bind and clear 'to'
    gl2.bindFramebuffer(FRAMEBUFFER, to)
    gl2.clear(COLOR_BUFFER_BIT)

    // Blit 'from' to 'to'
    gl2.bindFramebuffer(WebGL2RenderingContext.READ_FRAMEBUFFER, from)
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, to)
    gl2.blitFramebuffer(0, lastHeight, lastWidth, 0, 0, lastHeight, lastWidth, 0, COLOR_BUFFER_BIT, NEAREST)
    gl2.bindFramebuffer(WebGL2RenderingContext.READ_FRAMEBUFFER, null)
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, null)

    gl2.bindFramebuffer(FRAMEBUFFER, to)
  }

  def clearBuffer(buffer: WebGLFramebuffer): Unit = {
    gl2.bindFramebuffer(WebGL2RenderingContext.DRAW_FRAMEBUFFER, buffer)
    gl2.clear(COLOR_BUFFER_BIT)
  }

  def resize(canvas: html.Canvas, magnification: Int): Unit = {
    val actualWidth  = canvas.width
    val actualHeight = canvas.height

    if (!resizeRun || (lastWidth != actualWidth) || (lastHeight != actualHeight)) {
      resizeRun = true
      lastWidth = actualWidth
      lastHeight = actualHeight

      orthographicProjectionMatrix =
        CheapMatrix4.orthographic(actualWidth.toFloat / magnification, actualHeight.toFloat / magnification)
      defaultLayerProjectionMatrix = orthographicProjectionMatrix.scale(1.0, -1.0, 1.0).toArray
      orthographicProjectionMatrixNoMag = CheapMatrix4.orthographic(actualWidth.toFloat, actualHeight.toFloat).toArray
      orthographicProjectionMatrixNoMagFlipped =
        CheapMatrix4.orthographic(actualWidth.toFloat, actualHeight.toFloat).scale(1.0, -1.0, 1.0).toArray

      layerEntityFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      scalingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      greenDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      blueDstFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      emptyFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      globalEventStream.pushGlobalEvent(ViewportResize(GameViewport(actualWidth, actualHeight)))

      ()
    }
  }

}
