package indigo.platform.renderer.webgl1

import indigo.platform.renderer.Renderer
import indigo.platform.renderer.shared.WebGLHelper
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.ContextAndCanvas
import indigo.platform.events.GlobalEventStream

import indigo.shared.platform.RendererConfig
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayObject
import indigo.shared.events.ViewportResize
import indigo.shared.config.GameViewport
import indigo.shared.shader.RawShaderCode
import indigo.shared.time.Seconds

import scala.scalajs.js.typedarray.Float32Array
import org.scalajs.dom.html
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLProgram
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLUniformLocation

import scalajs.js.JSConverters._

import scala.collection.mutable

final class RendererWebGL1(
    config: RendererConfig,
    loadedTextureAssets: List[LoadedTextureAsset],
    cNc: ContextAndCanvas,
    globalEventStream: GlobalEventStream
) extends Renderer {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastHeight: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastWidth

  private val gl: WebGLRenderingContext = cNc.context
  private val vertexBuffer: WebGLBuffer = gl.createBuffer()
  private val standardShaderProgram     = WebGLHelper.shaderProgramSetup(gl, "Pixel", indigo.shaders.WebGL1)

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map(li => new TextureLookupResult(li.name, WebGLHelper.organiseImage(gl, li.data)))

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def init(shaders: Set[RawShaderCode]): Unit = {
    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)
    gl.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)

    val verticesAndTextureCoords: scalajs.js.Array[Float] = {
      val vert0 = scalajs.js.Array[Float](-0.5f, -0.5f, 0.0f, 1.0f)
      val vert1 = scalajs.js.Array[Float](-0.5f, 0.5f, 0.0f, 0.0f)
      val vert2 = scalajs.js.Array[Float](0.5f, -0.5f, 1.0f, 1.0f)
      val vert3 = scalajs.js.Array[Float](0.5f, 0.5f, 1.0f, 0.0f)

      vert0 ++ vert1 ++ vert2 ++ vert3
    }

    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(verticesAndTextureCoords), STATIC_DRAW)

    gl.useProgram(standardShaderProgram)

    val verticesLocation1 = gl.getAttribLocation(standardShaderProgram, "a_verticesAndCoords")
    bindAttibuteBuffer(gl, verticesLocation1, 4)

    gl.bindBuffer(ARRAY_BUFFER, null)

    gl.bindFramebuffer(FRAMEBUFFER, null)
  }

  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {
    resize(cNc.canvas, cNc.magnification)

    gl.clearColor(config.clearColor.r, config.clearColor.g, config.clearColor.b, config.clearColor.a)
    gl.clear(COLOR_BUFFER_BIT)

    val gameProjection = orthographicProjectionMatrix.mat.toJSArray

    sceneData.layers.foreach { layer =>
      val projection =
        layer.magnification match {
          case None =>
            gameProjection

          case Some(m) =>
            CheapMatrix4.orthographic(cNc.canvas.width.toDouble / m.toDouble, cNc.canvas.height.toDouble / m.toDouble).mat.toJSArray
        }

      drawLayer(layer.entities, standardShaderProgram, projection)
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def drawLayer(
      displayEntities: mutable.ListBuffer[DisplayEntity],
      shaderProgram: WebGLProgram,
      projectionMatrix: scalajs.js.Array[Double]
  ): Unit = {

    gl.uniformMatrix4fv(
      location = gl.getUniformLocation(shaderProgram, "u_projection"),
      transpose = false,
      value = projectionMatrix
    )

    gl.uniform1i(gl.getUniformLocation(shaderProgram, "u_texture"), 0)

    // This basic renderer only renders entities with images. So we can save work by only considering
    // 1) display objects, and that 2) have an atlas reference.
    displayEntities
      .collect { case d: DisplayObject if d.atlasName.isDefined => (d, d.atlasName.get) }
      .sortWith((d1, d2) => d1._1.z > d2._1.z)
      .foreach {
        case (displayObject: DisplayObject, objectAtlas) =>
          setupVertexShaderState(
            gl,
            displayObject,
            gl.getUniformLocation(shaderProgram, "u_transform"),
            gl.getUniformLocation(shaderProgram, "u_frameTransform")
          )

          textureLocations.find(t => t.name == objectAtlas) match {
            case None =>
              gl.activeTexture(TEXTURE0);
              gl.bindTexture(TEXTURE_2D, null)

            case Some(textureLookup) =>
              gl.activeTexture(TEXTURE0);
              gl.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          gl.drawArrays(TRIANGLE_STRIP, 0, 4)

        case _ =>
          ()
      }

  }

  def resize(canvas: html.Canvas, magnification: Int): Unit = {
    val actualWidth  = canvas.width
    val actualHeight = canvas.height

    if (!resizeRun || (lastWidth != actualWidth) || (lastHeight != actualHeight)) {
      resizeRun = true
      lastWidth = actualWidth
      lastHeight = actualHeight
      orthographicProjectionMatrix = CheapMatrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      globalEventStream.pushGlobalEvent(ViewportResize(GameViewport(actualWidth, actualHeight)))

      ()
    }
  }

  def bindAttibuteBuffer(gl: raw.WebGLRenderingContext, attributeLocation: Int, size: Int): Unit = {
    gl.enableVertexAttribArray(attributeLocation)
    gl.vertexAttribPointer(
      indx = attributeLocation,
      size = size,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
  }

  def setupVertexShaderState(
      gl: raw.WebGLRenderingContext,
      displayObject: DisplayObject,
      transformMatrixLocation: WebGLUniformLocation,
      frameTransform: WebGLUniformLocation
  ): Unit = {

    gl.uniformMatrix4fv(
      location = transformMatrixLocation,
      transpose = false,
      value = displayObject.transform.mat.toJSArray
    )

    gl.uniform4f(
      frameTransform,
      displayObject.channelOffset0X.toDouble,
      displayObject.channelOffset0Y.toDouble,
      displayObject.frameScaleX.toDouble,
      displayObject.frameScaleY.toDouble
    )
  }

}
