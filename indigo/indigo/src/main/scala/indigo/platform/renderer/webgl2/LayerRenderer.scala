package indigo.platform.renderer.webgl2

import indigo.facades.WebGL2RenderingContext
import indigo.shared.display.DisplayObject
import org.scalajs.dom.raw.WebGLProgram
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLTexture
import org.scalajs.dom.raw

import scala.annotation.tailrec
import scala.scalajs.js.typedarray.Float32Array
import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayClone
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayText
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.shared.datatypes.RGBA
import scala.collection.mutable.HashMap
import indigo.shared.shader.ShaderId
import indigo.platform.assets.AtlasId
import indigo.shared.scenegraph.CloneId

import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.platform.renderer.shared.WebGLHelper

class LayerRenderer(
    gl2: WebGL2RenderingContext,
    textureLocations: List[TextureLookupResult],
    maxBatchSize: Int,
    projectionUBOBuffer: => WebGLBuffer,
    frameDataUBOBuffer: => WebGLBuffer,
    lightDataUBOBuffer: => WebGLBuffer,
    textContext: raw.CanvasRenderingContext2D,
    textTexture: WebGLTexture
) {

  private val customDataUBOBuffers: HashMap[String, WebGLBuffer] =
    HashMap.empty[String, WebGLBuffer]

  // Instance Array Buffers
  private val matRotateScaleInstanceArray: WebGLBuffer       = gl2.createBuffer()
  private val matTranslateRotationInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val sizeAndFrameScaleInstanceArray: WebGLBuffer    = gl2.createBuffer()
  private val channelOffsets01InstanceArray: WebGLBuffer     = gl2.createBuffer()
  private val channelOffsets23InstanceArray: WebGLBuffer     = gl2.createBuffer()

  def setupInstanceArray(buffer: WebGLBuffer, location: Int, size: Int): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.enableVertexAttribArray(location)
    gl2.vertexAttribPointer(location, size, FLOAT, false, size * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(location, 1)
  }

  // Instance Data Arrays
  private val matRotateScaleData: scalajs.js.Array[Float]       = scalajs.js.Array[Float](4f * maxBatchSize)
  private val matTranslateRotationData: scalajs.js.Array[Float] = scalajs.js.Array[Float](4f * maxBatchSize)
  private val sizeAndFrameScaleData: scalajs.js.Array[Float]    = scalajs.js.Array[Float](4f * maxBatchSize)
  private val channelOffsets01Data: scalajs.js.Array[Float]     = scalajs.js.Array[Float](4f * maxBatchSize)
  private val channelOffsets23Data: scalajs.js.Array[Float]     = scalajs.js.Array[Float](4f * maxBatchSize)

  @inline private def bindData(buffer: WebGLBuffer, data: scalajs.js.Array[Float]): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(data), STATIC_DRAW)
  }

  private def updateData(d: DisplayObject, i: Int, matrixData1: List[Double], matrixData2: List[Double]): Unit = {
    matRotateScaleData((i * 4) + 0) = matrixData1(0).toFloat
    matRotateScaleData((i * 4) + 1) = matrixData1(1).toFloat
    matRotateScaleData((i * 4) + 2) = matrixData1(2).toFloat
    matRotateScaleData((i * 4) + 3) = matrixData1(3).toFloat

    matTranslateRotationData((i * 4) + 0) = matrixData2(0).toFloat
    matTranslateRotationData((i * 4) + 1) = matrixData2(1).toFloat
    matTranslateRotationData((i * 4) + 2) = matrixData2(2).toFloat
    matTranslateRotationData((i * 4) + 3) = d.rotation.toFloat

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

  private def updateTextData(d: DisplayText, i: Int, matrixData1: List[Double], matrixData2: List[Double]): Unit = {
    matRotateScaleData((i * 4) + 0) = matrixData1(0).toFloat
    matRotateScaleData((i * 4) + 1) = matrixData1(1).toFloat
    matRotateScaleData((i * 4) + 2) = matrixData1(2).toFloat
    matRotateScaleData((i * 4) + 3) = matrixData1(3).toFloat

    matTranslateRotationData((i * 4) + 0) = matrixData2(0).toFloat
    matTranslateRotationData((i * 4) + 1) = matrixData2(1).toFloat
    matTranslateRotationData((i * 4) + 2) = matrixData2(2).toFloat
    matTranslateRotationData((i * 4) + 3) = d.rotation.toFloat

    sizeAndFrameScaleData((i * 4) + 0) = d.width.toFloat
    sizeAndFrameScaleData((i * 4) + 1) = d.height.toFloat
    sizeAndFrameScaleData((i * 4) + 2) = 1
    sizeAndFrameScaleData((i * 4) + 3) = 1

    channelOffsets01Data((i * 4) + 0) = 0
    channelOffsets01Data((i * 4) + 1) = 0
    channelOffsets01Data((i * 4) + 2) = 0
    channelOffsets01Data((i * 4) + 3) = 0

    channelOffsets23Data((i * 4) + 0) = 0
    channelOffsets23Data((i * 4) + 1) = 0
    channelOffsets23Data((i * 4) + 2) = 0
    channelOffsets23Data((i * 4) + 3) = 0
  }

  def requiresContextChange(
      d: DisplayObject,
      atlasName: Option[AtlasId],
      currentShader: ShaderId,
      currentUniformHash: String
  ): Boolean = {
    val uniformHash: String = d.shaderUniformData.map(_.uniformHash).mkString

    d.shaderId != currentShader ||
    (uniformHash.nonEmpty && uniformHash != currentUniformHash) ||
    d.atlasName != atlasName
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var currentProgram: WebGLProgram                           = null
  private given CanEqual[Option[WebGLProgram], Option[WebGLProgram]] = CanEqual.derived

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def doContextChange(
      d: DisplayObject,
      atlasName: Option[AtlasId],
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
            throw new Exception(
              s"Missing entity shader '${d.shaderId}'. Have you remembered to add the shader to the boot sequence or disabled auto-loading of default shaders?"
            )
        }
      else currentProgram

    // UBO data
    val uniformHash: String = d.shaderUniformData.map(_.uniformHash).mkString
    if (uniformHash.nonEmpty && uniformHash != currentUniformHash)
      d.shaderUniformData.zipWithIndex.foreach { case (ud, i) =>
        val buff = customDataUBOBuffers.getOrElseUpdate(ud.uniformHash, gl2.createBuffer())

        WebGLHelper.attachUBOData(gl2, ud.data, buff)
        WebGLHelper.bindUBO(
          gl2,
          activeShader,
          ud.blockName,
          RendererWebGL2Constants.customDataBlockOffsetPointer + i,
          buff
        )
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

    WebGLHelper.bindUBO(
      gl2,
      program,
      "IndigoProjectionData",
      RendererWebGL2Constants.projectionBlockPointer,
      projectionUBOBuffer
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      "IndigoFrameData",
      RendererWebGL2Constants.frameDataBlockPointer,
      frameDataUBOBuffer
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      "IndigoDynamicLightingData",
      RendererWebGL2Constants.lightDataBlockPointer,
      lightDataUBOBuffer
    )

    // Instance attributes
    // vec4 a_matRotateScale
    setupInstanceArray(matRotateScaleInstanceArray, 1, 4) //
    // vec4 a_matTranslateAlpha
    setupInstanceArray(matTranslateRotationInstanceArray, 2, 4) //
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
      bindData(matTranslateRotationInstanceArray, matTranslateRotationData)
      bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
      bindData(channelOffsets01InstanceArray, channelOffsets01Data)
      bindData(channelOffsets23InstanceArray, channelOffsets23Data)

      gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
    }

  private given CanEqual[List[DisplayEntity], List[DisplayEntity]] = CanEqual.derived

  def drawLayer(
      cloneBlankDisplayObjects: Map[CloneId, DisplayObject],
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
    def rec(
        remaining: List[DisplayEntity],
        batchCount: Int,
        atlasName: Option[AtlasId],
        currentShader: ShaderId,
        currentShaderHash: String
    ): Unit =
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
          rec(remaining, 0, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).mkString)

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
                rec(remaining, 0, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).mkString)
              } else {
                val data = c.transform.data
                updateData(d, batchCount, data._1, data._2)
                rec(ds, batchCount + 1, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).mkString)
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
                rec(remaining, 0, d.atlasName, d.shaderId, d.shaderUniformData.map(_.uniformHash).mkString)
              } else {
                val numberProcessed: Int =
                  processCloneBatch(c, d, batchCount)

                rec(
                  ds,
                  batchCount + numberProcessed,
                  d.atlasName,
                  d.shaderId,
                  d.shaderUniformData.map(_.uniformHash).mkString
                )
              }
          }

        case (t: DisplayText) :: ds =>
          drawBuffer(batchCount)

          // Change context
          val shaderId = indigo.shared.shader.StandardShaders.Bitmap.id
          if (currentShader != shaderId) {
            customShaders.get(shaderId) match {
              case Some(s) =>
                currentProgram = s
                setupShader(s)

              case None =>
                throw new Exception(
                  s"(TextBox) Missing entity shader '$shaderId'. Have you remembered to add the shader to the boot sequence or disabled auto-loading of default shaders?"
                )
            }
          }

          gl2.bindTexture(TEXTURE_2D, textTexture)

          gl2.texImage2D(
            TEXTURE_2D,
            0,
            WebGLRenderingContext.RGBA,
            WebGLRenderingContext.RGBA,
            UNSIGNED_BYTE,
            makeTextImageData(t.text, t.width, t.height)
          )

          val data = t.transform.data
          updateTextData(t, 0, data._1, data._2)
          rec(ds, 0 + 1, None, shaderId, "")
      }

    rec(sorted.toList, 0, None, ShaderId(""), "")
  }

  private def makeTextImageData(
      text: String,
      width: Int,
      height: Int
  ): raw.HTMLCanvasElement = {
    textContext.canvas.width = width
    textContext.canvas.height = height
    textContext.font = "14px monospace"
    textContext.textAlign = "center"
    textContext.textBaseline = "middle"
    textContext.fillStyle = "white"
    textContext.clearRect(0, 0, textContext.canvas.width, textContext.canvas.height)
    textContext.fillText(text, width / 2, height / 2)

    textContext.canvas
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
