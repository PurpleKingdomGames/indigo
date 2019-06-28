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
import indigo.facades.WebGL2RenderingContext
import indigo.platform.shaders._

final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends Renderer {

  private val gl: WebGLRenderingContext =
    cNc.context

  private val gl2: WebGL2RenderingContext =
    gl.asInstanceOf[WebGL2RenderingContext]

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, RendererFunctions.organiseImage(gl, li.data))
    }

  private val vertexBuffer: WebGLBuffer           = gl.createBuffer()
  private val textureBuffer: WebGLBuffer          = gl.createBuffer()
  private val displayObjectUBOBuffer: WebGLBuffer = gl.createBuffer()

  private val standardShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Pixel", StandardPixelArtVert.shader, StandardPixelArtFrag.shader)

  private val lightingShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Lighting", StandardLightingPixelArtVert.shader, StandardLightingPixelArtFrag.shader)

  private val mergeShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Merge", StandardMergeVert.shader, StandardMergeFrag.shader)

  private val gameFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)
    gl.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)

    // Vertex
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.vertices), STATIC_DRAW)

    List(standardShaderProgram, lightingShaderProgram, mergeShaderProgram).foreach { shaderProgram =>
      val verticesLocation = gl.getAttribLocation(shaderProgram, "a_vertices")
      RendererFunctions.bindAttibuteBuffer(gl, verticesLocation, 3)
    }

    // Bind texture coords
    gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.textureCoordinates), STATIC_DRAW)

    List(standardShaderProgram, lightingShaderProgram, mergeShaderProgram).foreach { shaderProgram =>
      val texcoordLocation = gl.getAttribLocation(shaderProgram, "a_texcoord")
      RendererFunctions.bindAttibuteBuffer(gl, texcoordLocation, 2)
    }
  }

  def drawScene(displayable: Displayable, metrics: Metrics): Unit = {
    RendererFunctions.resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight, cNc.magnification)

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
    drawLayer(List(RendererFunctions.screenDisplayObject(cNc.width, cNc.height)), None, config.clearColor, mergeShaderProgram, CurrentDrawLayer.Merge, metrics)
    metrics.record(RenderToWindowEndMetric)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
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
      value = RendererFunctions.mat4ToJsArray(projectionMatrix)
    )

    // Texture attribute and uniform
    val textureLocation = gl.getUniformLocation(shaderProgram, "u_texture")
    gl.uniform1i(textureLocation, 0)

    // Bind UBO buffer
    gl.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(gl2.UNIFORM_BUFFER, 0, displayObjectUBOBuffer, 0, RendererFunctions.displayObjectUBODataSize * Float32Array.BYTES_PER_ELEMENT)

    var lastTextureName: String = ""

    RendererFunctions.sortByDepth(displayObjects).foreach { displayObject =>
      metrics.record(layer.metricStart)

      // Set all the uniforms
      gl.bufferData(
        ARRAY_BUFFER,
        new Float32Array(
          RendererFunctions.makeUBOData(displayObject)
        ),
        STATIC_DRAW
      )

      // If needed, update texture state
      layer match {
        case CurrentDrawLayer.Merge =>
          RendererFunctions.setupMergeFragmentShaderState(
            gl,
            mergeShaderProgram,
            gameFrameBuffer.texture,
            lightingFrameBuffer.texture,
            uiFrameBuffer.texture
          )

        case _ if displayObject.imageRef !== lastTextureName =>
          textureLocations.find(t => t.name === displayObject.imageRef).foreach { textureLookup =>
            gl.bindTexture(TEXTURE_2D, textureLookup.texture)
            lastTextureName = displayObject.imageRef
          }

        case _ =>
          ()
      }

      gl.drawArrays(TRIANGLE_STRIP, 0, 4)

      metrics.record(layer.metricDraw)

      metrics.record(layer.metricEnd)
    }

  }

}
