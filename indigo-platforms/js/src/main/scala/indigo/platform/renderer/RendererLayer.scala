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
import scala.scalajs.js.typedarray.Float32Array

class RendererLayer(gl2: WebGL2RenderingContext, textureLocations: List[TextureLookupResult]) {

  private val buffer2: WebGLBuffer = gl2.createBuffer()
  private val buffer3: WebGLBuffer = gl2.createBuffer()
  private val buffer4: WebGLBuffer = gl2.createBuffer()
  private val buffer5: WebGLBuffer = gl2.createBuffer()
  private val buffer6: WebGLBuffer = gl2.createBuffer()
  private val buffer7: WebGLBuffer = gl2.createBuffer()
  private val buffer8: WebGLBuffer = gl2.createBuffer()
  private val buffer9: WebGLBuffer = gl2.createBuffer()

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.Var",
      "org.wartremover.warts.NonUnitStatements",
      "org.wartremover.warts.While",
      "org.wartremover.warts.Null",
      "org.wartremover.warts.TraversableOps"
    )
  )
  def drawLayer(
      displayObjects: List[DisplayObject],
      frameBufferComponents: FrameBufferComponents,
      clearColor: ClearColor,
      maxBatchSize: Int,
      shaderProgram: WebGLProgram,
      layer: CurrentDrawLayer,
      metrics: Metrics
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor)

    gl2.useProgram(shaderProgram)

    val textureLocation = gl2.getUniformLocation(shaderProgram, "u_texture")
    gl2.uniform1i(textureLocation, 0)

    // Projection
    val projectionLocation = gl2.getUniformLocation(shaderProgram, "u_projection")
    gl2.uniformMatrix4fv(projectionLocation, false, RendererFunctions.orthographicProjectionMatrix)

    // Instance attributes

    // vec2 a_translation
    gl2.bindBuffer(ARRAY_BUFFER, buffer2)
    gl2.enableVertexAttribArray(2)
    gl2.vertexAttribPointer(2, 2, FLOAT, false, 2 * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(2, 1)
    // vec2 a_scale
    gl2.bindBuffer(ARRAY_BUFFER, buffer3)
    gl2.enableVertexAttribArray(3)
    gl2.vertexAttribPointer(3, 2, FLOAT, false, 2 * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(3, 1)
    // vec4 a_tint
    gl2.bindBuffer(ARRAY_BUFFER, buffer4)
    gl2.enableVertexAttribArray(4)
    gl2.vertexAttribPointer(4, 4, FLOAT, false, 4 * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(4, 1)
    // vec2 a_frameTranslation
    gl2.bindBuffer(ARRAY_BUFFER, buffer5)
    gl2.enableVertexAttribArray(5)
    gl2.vertexAttribPointer(5, 2, FLOAT, false, 2 * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(5, 1)
    // vec2 a_frameScale
    gl2.bindBuffer(ARRAY_BUFFER, buffer6)
    gl2.enableVertexAttribArray(6)
    gl2.vertexAttribPointer(6, 2, FLOAT, false, 2 * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(6, 1)
    // float a_rotation
    gl2.bindBuffer(ARRAY_BUFFER, buffer7)
    gl2.enableVertexAttribArray(7)
    gl2.vertexAttribPointer(7, 1, FLOAT, false, Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(7, 1)
    // float a_fliph
    gl2.bindBuffer(ARRAY_BUFFER, buffer8)
    gl2.enableVertexAttribArray(8)
    gl2.vertexAttribPointer(8, 1, FLOAT, false, Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(8, 1)
    // float a_flipv
    gl2.bindBuffer(ARRAY_BUFFER, buffer9)
    gl2.enableVertexAttribArray(9)
    gl2.vertexAttribPointer(9, 1, FLOAT, false, Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(9, 1)
    //
    
    val sorted = RendererFunctions.sortByDepth(displayObjects)

    def drawBufferer(instanceCount: Int, buffers: InstanceBuffers): Unit =
      if (instanceCount > 0) {

        gl2.bindBuffer(ARRAY_BUFFER, buffer2)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b2), STATIC_DRAW)

        gl2.bindBuffer(ARRAY_BUFFER, buffer3)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b3), STATIC_DRAW)

        gl2.bindBuffer(ARRAY_BUFFER, buffer4)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b4), STATIC_DRAW)

        gl2.bindBuffer(ARRAY_BUFFER, buffer5)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b5), STATIC_DRAW)

        gl2.bindBuffer(ARRAY_BUFFER, buffer6)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b6), STATIC_DRAW)

        gl2.bindBuffer(ARRAY_BUFFER, buffer7)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b7), STATIC_DRAW)

        gl2.bindBuffer(ARRAY_BUFFER, buffer8)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b8), STATIC_DRAW)

        gl2.bindBuffer(ARRAY_BUFFER, buffer9)
        gl2.bufferData(ARRAY_BUFFER, new Float32Array(buffers.b9), STATIC_DRAW)

        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
        metrics.record(layer.metricDraw)

      }

    sorted.headOption.foreach { d =>
      textureLocations.find(t => t.name === d.imageRef).foreach { textureLookup =>
        gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
      }
    }

    @tailrec
    def rec(remaining: List[DisplayObject], batchCount: Int, textureName: String, buffers: InstanceBuffers): Unit =
      remaining match {
        case Nil =>
          drawBufferer(batchCount, buffers)

        case d :: _ if d.imageRef !== textureName =>
          drawBufferer(batchCount, buffers)
          textureLocations.find(t => t.name === d.imageRef).foreach { textureLookup =>
            gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }
          rec(remaining, 0, d.imageRef, InstanceBuffers.empty)

        case _ if batchCount === maxBatchSize =>
          drawBufferer(batchCount, buffers)
          rec(remaining, 0, textureName, InstanceBuffers.empty)

        case d :: ds =>
          rec(ds, batchCount + 1, textureName, buffers.add(d))
      }

    metrics.record(layer.metricStart)
    rec(sorted, 0, "", InstanceBuffers.empty)
    metrics.record(layer.metricEnd)

  }

}

final class InstanceBuffers(
    val b2: scalajs.js.Array[Double],
    val b3: scalajs.js.Array[Double],
    val b4: scalajs.js.Array[Double],
    val b5: scalajs.js.Array[Double],
    val b6: scalajs.js.Array[Double],
    val b7: scalajs.js.Array[Double],
    val b8: scalajs.js.Array[Double],
    val b9: scalajs.js.Array[Double]
) {

  def add(d: DisplayObject): InstanceBuffers =
    new InstanceBuffers(
      b2.concat(scalajs.js.Array[Double](d.x.toDouble, d.y.toDouble)),
      b3.concat(scalajs.js.Array[Double](d.width.toDouble * d.scaleX, d.height.toDouble * d.scaleY)),
      b4.concat(scalajs.js.Array[Double](d.tintR.toDouble, d.tintG.toDouble, d.tintB.toDouble, d.alpha.toDouble)),
      b5.concat(scalajs.js.Array[Double](d.frame.translate.x, d.frame.translate.y)),
      b6.concat(scalajs.js.Array[Double](d.frame.scale.x, d.frame.scale.y)),
      b7.concat(scalajs.js.Array[Double](d.rotation)),
      b8.concat(scalajs.js.Array[Double](if (d.flipHorizontal) -1.0d else 1.0d)),
      b9.concat(scalajs.js.Array[Double](if (d.flipVertical) 1.0d else -1.0d))
    )

}

object InstanceBuffers {
  val empty: InstanceBuffers =
    new InstanceBuffers(
      scalajs.js.Array[Double](),
      scalajs.js.Array[Double](),
      scalajs.js.Array[Double](),
      scalajs.js.Array[Double](),
      scalajs.js.Array[Double](),
      scalajs.js.Array[Double](),
      scalajs.js.Array[Double](),
      scalajs.js.Array[Double]()
    )
}
