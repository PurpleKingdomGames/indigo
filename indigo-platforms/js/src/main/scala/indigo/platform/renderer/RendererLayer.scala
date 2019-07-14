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

class RendererLayer(gl2: WebGL2RenderingContext, textureLocations: List[TextureLookupResult], maxBatchSize: Int) {

  // Instance Array Buffers
  private val translationInstanceArray: WebGLBuffer      = gl2.createBuffer()
  private val scaleInstanceArray: WebGLBuffer            = gl2.createBuffer()
  private val tintInstanceArray: WebGLBuffer             = gl2.createBuffer()
  private val frameTranslationInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val frameScaleInstanceArray: WebGLBuffer       = gl2.createBuffer()
  private val rotationInstanceArray: WebGLBuffer         = gl2.createBuffer()
  private val hFlipInstanceArray: WebGLBuffer            = gl2.createBuffer()
  private val vFlipInstanceArray: WebGLBuffer            = gl2.createBuffer()

  def setupInstanceArray(buffer: WebGLBuffer, location: Int, size: Int): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.enableVertexAttribArray(location)
    gl2.vertexAttribPointer(location, size, FLOAT, false, size * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(location, 1)
  }

  // Instance Data Arrays
  private val translationData: scalajs.js.Array[Double]      = scalajs.js.Array[Double](2d * maxBatchSize)
  private val scaleData: scalajs.js.Array[Double]            = scalajs.js.Array[Double](2d * maxBatchSize)
  private val tintData: scalajs.js.Array[Double]             = scalajs.js.Array[Double](4d * maxBatchSize)
  private val frameTranslationData: scalajs.js.Array[Double] = scalajs.js.Array[Double](2d * maxBatchSize)
  private val frameScaleData: scalajs.js.Array[Double]       = scalajs.js.Array[Double](2d * maxBatchSize)
  private val rotationData: scalajs.js.Array[Double]         = scalajs.js.Array[Double](1d * maxBatchSize)
  private val hFlipData: scalajs.js.Array[Double]            = scalajs.js.Array[Double](1d * maxBatchSize)
  private val vFlipData: scalajs.js.Array[Double]            = scalajs.js.Array[Double](1d * maxBatchSize)

  private def bindData(buffer: WebGLBuffer, data: scalajs.js.Array[Double]) = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(data), STATIC_DRAW)
  }

  private def updateData(d: DisplayObject, i: Int): Unit = {
    translationData((i * 2) + 0) = d.x.toDouble
    translationData((i * 2) + 1) = d.y.toDouble
    scaleData((i * 2) + 0) = d.width.toDouble * d.scaleX
    scaleData((i * 2) + 1) = d.height.toDouble * d.scaleY
    tintData((i * 4) + 0) = d.tintR.toDouble
    tintData((i * 4) + 1) = d.tintG.toDouble
    tintData((i * 4) + 2) = d.tintB.toDouble
    tintData((i * 4) + 3) = d.alpha.toDouble
    frameTranslationData((i * 2) + 0) = d.frame.translate.x
    frameTranslationData((i * 2) + 1) = d.frame.translate.y
    frameScaleData((i * 2) + 0) = d.frame.scale.x
    frameScaleData((i * 2) + 1) = d.frame.scale.y
    rotationData(i) = d.rotation
    hFlipData(i) = if (d.flipHorizontal) -1.0d else 1.0d
    vFlipData(i) = if (d.flipVertical) 1.0d else -1.0d
  }

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
    setupInstanceArray(translationInstanceArray, 2, 2)
    // vec2 a_scale
    setupInstanceArray(scaleInstanceArray, 3, 2)
    // vec4 a_tint
    setupInstanceArray(tintInstanceArray, 4, 4)
    // vec2 a_frameTranslation
    setupInstanceArray(frameTranslationInstanceArray, 5, 2)
    // vec2 a_frameScale
    setupInstanceArray(frameScaleInstanceArray, 6, 2)
    // float a_rotation
    setupInstanceArray(rotationInstanceArray, 7, 1)
    // float a_fliph
    setupInstanceArray(hFlipInstanceArray, 8, 1)
    // float a_flipv
    setupInstanceArray(vFlipInstanceArray, 9, 1)
    //

    val sorted = RendererFunctions.sortByDepth(displayObjects)

    def drawBufferer(instanceCount: Int): Unit =
      if (instanceCount > 0) {
        bindData(translationInstanceArray, translationData)
        bindData(scaleInstanceArray, scaleData)
        bindData(tintInstanceArray, tintData)
        bindData(frameTranslationInstanceArray, frameTranslationData)
        bindData(frameScaleInstanceArray, frameScaleData)
        bindData(rotationInstanceArray, rotationData)
        bindData(hFlipInstanceArray, hFlipData)
        bindData(vFlipInstanceArray, vFlipData)

        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
        metrics.record(layer.metricDraw)
      }

    @tailrec
    def rec(remaining: List[DisplayObject], batchCount: Int, textureName: String): Unit =
      remaining match {
        case Nil =>
          drawBufferer(batchCount)

        case d :: _ if d.imageRef !== textureName =>
          drawBufferer(batchCount)
          textureLocations.find(t => t.name === d.imageRef).foreach { textureLookup =>
            gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }
          rec(remaining, 0, d.imageRef)

        case _ if batchCount === maxBatchSize =>
          drawBufferer(batchCount)
          rec(remaining, 0, textureName)

        case d :: ds =>
          updateData(d, batchCount)
          rec(ds, batchCount + 1, textureName)
      }

    metrics.record(layer.metricStart)
    rec(sorted, 0, "")
    metrics.record(layer.metricEnd)

  }

}
