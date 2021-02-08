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
// import indigo.shared.display.DisplayCloneBatchData
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.platform.renderer.shared.RendererHelper
import indigo.shared.datatypes.RGBA
import scala.collection.mutable.HashMap
import indigo.shared.shader.ShaderId

import scala.scalajs.js.JSConverters._
import indigo.shared.datatypes.mutable.CheapMatrix4

class LayerRenderer(gl2: WebGL2RenderingContext, textureLocations: List[TextureLookupResult], maxBatchSize: Int) {

  private val customDataUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  // Instance Array Buffers
  private val matRotateScaleInstanceArray: WebGLBuffer    = gl2.createBuffer()
  private val matTranslateAlphaInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val sizeAndFrameScaleInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val channelOffsets01InstanceArray: WebGLBuffer  = gl2.createBuffer()
  private val channelOffsets23InstanceArray: WebGLBuffer  = gl2.createBuffer()

  def setupInstanceArray(buffer: WebGLBuffer, location: Int, size: Int): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.enableVertexAttribArray(location)
    gl2.vertexAttribPointer(location, size, FLOAT, false, size * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(location, 1)
  }

  // Instance Data Arrays
  private val matRotateScaleData: scalajs.js.Array[Float]    = scalajs.js.Array[Float](4f * maxBatchSize)
  private val matTranslateAlphaData: scalajs.js.Array[Float] = scalajs.js.Array[Float](4f * maxBatchSize)
  private val sizeAndFrameScaleData: scalajs.js.Array[Float] = scalajs.js.Array[Float](4f * maxBatchSize)
  private val channelOffsets01Data: scalajs.js.Array[Float]  = scalajs.js.Array[Float](4f * maxBatchSize)
  private val channelOffsets23Data: scalajs.js.Array[Float]  = scalajs.js.Array[Float](4f * maxBatchSize)

  @inline private def bindData(buffer: WebGLBuffer, data: scalajs.js.Array[Float]): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(data), STATIC_DRAW)
  }

  private def updateData(d: DisplayObject, i: Int, matrixData1: List[Double], matrixData2: List[Double]): Unit = {
    matRotateScaleData((i * 4) + 0) = matrixData1(0).toFloat
    matRotateScaleData((i * 4) + 1) = matrixData1(1).toFloat
    matRotateScaleData((i * 4) + 2) = matrixData1(2).toFloat
    matRotateScaleData((i * 4) + 3) = matrixData1(3).toFloat

    matTranslateAlphaData((i * 4) + 0) = matrixData2(0).toFloat
    matTranslateAlphaData((i * 4) + 1) = matrixData2(1).toFloat
    matTranslateAlphaData((i * 4) + 2) = matrixData2(2).toFloat
    matTranslateAlphaData((i * 4) + 3) = 0.0f

    sizeAndFrameScaleData((i * 4) + 0) = d.width
    sizeAndFrameScaleData((i * 4) + 1) = d.height
    sizeAndFrameScaleData((i * 4) + 2) = d.frameScaleX
    sizeAndFrameScaleData((i * 4) + 3) = d.frameScaleY

    channelOffsets01Data((i * 4) + 0) = d.channelOffset0X
    channelOffsets01Data((i * 4) + 1) = d.channelOffset0Y
    channelOffsets01Data((i * 4) + 2) = d.channelOffset1X
    channelOffsets01Data((i * 4) + 3) = d.channelOffset1Y

    channelOffsets23Data((i * 4) + 0) = d.channelOffset2X
    channelOffsets23Data((i * 4) + 1) = d.channelOffset2Y
    channelOffsets23Data((i * 4) + 2) = d.channelOffset3X
    channelOffsets23Data((i * 4) + 3) = d.channelOffset3Y
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def drawLayer(
      projection: scalajs.js.Array[Double],
      cloneBlankDisplayObjects: Map[String, DisplayObject],
      displayEntities: ListBuffer[DisplayEntity],
      frameBufferComponents: FrameBufferComponents,
      clearColor: RGBA,
      shaderProgram: WebGLProgram,
      customShaders: HashMap[ShaderId, WebGLProgram],
      runningTime: Double
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor, true)
    gl2.drawBuffers(frameBufferComponents.colorAttachments)

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    var currentProgram: WebGLProgram = null

    def setupShader(program: WebGLProgram): Unit = {
      currentProgram = program

      gl2.useProgram(program)

      gl2.uniformMatrix4fv(
        gl2.getUniformLocation(program, "u_projection"),
        false,
        projection
      )

      gl2.uniform1f(
        gl2.getUniformLocation(program, "TIME"),
        runningTime
      )

      // Instance attributes
      // vec4 a_matRotateScale
      setupInstanceArray(matRotateScaleInstanceArray, 1, 4) //
      // vec4 a_matTranslateAlpha
      setupInstanceArray(matTranslateAlphaInstanceArray, 2, 4) //
      // vec4 a_sizeAndFrameScale
      setupInstanceArray(sizeAndFrameScaleInstanceArray, 3, 4) //
      // vec4 a_channelOffsets01
      setupInstanceArray(channelOffsets01InstanceArray, 4, 4) //
      // vec4 a_channelOffsets23
      setupInstanceArray(channelOffsets23InstanceArray, 5, 4) //
    }

    setupShader(shaderProgram)

    val sorted: ListBuffer[DisplayEntity] =
      RendererHelper.sortByDepth(displayEntities)

    @inline def drawBuffer(instanceCount: Int): Unit =
      if (instanceCount > 0) {
        bindData(matRotateScaleInstanceArray, matRotateScaleData)
        bindData(matTranslateAlphaInstanceArray, matTranslateAlphaData)
        bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
        bindData(channelOffsets01InstanceArray, channelOffsets01Data)
        bindData(channelOffsets23InstanceArray, channelOffsets23Data)

        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
      }

    gl2.activeTexture(TEXTURE0);

    @tailrec
    def rec(remaining: List[DisplayEntity], batchCount: Int, atlasName: Option[String], currentShader: ShaderId, currentShaderHash: String): Unit =
      remaining match {
        case Nil =>
          drawBuffer(batchCount)

        // Switch shader
        case (d: DisplayObject) :: _ if d.shaderId != currentShader =>
          drawBuffer(batchCount)

          customShaders.get(d.shaderId) match {
            case Some(s) =>
              setupShader(s)

            case None =>
              setupShader(shaderProgram)
          }

          rec(remaining, 0, atlasName, d.shaderId, currentShaderHash)

        // Update uniforms
        case (d: DisplayObject) :: _ if d.shaderUniformHash != currentShaderHash =>
          if (d.shaderUniformHash.isEmpty())
            rec(remaining, batchCount, atlasName, d.shaderId, d.shaderUniformHash)
          else {
            drawBuffer(batchCount)

            // UBO blocks must be multiples of 16
            val uboSize: Int =
              Math.ceil(d.shaderUBO.length.toDouble / 16).toInt * 16

            // UBO data
            gl2.bindBuffer(gl2.UNIFORM_BUFFER, customDataUBOBuffer)
            gl2.bufferData(gl2.UNIFORM_BUFFER, uboSize * Float32Array.BYTES_PER_ELEMENT, DYNAMIC_DRAW)
            gl2.bindBufferBase(gl2.UNIFORM_BUFFER, 0, customDataUBOBuffer)
            gl2.bindBufferRange(
              gl2.UNIFORM_BUFFER,
              0,
              customDataUBOBuffer,
              0,
              uboSize * Float32Array.BYTES_PER_ELEMENT
            )
            gl2.bufferSubData(gl2.UNIFORM_BUFFER, 0, new Float32Array(d.shaderUBO.toJSArray))
            gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);

            rec(remaining, 0, atlasName, d.shaderId, d.shaderUniformHash)
          }

        // Switch Atlas
        case (d: DisplayObject) :: _ if d.atlasName != atlasName =>
          drawBuffer(batchCount)

          d.atlasName.flatMap { nextAtlas =>
            textureLocations.find(t => t.name == nextAtlas)
          } match {
            case None =>
              //gl2.bindTexture(TEXTURE_2D, null)
              ()

            case Some(textureLookup) =>
              gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          rec(remaining, 0, d.atlasName, currentShader, currentShaderHash)

        // Batch full
        case _ if batchCount == maxBatchSize =>
          drawBuffer(batchCount)
          rec(remaining, 0, atlasName, currentShader, currentShaderHash)

        case (d: DisplayObject) :: ds =>
          val data = d.transform.data
          updateData(d, batchCount, data._1, data._2)
          rec(ds, batchCount + 1, atlasName, currentShader, currentShaderHash)

        case (c: DisplayClone) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName, currentShader, currentShaderHash)

            case Some(refDisplayObject) =>
              // val cl   = DisplayClone.asBatchData(c)
              val data = c.transform.data
              updateData(refDisplayObject, batchCount, data._1, data._2)
              rec(ds, batchCount + 1, atlasName, currentShader, currentShaderHash)
          }

        case (c: DisplayCloneBatch) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName, currentShader, currentShaderHash)

            case Some(refDisplayObject) =>
              val numberProcessed: Int =
                processCloneBatch(c, refDisplayObject, batchCount)

              rec(ds, batchCount + numberProcessed, atlasName, currentShader, currentShaderHash)
          }

      }

    rec(sorted.toList, 0, None, ShaderId(""), "")

  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private def processCloneBatch(c: DisplayCloneBatch, refDisplayObject: DisplayObject, batchCount: Int): Int = {

    val count: Int                         = c.clones.length
    var i: Int                             = 0
    var data: (List[Double], List[Double]) = (Nil, Nil)
    var cl: CheapMatrix4          = CheapMatrix4.identity

    while (i < count) {
      cl = c.clones(i)
      data = cl.data
      updateData(refDisplayObject, batchCount + i, data._1, data._2)
      i += 1
    }

    count
  }

}
