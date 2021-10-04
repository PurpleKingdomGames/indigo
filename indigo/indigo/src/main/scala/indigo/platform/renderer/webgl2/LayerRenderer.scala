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
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneTiles
import indigo.shared.display.DisplayText
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.shared.datatypes.RGBA
import scala.collection.mutable.HashMap
import indigo.shared.shader.ShaderId
import indigo.platform.assets.AtlasId
import indigo.shared.scenegraph.CloneId

import indigo.platform.assets.DynamicText

import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.platform.renderer.shared.WebGLHelper
import indigo.shared.display.DisplayGroup
import indigo.shared.scenegraph.CloneBatchData
import indigo.shared.scenegraph.CloneTileData

import scalajs.js.JSConverters._

class LayerRenderer(
    gl2: WebGL2RenderingContext,
    textureLocations: List[TextureLookupResult],
    maxBatchSize: Int,
    projectionUBOBuffer: => WebGLBuffer,
    frameDataUBOBuffer: => WebGLBuffer,
    cloneReferenceUBOBuffer: => WebGLBuffer,
    lightDataUBOBuffer: => WebGLBuffer,
    dynamicText: DynamicText,
    textTexture: WebGLTexture
) {

  private val customDataUBOBuffers: HashMap[String, WebGLBuffer] =
    HashMap.empty[String, WebGLBuffer]

  // Instance Array Buffers
  private val translateScaleInstanceArray: WebGLBuffer       = gl2.createBuffer()
  private val refFlipInstanceArray: WebGLBuffer              = gl2.createBuffer()
  private val sizeAndFrameScaleInstanceArray: WebGLBuffer    = gl2.createBuffer()
  private val channelOffsets01InstanceArray: WebGLBuffer     = gl2.createBuffer()
  private val channelOffsets23InstanceArray: WebGLBuffer     = gl2.createBuffer()
  private val textureSizeAtlasSizeInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val rotationInstanceArray: WebGLBuffer             = gl2.createBuffer()

  def setupInstanceArray(buffer: WebGLBuffer, location: Int, size: Int): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.enableVertexAttribArray(location)
    gl2.vertexAttribPointer(location, size, FLOAT, false, size * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(location, 1)
  }

  // Instance Data Arrays
  private val translateScaleData: scalajs.js.Array[Float]       = scalajs.js.Array[Float](4f * maxBatchSize)
  private val refFlipData: scalajs.js.Array[Float]              = scalajs.js.Array[Float](4f * maxBatchSize)
  private val sizeAndFrameScaleData: scalajs.js.Array[Float]    = scalajs.js.Array[Float](4f * maxBatchSize)
  private val channelOffsets01Data: scalajs.js.Array[Float]     = scalajs.js.Array[Float](4f * maxBatchSize)
  private val channelOffsets23Data: scalajs.js.Array[Float]     = scalajs.js.Array[Float](4f * maxBatchSize)
  private val textureSizeAtlasSizeData: scalajs.js.Array[Float] = scalajs.js.Array[Float](4f * maxBatchSize)
  private val rotationData: scalajs.js.Array[Float]             = scalajs.js.Array[Float](1f * maxBatchSize)

  @inline private def bindData(buffer: WebGLBuffer, data: scalajs.js.Array[Float]): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(data), STATIC_DRAW)
  }

  private def updateData(d: DisplayObject, i: Int): Unit = {
    translateScaleData((i * 4) + 0) = d.x
    translateScaleData((i * 4) + 1) = d.y
    translateScaleData((i * 4) + 2) = d.scaleX
    translateScaleData((i * 4) + 3) = d.scaleY

    rotationData(i) = d.rotation.toFloat

    refFlipData((i * 4) + 0) = d.refX
    refFlipData((i * 4) + 1) = d.refY
    refFlipData((i * 4) + 2) = d.flipX
    refFlipData((i * 4) + 3) = d.flipY

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

    textureSizeAtlasSizeData((i * 4) + 0) = d.textureWidth
    textureSizeAtlasSizeData((i * 4) + 1) = d.textureHeight
    textureSizeAtlasSizeData((i * 4) + 2) = d.atlasWidth
    textureSizeAtlasSizeData((i * 4) + 3) = d.atlasHeight
  }

  inline private def updateCloneData(
      i: Int,
      x: Float,
      y: Float,
      rotation: Float,
      scaleX: Float,
      scaleY: Float
  ): Unit = {
    translateScaleData((i * 4) + 0) = x
    translateScaleData((i * 4) + 1) = y
    translateScaleData((i * 4) + 2) = scaleX
    translateScaleData((i * 4) + 3) = scaleY

    rotationData(i) = rotation
  }

  inline private def updateCloneTileData(
      i: Int,
      x: Float,
      y: Float,
      rotation: Float,
      scaleX: Float,
      scaleY: Float,
      frameScaleX: Float,
      frameScaleY: Float,
      channelOffset0X: Float,
      channelOffset0Y: Float,
      channelOffset1X: Float,
      channelOffset1Y: Float,
      channelOffset2X: Float,
      channelOffset2Y: Float,
      channelOffset3X: Float,
      channelOffset3Y: Float
  ): Unit = {
    translateScaleData((i * 4) + 0) = x
    translateScaleData((i * 4) + 1) = y
    translateScaleData((i * 4) + 2) = scaleX
    translateScaleData((i * 4) + 3) = scaleY

    sizeAndFrameScaleData((i * 4) + 2) = frameScaleX
    sizeAndFrameScaleData((i * 4) + 3) = frameScaleY

    channelOffsets01Data((i * 4) + 0) = channelOffset0X
    channelOffsets01Data((i * 4) + 1) = channelOffset0Y
    channelOffsets01Data((i * 4) + 2) = channelOffset1X
    channelOffsets01Data((i * 4) + 3) = channelOffset1Y

    channelOffsets23Data((i * 4) + 0) = channelOffset2X
    channelOffsets23Data((i * 4) + 1) = channelOffset2Y
    channelOffsets23Data((i * 4) + 2) = channelOffset3X
    channelOffsets23Data((i * 4) + 3) = channelOffset3Y

    rotationData(i) = rotation
  }

  private def updateTextData(d: DisplayText, i: Int): Unit = {
    translateScaleData((i * 4) + 0) = d.x
    translateScaleData((i * 4) + 1) = d.y
    translateScaleData((i * 4) + 2) = d.scaleX
    translateScaleData((i * 4) + 3) = d.scaleY

    refFlipData((i * 4) + 0) = d.refX
    refFlipData((i * 4) + 1) = d.refY
    refFlipData((i * 4) + 2) = d.flipX
    refFlipData((i * 4) + 3) = d.flipY

    rotationData(i) = d.rotation.toFloat

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

    textureSizeAtlasSizeData((i * 4) + 0) = 0
    textureSizeAtlasSizeData((i * 4) + 1) = 0
    textureSizeAtlasSizeData((i * 4) + 2) = 0
    textureSizeAtlasSizeData((i * 4) + 3) = 0
  }

  def init(): LayerRenderer =

    // pre-populate array
    val refData: Array[Float] =
      List.fill(20)(0.0f).toArray
    WebGLHelper.attachUBOData(gl2, refData, cloneReferenceUBOBuffer)

    this

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

  private def setBaseTransform(baseTransform: CheapMatrix4): Unit =
    if currentProgram == null then ()
    else
      gl2.uniformMatrix4fv(
        location = gl2.getUniformLocation(currentProgram, "u_baseTransform"),
        transpose = false,
        value = Float32Array(baseTransform.toArray.toJSArray)
      )

  private def setMode(mode: Int): Unit =
    if currentProgram == null then ()
    else
      gl2.uniform1i(
        gl2.getUniformLocation(currentProgram, "u_mode"),
        mode
      )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def doContextChange(
      d: DisplayObject,
      atlasName: Option[AtlasId],
      currentShader: ShaderId,
      currentUniformHash: String,
      customShaders: HashMap[ShaderId, WebGLProgram],
      baseTransform: CheapMatrix4
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

    // Base transform
    setBaseTransform(baseTransform)

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
      "IndigoCloneReferenceData",
      RendererWebGL2Constants.cloneReferenceDataBlockPointer,
      cloneReferenceUBOBuffer
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
    setupInstanceArray(translateScaleInstanceArray, 1, 4) //
    // vec4 a_matTranslateAlpha
    setupInstanceArray(refFlipInstanceArray, 2, 4) //
    // vec4 a_sizeAndFrameScale
    setupInstanceArray(sizeAndFrameScaleInstanceArray, 3, 4) //
    // vec4 a_channelOffsets01
    setupInstanceArray(channelOffsets01InstanceArray, 4, 4) //
    // vec4 a_channelOffsets23
    setupInstanceArray(channelOffsets23InstanceArray, 5, 4) //
    // vec4 a_textureSize + atlasSize
    setupInstanceArray(textureSizeAtlasSizeInstanceArray, 6, 4) //
    // float a_rotation
    setupInstanceArray(rotationInstanceArray, 7, 1) //
  }

  def enableCloneBatchMode(): Unit =
    setMode(1)
    gl2.disableVertexAttribArray(2)
    gl2.disableVertexAttribArray(3)
    gl2.disableVertexAttribArray(4)
    gl2.disableVertexAttribArray(5)
    gl2.disableVertexAttribArray(6)

  def enableCloneTileMode(): Unit =
    setMode(2)
    gl2.disableVertexAttribArray(2)
    gl2.enableVertexAttribArray(3)
    gl2.enableVertexAttribArray(4)
    gl2.enableVertexAttribArray(5)
    gl2.disableVertexAttribArray(6)

  def disableCloneMode(): Unit =
    setMode(0)
    gl2.enableVertexAttribArray(2)
    gl2.enableVertexAttribArray(3)
    gl2.enableVertexAttribArray(4)
    gl2.enableVertexAttribArray(5)
    gl2.enableVertexAttribArray(6)

  def drawBuffer(instanceCount: Int): Unit =
    if (instanceCount > 0) {
      disableCloneMode()

      bindData(translateScaleInstanceArray, translateScaleData)
      bindData(refFlipInstanceArray, refFlipData)
      bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
      bindData(channelOffsets01InstanceArray, channelOffsets01Data)
      bindData(channelOffsets23InstanceArray, channelOffsets23Data)
      bindData(textureSizeAtlasSizeInstanceArray, textureSizeAtlasSizeData)
      bindData(rotationInstanceArray, rotationData)

      gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
    }

  def drawCloneBuffer(instanceCount: Int): Unit =
    if (instanceCount > 0) {
      enableCloneBatchMode()

      bindData(translateScaleInstanceArray, translateScaleData)
      bindData(rotationInstanceArray, rotationData)

      gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
    }

  def drawCloneTileBuffer(instanceCount: Int): Unit =
    if (instanceCount > 0) {
      enableCloneTileMode()

      bindData(translateScaleInstanceArray, translateScaleData)
      bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
      bindData(channelOffsets01InstanceArray, channelOffsets01Data)
      bindData(channelOffsets23InstanceArray, channelOffsets23Data)
      bindData(rotationInstanceArray, rotationData)

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

    gl2.activeTexture(TEXTURE0);

    renderEntities(cloneBlankDisplayObjects, displayEntities, customShaders, CheapMatrix4.identity)
  }

  private def renderEntities(
      cloneBlankDisplayObjects: Map[CloneId, DisplayObject],
      displayEntities: ListBuffer[DisplayEntity],
      customShaders: HashMap[ShaderId, WebGLProgram],
      baseTransform: CheapMatrix4
  ): Unit = {

    val count: Int                    = displayEntities.length
    var i: Int                        = 0
    var batchCount: Int               = 0
    var atlasName: Option[AtlasId]    = None
    var currentShader: ShaderId       = ShaderId("")
    var currentShaderHash: String     = ""
    var currentBaseTransformHash: Int = 0

    val sortedEntities: Vector[DisplayEntity] =
      displayEntities.sortWith((d1, d2) => d1.z > d2.z).toVector

    while (i <= count)
      if i == count then
        drawBuffer(batchCount)
        i += 1
      else if batchCount == maxBatchSize then
        drawBuffer(batchCount)
        batchCount = 0
      else
        sortedEntities(i) match {
          case d: DisplayGroup if d.entities.isEmpty =>
            i += 1

          case d: DisplayGroup =>
            drawBuffer(batchCount)
            batchCount = 0
            atlasName = None
            currentShader = ShaderId("")
            currentShaderHash = ""
            renderEntities(cloneBlankDisplayObjects, d.entities, customShaders, d.transform * baseTransform)
            setBaseTransform(baseTransform)
            i += 1

          case d: DisplayObject if requiresContextChange(d, atlasName, currentShader, currentShaderHash) =>
            drawBuffer(batchCount)
            doContextChange(
              d,
              atlasName,
              currentShader,
              currentShaderHash,
              customShaders,
              baseTransform
            )
            batchCount = 0
            atlasName = d.atlasName
            currentShader = d.shaderId
            currentShaderHash = d.shaderUniformData.map(_.uniformHash).mkString

          case d: DisplayObject =>
            updateData(d, batchCount)
            batchCount = batchCount + 1
            i += 1

          case c: DisplayCloneBatch =>
            cloneBlankDisplayObjects.get(c.id) match
              case None =>
                i += 1

              case Some(d) =>
                // Always clear down.
                drawBuffer(batchCount)
                doContextChange(
                  d,
                  atlasName,
                  currentShader,
                  currentShaderHash,
                  customShaders,
                  baseTransform
                )
                batchCount = 0
                atlasName = d.atlasName
                currentShader = d.shaderId
                currentShaderHash = d.shaderUniformData.map(_.uniformHash).mkString

                val numberProcessed: Int =
                  processCloneBatch(c, d)

                drawCloneBuffer(numberProcessed)

                batchCount = 0
                i += 1

          case c: DisplayCloneTiles =>
            cloneBlankDisplayObjects.get(c.id) match
              case None =>
                i += 1

              case Some(d) =>
                // Always clear down.
                drawBuffer(batchCount)
                doContextChange(
                  d,
                  atlasName,
                  currentShader,
                  currentShaderHash,
                  customShaders,
                  baseTransform
                )
                batchCount = 0
                atlasName = d.atlasName
                currentShader = d.shaderId
                currentShaderHash = d.shaderUniformData.map(_.uniformHash).mkString

                val numberProcessed: Int =
                  processCloneTiles(c, d)

                drawCloneTileBuffer(numberProcessed)

                batchCount = 0
                i += 1

          case t: DisplayText =>
            drawBuffer(batchCount)

            // Change context
            val shaderId = indigo.shared.shader.StandardShaders.Bitmap.id
            val activeShader: WebGLProgram =
              if (currentShader != shaderId) {
                customShaders.get(shaderId) match {
                  case Some(s) =>
                    currentProgram = s
                    setupShader(s)
                    s

                  case None =>
                    throw new Exception(
                      s"(TextBox) Missing entity shader '$shaderId'. Have you remembered to add the shader to the boot sequence or disabled auto-loading of default shaders?"
                    )
                }
              } else currentProgram
            //

            // Base transform
            setBaseTransform(baseTransform)

            // UBO data
            val buff = customDataUBOBuffers.getOrElseUpdate("FILLTYPE0", gl2.createBuffer())
            WebGLHelper.attachUBOData(gl2, Array[Float](0), buff)
            WebGLHelper.bindUBO(
              gl2,
              activeShader,
              "IndigoBitmapData",
              RendererWebGL2Constants.customDataBlockOffsetPointer,
              buff
            )
            //

            gl2.bindTexture(TEXTURE_2D, textTexture)
            gl2.texImage2D(
              TEXTURE_2D,
              0,
              WebGLRenderingContext.RGBA,
              WebGLRenderingContext.RGBA,
              UNSIGNED_BYTE,
              dynamicText.makeTextImageData(t.text, t.style, t.width, t.height)
            )

            updateTextData(t, 0)
            batchCount = 1
            atlasName = None
            currentShaderHash = ""
            i += 1
        }
  }

  private def uploadRefUBO(refDisplayObject: DisplayObject): Unit =
    val refData: Array[Float] =
      Array(
        refDisplayObject.refX,
        refDisplayObject.refY,
        refDisplayObject.flipX,
        refDisplayObject.flipY,
        refDisplayObject.width,
        refDisplayObject.height,
        refDisplayObject.frameScaleX,
        refDisplayObject.frameScaleY,
        refDisplayObject.channelOffset0X,
        refDisplayObject.channelOffset0Y,
        refDisplayObject.channelOffset1X,
        refDisplayObject.channelOffset1Y,
        refDisplayObject.channelOffset2X,
        refDisplayObject.channelOffset2Y,
        refDisplayObject.channelOffset3X,
        refDisplayObject.channelOffset3Y,
        refDisplayObject.textureWidth,
        refDisplayObject.textureHeight,
        refDisplayObject.atlasWidth,
        refDisplayObject.atlasHeight
      )
    WebGLHelper.attachUBOData(gl2, refData, cloneReferenceUBOBuffer)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private def processCloneBatch(
      c: DisplayCloneBatch,
      refDisplayObject: DisplayObject
  ): Int = {
    uploadRefUBO(refDisplayObject)

    val dataLength: Int = CloneBatchData.dataLength
    val count: Int      = c.cloneData.size
    var i: Int          = 0

    while (i < count) {
      updateCloneData(
        i,
        c.cloneData.toArray((i * dataLength) + 0),
        c.cloneData.toArray((i * dataLength) + 1),
        c.cloneData.toArray((i * dataLength) + 2),
        c.cloneData.toArray((i * dataLength) + 3),
        c.cloneData.toArray((i * dataLength) + 4)
      )

      i += 1
    }

    count
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private def processCloneTiles(
      c: DisplayCloneTiles,
      refDisplayObject: DisplayObject
  ): Int = {
    uploadRefUBO(refDisplayObject)

    val dataLength: Int = CloneTileData.dataLength
    val count: Int      = c.cloneData.size
    var i: Int          = 0

    while (i < count) {
      val cropX           = c.cloneData.toArray((i * dataLength) + 5)
      val cropY           = c.cloneData.toArray((i * dataLength) + 6)
      val cropWidth       = c.cloneData.toArray((i * dataLength) + 7)
      val cropHeight      = c.cloneData.toArray((i * dataLength) + 8)
      val frameScaleX     = cropWidth / refDisplayObject.atlasWidth
      val frameScaleY     = cropHeight / refDisplayObject.atlasHeight
      val channelOffset0X = frameScaleX * ((cropX + refDisplayObject.textureX) / cropWidth)
      val channelOffset0Y = frameScaleY * ((cropY + refDisplayObject.textureY) / cropHeight)

      updateCloneTileData(
        i = i,
        x = c.cloneData.toArray((i * dataLength) + 0),
        y = c.cloneData.toArray((i * dataLength) + 1),
        rotation = c.cloneData.toArray((i * dataLength) + 2),
        scaleX = c.cloneData.toArray((i * dataLength) + 3),
        scaleY = c.cloneData.toArray((i * dataLength) + 4),
        frameScaleX = frameScaleX,
        frameScaleY = frameScaleY,
        channelOffset0X = channelOffset0X,
        channelOffset0Y = channelOffset0Y,
        channelOffset1X = channelOffset0X + (refDisplayObject.channelOffset1X - refDisplayObject.channelOffset0X),
        channelOffset1Y = channelOffset0Y + (refDisplayObject.channelOffset1Y - refDisplayObject.channelOffset0Y),
        channelOffset2X = channelOffset0X + (refDisplayObject.channelOffset2X - refDisplayObject.channelOffset0X),
        channelOffset2Y = channelOffset0Y + (refDisplayObject.channelOffset2Y - refDisplayObject.channelOffset0Y),
        channelOffset3X = channelOffset0X + (refDisplayObject.channelOffset3X - refDisplayObject.channelOffset0X),
        channelOffset3Y = channelOffset0Y + (refDisplayObject.channelOffset3Y - refDisplayObject.channelOffset0Y)
      )

      i += 1
    }

    count
  }

}
