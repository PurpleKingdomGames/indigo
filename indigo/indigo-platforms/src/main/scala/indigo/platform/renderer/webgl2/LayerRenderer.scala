package indigo.platform.renderer.webgl2

import indigo.facades.WebGL2RenderingContext
import indigo.shared.display.DisplayObject
import org.scalajs.dom.raw.WebGLProgram
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer

import scala.annotation.tailrec
import scala.scalajs.js.typedarray.Float32Array
import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayClone
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneBatchData
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.platform.renderer.shared.RendererHelper
import indigo.shared.datatypes.RGBA
import indigo.shared.display.DisplayObjectShape
import scala.collection.mutable.HashMap
import indigo.shared.display.ShaderId

class LayerRenderer(gl2: WebGL2RenderingContext, textureLocations: List[TextureLookupResult], maxBatchSize: Int) {

  // Instance Array Buffers
  private val matRotateScaleInstanceArray: WebGLBuffer    = gl2.createBuffer()
  private val matTranslateAlphaInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val sizeInstanceArray: WebGLBuffer              = gl2.createBuffer()
  private val frameTransformInstanceArray: WebGLBuffer    = gl2.createBuffer()

  def setupInstanceArray(buffer: WebGLBuffer, location: Int, size: Int): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.enableVertexAttribArray(location)
    gl2.vertexAttribPointer(location, size, FLOAT, false, size * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(location, 1)
  }

  // Instance Data Arrays
  private val matRotateScaleData: scalajs.js.Array[Float]    = scalajs.js.Array[Float](4f * maxBatchSize)
  private val matTranslateAlphaData: scalajs.js.Array[Float] = scalajs.js.Array[Float](4f * maxBatchSize)
  private val sizeData: scalajs.js.Array[Float]              = scalajs.js.Array[Float](4f * maxBatchSize)
  private val frameTransformData: scalajs.js.Array[Float]    = scalajs.js.Array[Float](4f * maxBatchSize)

  @inline private def bindData(buffer: WebGLBuffer, data: scalajs.js.Array[Float]): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(data), STATIC_DRAW)
  }

  private def updateData(d: DisplayObject, i: Int, matrixData1: List[Double], matrixData2: List[Double], alpha: Float): Unit = {
    matRotateScaleData((i * 4) + 0) = matrixData1(0).toFloat
    matRotateScaleData((i * 4) + 1) = matrixData1(1).toFloat
    matRotateScaleData((i * 4) + 2) = matrixData1(2).toFloat
    matRotateScaleData((i * 4) + 3) = matrixData1(3).toFloat

    matTranslateAlphaData((i * 4) + 0) = matrixData2(0).toFloat
    matTranslateAlphaData((i * 4) + 1) = matrixData2(1).toFloat
    matTranslateAlphaData((i * 4) + 2) = matrixData2(2).toFloat
    matTranslateAlphaData((i * 4) + 3) = alpha

    sizeData((i * 2) + 0) = d.width
    sizeData((i * 2) + 1) = d.height

    frameTransformData((i * 4) + 0) = d.frameX
    frameTransformData((i * 4) + 1) = d.frameY
    frameTransformData((i * 4) + 2) = d.frameScaleX
    frameTransformData((i * 4) + 3) = d.frameScaleY
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def drawLayer(
      projection: scalajs.js.Array[Double],
      cloneBlankDisplayObjects: Map[String, DisplayObject],
      displayEntities: ListBuffer[DisplayEntity],
      frameBufferComponents: FrameBufferComponents,
      clearColor: RGBA,
      shaderProgram: WebGLProgram,
      customShaders: HashMap[ShaderId, WebGLProgram]
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor)
    gl2.drawBuffers(frameBufferComponents.colorAttachments)

    def setupShader(program: WebGLProgram): Unit = {

      gl2.useProgram(program)

      val textureLocation = gl2.getUniformLocation(program, "u_textureDiffuse")
      gl2.uniform1i(textureLocation, 0)

      // Projection
      val projectionLocation = gl2.getUniformLocation(program, "u_projection")
      gl2.uniformMatrix4fv(projectionLocation, false, projection)

      // Instance attributes
      // vec4 a_matRotateScale
      setupInstanceArray(matRotateScaleInstanceArray, 1, 4) //
      // vec4 a_matTranslateAlpha
      setupInstanceArray(matTranslateAlphaInstanceArray, 2, 4) //
      // vec2 a_size
      setupInstanceArray(sizeInstanceArray, 3, 2) //
      // vec4 a_frameTransform
      setupInstanceArray(frameTransformInstanceArray, 4, 4) //
    }

    setupShader(shaderProgram)

    val sorted: ListBuffer[DisplayEntity] =
      RendererHelper.sortByDepth(displayEntities)

    @inline def drawBuffer(instanceCount: Int): Unit =
      if (instanceCount > 0) {
        bindData(matRotateScaleInstanceArray, matRotateScaleData)
        bindData(matTranslateAlphaInstanceArray, matTranslateAlphaData)
        bindData(sizeInstanceArray, sizeData)
        bindData(frameTransformInstanceArray, frameTransformData)

        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
      }

    @tailrec
    def rec(remaining: List[DisplayEntity], batchCount: Int, atlasName: String, currentShader: Option[ShaderId]): Unit =
      remaining match {
        case Nil =>
          drawBuffer(batchCount)

        case (_: DisplayObjectShape) :: ds =>
          //TODO
          // val data = d.transform.data
          // updateData(d, batchCount, data._1, data._2, d.effects.alpha)
          rec(ds, batchCount, atlasName, currentShader)

        case (d: DisplayObject) :: _ if d.shaderId != currentShader =>
          drawBuffer(batchCount)

          d.shaderId.flatMap(customShaders.get) match {
            case Some(s) =>
              setupShader(s)

            case None =>
              setupShader(shaderProgram)
          }

          rec(remaining, 0, d.atlasName, d.shaderId)

        case (d: DisplayObject) :: _ if d.atlasName != atlasName =>
          drawBuffer(batchCount)

          // Diffuse
          textureLocations.find(t => t.name == d.atlasName) match {
            case None =>
              gl2.activeTexture(TEXTURE0);
              gl2.bindTexture(TEXTURE_2D, null)

            case Some(textureLookup) =>
              gl2.activeTexture(TEXTURE0);
              gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          rec(remaining, 0, d.atlasName, currentShader)

        case _ if batchCount == maxBatchSize =>
          drawBuffer(batchCount)
          rec(remaining, 0, atlasName, currentShader)

        case (d: DisplayObject) :: ds =>
          val data = d.transform.data
          updateData(d, batchCount, data._1, data._2, d.effects.alpha)
          rec(ds, batchCount + 1, atlasName, currentShader)

        case (c: DisplayClone) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName, currentShader)

            case Some(refDisplayObject) =>
              val cl   = DisplayClone.asBatchData(c)
              val data = c.transform.data
              updateData(refDisplayObject, batchCount, data._1, data._2, cl.alpha)
              rec(ds, batchCount + 1, atlasName, currentShader)
          }

        case (c: DisplayCloneBatch) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName, currentShader)

            case Some(refDisplayObject) =>
              val numberProcessed: Int =
                processCloneBatch(c, refDisplayObject, batchCount)

              rec(ds, batchCount + numberProcessed, atlasName, currentShader)
          }

      }

    rec(sorted.toList, 0, "", None)

  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private def processCloneBatch(c: DisplayCloneBatch, refDisplayObject: DisplayObject, batchCount: Int): Int = {

    val count: Int                         = c.clones.length
    var i: Int                             = 0
    var data: (List[Double], List[Double]) = (Nil, Nil)
    var cl: DisplayCloneBatchData          = DisplayCloneBatchData.None

    while (i < count) {
      cl = c.clones(i)
      data = cl.transform.data
      updateData(refDisplayObject, batchCount + i, data._1, data._2, cl.alpha)
      i += 1
    }

    count
  }

}
