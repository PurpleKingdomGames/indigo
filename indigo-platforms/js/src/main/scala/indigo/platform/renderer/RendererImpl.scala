package indigo.platform.renderer

import indigo.shared.datatypes.AmbientLight
import indigo.shared.metrics._
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.shared.display.{DisplayObject, Displayable}
import indigo.shared.EqualTo._
import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.datatypes.Matrix4
import scala.scalajs.js.typedarray.Float32Array

final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends Renderer {

  import RendererFunctions._
  import indigo.platform.shaders._

  private val gl: WebGLRenderingContext =
    cNc.context

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, organiseImage(gl, li.data))
    }

  val vertices: scalajs.js.Array[Double] = {
    val xd: Double = -0.5d
    val yd: Double = -0.5d
    val zd: Double = 1.0d
    val wd: Double = 1.0d
    val hd: Double = 1.0d

    scalajs.js.Array[Double](
      xd,
      yd,
      zd,
      xd,
      hd + yd,
      zd,
      wd + xd,
      yd,
      zd,
      xd,
      hd + yd,
      zd,
      wd + xd,
      yd,
      zd,
      wd + xd,
      hd + yd,
      zd
    )
  }

  private val vertexCount: Int = vertices.length / 3

  private val vertexBuffer: WebGLBuffer  = gl.createBuffer()
  private val textureBuffer: WebGLBuffer = gl.createBuffer()

  private val standardShaderProgram = shaderProgramSetup(gl, "Pixel", StandardPixelArtVert.shader, StandardPixelArtFrag.shader)
  private val lightingShaderProgram = shaderProgramSetup(gl, "Lighting", StandardLightingPixelArtVert.shader, StandardLightingPixelArtFrag.shader)
  private val mergeShaderProgram    = shaderProgramSetup(gl, "Merge", StandardMergeVert.shader, StandardMergeFrag.shader)

  private val gameFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, cNc.width.toDouble, cNc.height.toDouble)
    gl.enable(BLEND)
    gl.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)

    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)
  }

  def drawScene(displayable: Displayable, metrics: Metrics): Unit = {

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight, cNc.magnification)

    metrics.record(DrawGameLayerStartMetric)
    drawLayer(displayable.game, Some(gameFrameBuffer), config.clearColor, standardShaderProgram, CurrentDrawLayer.Game, metrics)
    metrics.record(DrawGameLayerEndMetric)

    metrics.record(DrawLightingLayerStartMetric)
    drawLayer(
      displayable.lighting,
      Some(lightingFrameBuffer),
      AmbientLight.toClearColor(displayable.ambientLight),
      lightingShaderProgram,
      CurrentDrawLayer.Lighting,
      metrics
    )
    metrics.record(DrawLightingLayerEndMetric)

    metrics.record(DrawUiLayerStartMetric)
    drawLayer(displayable.ui, Some(uiFrameBuffer), ClearColor.Black.forceTransparent, standardShaderProgram, CurrentDrawLayer.UI, metrics)
    metrics.record(DrawUiLayerEndMetric)

    metrics.record(RenderToWindowStartMetric)
    drawLayer(List(screenDisplayObject(cNc.width, cNc.height)), None, config.clearColor, mergeShaderProgram, CurrentDrawLayer.Merge, metrics)
    metrics.record(RenderToWindowEndMetric)
  }

  def drawLayer(
      displayObjects: List[DisplayObject],
      frameBufferComponents: Option[FrameBufferComponents],
      clearColor: ClearColor,
      shaderProgram: WebGLProgram,
      layer: CurrentDrawLayer,
      metrics: Metrics
  ): Unit = {

    frameBufferComponents match {
      case Some(fb) =>
        FrameBufferFunctions.switchToFramebuffer(gl, fb.frameBuffer, clearColor)

      case None =>
        FrameBufferFunctions.switchToCanvas(gl, config.clearColor)
    }

    gl.useProgram(shaderProgram)

    // Projection
    val projectionMatrix: Matrix4 =
      if (layer.isMerge) RendererFunctions.orthographicProjectionMatrixNoMag
      else RendererFunctions.orthographicProjectionMatrix

    gl.uniformMatrix4fv(
      location = gl.getUniformLocation(shaderProgram, "u_projection"),
      transpose = false,
      value = mat4ToJsArray(projectionMatrix)
    )

    // Attribute locations
    val verticesLocation = gl.getAttribLocation(shaderProgram, "a_vertices")
    val texcoordLocation = gl.getAttribLocation(shaderProgram, "a_texcoord")

    // Uniform locations (vertex)
    val translationLocation = gl.getUniformLocation(shaderProgram, "u_translation")
    val rotationLocation    = gl.getUniformLocation(shaderProgram, "u_rotation")
    val scaleLocation       = gl.getUniformLocation(shaderProgram, "u_scale")

    // Uniform locations (fragment)
    val tintLocation    = gl.getUniformLocation(shaderProgram, "u_tint")
    val textureLocation = gl.getUniformLocation(shaderProgram, "u_texture")

    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    bindAttibuteBuffer(gl, verticesLocation, 3)

    // Set once
    gl.uniform1i(textureLocation, 0)

    sortByDepth(displayObjects).foreach { displayObject =>
      metrics.record(layer.metricStart)

      gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
      gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.textureCoordinates(displayObject)), STATIC_DRAW)
      bindAttibuteBuffer(gl, texcoordLocation, 2)

      setupVertexShaderState(gl, displayObject, translationLocation, rotationLocation, scaleLocation)

      layer match {
        case CurrentDrawLayer.Merge =>
          setupMergeFragmentShaderState(gl, mergeShaderProgram, gameFrameBuffer.texture, lightingFrameBuffer.texture, uiFrameBuffer.texture)

        case _ =>
          textureLocations.find(t => t.name === displayObject.imageRef).foreach { textureLookup =>
            setupFragmentShaderState(gl, textureLookup.texture, displayObject, tintLocation)
          }
      }

      gl.drawArrays(TRIANGLES, 0, vertexCount)

      metrics.record(layer.metricDraw)

      metrics.record(layer.metricEnd)
    }

  }

}
