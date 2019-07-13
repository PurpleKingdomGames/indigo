package indigo.platform.renderer

import indigo.facades.WebGL2RenderingContext
import scala.scalajs.js.typedarray.Float32Array
import indigo.shared.display.DisplayObject
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.metrics.Metrics
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import indigo.shared.EqualTo._
import scala.annotation.tailrec

class RendererLayer(gl2: WebGL2RenderingContext, textureLocations: List[TextureLookupResult]) {

  private val displayObjectUBOBuffer: WebGLBuffer = gl2.createBuffer()
  private val instanceDataBuffer: WebGLBuffer     = gl2.createBuffer()

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.While", "org.wartremover.warts.Null"))
  def drawLayer(
      displayObjects: List[DisplayObject],
      frameBufferComponents: FrameBufferComponents,
      clearColor: ClearColor,
      shaderProgram: WebGLProgram,
      layer: CurrentDrawLayer,
      metrics: Metrics
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor)

    gl2.useProgram(shaderProgram)

    val textureLocation = gl2.getUniformLocation(shaderProgram, "u_texture")
    gl2.uniform1i(textureLocation, 0)

    // Projection
    val projectionMatrix: scalajs.js.Array[Double] =
      RendererFunctions.orthographicProjectionMatrix

    // Bind UBO buffer
    gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(
      gl2.UNIFORM_BUFFER,
      0,
      displayObjectUBOBuffer,
      0,
      RendererFunctions.projectionMatrixUBODataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(projectionMatrix), STATIC_DRAW)

    // Instance attributes
    gl2.bindBuffer(ARRAY_BUFFER, instanceDataBuffer)
    RendererFunctions.bindInstanceAttributes(gl2, 2, List(2, 2, 4, 2, 2, 1, 1, 1))

    val maxBatchSize: Int = 2

    val sorted = RendererFunctions.sortByDepth(displayObjects)

    def drawBufferer(instanceCount: Int, buffer: scalajs.js.Array[Double]): Unit =
      if (instanceCount > 0 && buffer.nonEmpty) {
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffer), STATIC_DRAW)
        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
        metrics.record(layer.metricDraw)
      }

    @tailrec
    def rec(remaining: List[DisplayObject], batchCount: Int, textureName: String, buffer: scalajs.js.Array[Double]): Unit =
      remaining match {
        case Nil =>
          drawBufferer(batchCount, buffer)

        case d :: _ if d.imageRef !== textureName =>
          drawBufferer(batchCount, buffer)
          textureLocations.find(t => t.name === d.imageRef).foreach { textureLookup =>
            gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }
          rec(remaining, 0, d.imageRef, scalajs.js.Array[Double]())

        case _ if batchCount === maxBatchSize =>
          drawBufferer(batchCount, buffer)
          rec(remaining, 0, textureName, scalajs.js.Array[Double]())

        case d :: ds =>
          rec(ds, batchCount + 1, textureName, buffer.concat(RendererFunctions.updateUBOData(d)))
      }

    metrics.record(layer.metricStart)
    rec(sorted, 0, "", scalajs.js.Array[Double]())
    metrics.record(layer.metricEnd)

  }

}
