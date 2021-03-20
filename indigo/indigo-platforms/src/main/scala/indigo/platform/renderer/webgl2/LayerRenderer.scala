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
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.shared.datatypes.RGBA
import scala.collection.mutable.HashMap
import indigo.shared.shader.ShaderId

import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.platform.renderer.shared.WebGLHelper

class LayerRenderer(
    gl2: WebGL2RenderingContext,
    textureLocations: List[TextureLookupResult],
    maxBatchSize: Int,
    projectionUBOBuffer: => WebGLBuffer,
    frameDataUBOBuffer: => WebGLBuffer
) {

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

  def requiresContextChange(d: DisplayObject, atlasName: Option[String], currentShader: ShaderId, currentUniformHash: String): Boolean = {
    val uniformHash: String = d.shaderUniformData.map(_.uniformHash).getOrElse("")

    d.shaderId != currentShader ||
    (uniformHash.nonEmpty && uniformHash != currentUniformHash) ||
    d.atlasName != atlasName
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var currentProgram: WebGLProgram = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def doContextChange(
      d: DisplayObject,
      atlasName: Option[String],
      currentShader: ShaderId,
      currentUniformHash: String,
      customShaders: HashMap[ShaderId, WebGLProgram]
  ): Unit = {

    // Switch and reference shader
    val activeShader: WebGLProgram =
      if (d.shaderId != currentShader)
        customShaders.get(d.shaderId) match {
          case Some(s) =>
            currentProgram = s
            setupShader(s)
            s

          case None =>
            throw new Exception(s"Missing entity shader '${d.shaderId.value}'. Have you remembered to add the shader to the boot sequence or disabled auto-loading of default shaders?")
        }
      else currentProgram

    // UBO data
    d.shaderUniformData.foreach { ud =>
      if (ud.uniformHash.nonEmpty && ud.uniformHash != currentUniformHash) {
        WebGLHelper.attachUBOData(gl2, ud.data, customDataUBOBuffer)
        if (d.shaderId != currentShader)
          WebGLHelper.bindUBO(gl2, activeShader, ud.blockName, RendererWebGL2Constants.customDataBlockPointer, customDataUBOBuffer)
      }
    }

    // Atlas
    if (d.atlasName != atlasName)
      d.atlasName.flatMap { nextAtlas =>
        textureLocations.find(t => t.name == nextAtlas)
      } match {
        case None =>
          ()

        case Some(textureLookup) =>
          gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
      }

    ()
  }

  def setupShader(program: WebGLProgram): Unit = {

    gl2.useProgram(program)

    WebGLHelper.bindUBO(gl2, program, "IndigoProjectionData", RendererWebGL2Constants.projectionBlockPointer, projectionUBOBuffer)
    WebGLHelper.bindUBO(gl2, program, "IndigoFrameData", RendererWebGL2Constants.frameDataBlockPointer, frameDataUBOBuffer)

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

  @inline def drawBuffer(instanceCount: Int): Unit =
    if (instanceCount > 0) {
      bindData(matRotateScaleInstanceArray, matRotateScaleData)
      bindData(matTranslateAlphaInstanceArray, matTranslateAlphaData)
      bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
      bindData(channelOffsets01InstanceArray, channelOffsets01Data)
      bindData(channelOffsets23InstanceArray, channelOffsets23Data)

      gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
    }

  def drawLayer(
      cloneBlankDisplayObjects: Map[String, DisplayObject],
      displayEntities: ListBuffer[DisplayEntity],
      frameBufferComponents: FrameBufferComponents,
      clearColor: RGBA,
      customShaders: HashMap[ShaderId, WebGLProgram]
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor, true)
    gl2.drawBuffers(frameBufferComponents.colorAttachments)

    val sorted: ListBuffer[DisplayEntity] =
      displayEntities.sortWith((d1, d2) => d1.z > d2.z)

    gl2.activeTexture(TEXTURE0);

    @tailrec
    def rec(remaining: List[DisplayEntity], batchCount: Int, atlasName: Option[String], currentShader: ShaderId, currentShaderHash: String): Unit =
      remaining match {
        case Nil =>
          drawBuffer(batchCount)

        case _ if batchCount == maxBatchSize =>
          drawBuffer(batchCount)
          rec(remaining, 0, atlasName, currentShader, currentShaderHash)

        case (d: DisplayObject) :: _ if requiresContextChange(d, atlasName, currentShader, currentShaderHash) =>
          drawBuffer(batchCount)
          doContextChange(
            d,
            atlasName,
            currentShader,
            currentShaderHash,
            customShaders
          )
          rec(remaining, 0, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).getOrElse(""))

        case (d: DisplayObject) :: ds =>
          val data = d.transform.data
          updateData(d, batchCount, data._1, data._2)
          rec(ds, batchCount + 1, atlasName, currentShader, currentShaderHash)

        case (c: DisplayClone) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName, currentShader, currentShaderHash)

            case Some(d) =>
              if (requiresContextChange(d, atlasName, currentShader, currentShaderHash)) {
                drawBuffer(batchCount)
                doContextChange(
                  d,
                  atlasName,
                  currentShader,
                  currentShaderHash,
                  customShaders
                )
                rec(remaining, 0, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).getOrElse(""))
              } else {
                val data = c.transform.data
                updateData(d, batchCount, data._1, data._2)
                rec(ds, batchCount + 1, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).getOrElse(""))
              }
          }

        case (c: DisplayCloneBatch) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName, currentShader, currentShaderHash)

            case Some(d) =>
              if (requiresContextChange(d, atlasName, currentShader, currentShaderHash)) {
                drawBuffer(batchCount)
                doContextChange(
                  d,
                  atlasName,
                  currentShader,
                  currentShaderHash,
                  customShaders
                )
                rec(remaining, 0, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).getOrElse(""))
              } else {
                val numberProcessed: Int =
                  processCloneBatch(c, d, batchCount)

                rec(ds, batchCount + numberProcessed, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).getOrElse(""))
              }
          }

      }

    rec(sorted.toList, 0, None, ShaderId(""), "")
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private def processCloneBatch(c: DisplayCloneBatch, refDisplayObject: DisplayObject, batchCount: Int): Int = {
    val count: Int                         = c.clones.length
    var i: Int                             = 0
    var data: (List[Double], List[Double]) = (Nil, Nil)
    var cl: CheapMatrix4                   = CheapMatrix4.identity

    while (i < count) {
      cl = c.clones(i)
      data = cl.data
      updateData(refDisplayObject, batchCount + i, data._1, data._2)
      i += 1
    }

    count
  }

}
