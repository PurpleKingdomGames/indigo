package indigo.platform.renderer.webgl2

import indigo.facades.WebGL2RenderingContext
import indigo.platform.assets.AtlasId
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.WebGLHelper
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneTiles
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayGroup
import indigo.shared.display.DisplayMutants
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayTextLetters
import indigo.shared.scenegraph.CloneBatchData
import indigo.shared.scenegraph.CloneId
import indigo.shared.shader.ShaderId
import org.scalajs.dom
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.*

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

class LayerRenderer(
    gl2: WebGL2RenderingContext,
    textureLocations: js.Array[TextureLookupResult],
    maxBatchSize: Int,
    projectionUBOBuffer: => WebGLBuffer,
    frameDataUBOBuffer: => WebGLBuffer,
    cloneReferenceUBOBuffer: => WebGLBuffer,
    lightDataUBOBuffer: => WebGLBuffer
) {

  private val customDataUBOBuffers: js.Dictionary[WebGLBuffer] =
    js.Dictionary.empty[WebGLBuffer]

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
  private val translateScaleData: js.Array[Float]       = js.Array[Float](4f * maxBatchSize)
  private val refFlipData: js.Array[Float]              = js.Array[Float](4f * maxBatchSize)
  private val sizeAndFrameScaleData: js.Array[Float]    = js.Array[Float](4f * maxBatchSize)
  private val channelOffsets01Data: js.Array[Float]     = js.Array[Float](4f * maxBatchSize)
  private val channelOffsets23Data: js.Array[Float]     = js.Array[Float](4f * maxBatchSize)
  private val textureSizeAtlasSizeData: js.Array[Float] = js.Array[Float](4f * maxBatchSize)
  private val rotationData: js.Array[Float]             = js.Array[Float](1f * maxBatchSize)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var lastRenderMode: Int = 0

  inline private def bindData(buffer: WebGLBuffer, data: js.Array[Float]): Unit = {
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
      clone: CloneBatchData
  ): Unit = {
    translateScaleData((i * 4) + 0) = clone.x.toFloat
    translateScaleData((i * 4) + 1) = clone.y.toFloat
    translateScaleData((i * 4) + 2) = clone.scaleX.toFloat
    translateScaleData((i * 4) + 3) = clone.scaleY.toFloat

    rotationData(i) = clone.rotation.toFloat
  }

  inline private def updateCloneTileData(
      i: Int,
      x: Float,
      y: Float,
      rotation: Float,
      scaleX: Float,
      scaleY: Float,
      width: Float,
      height: Float,
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

    sizeAndFrameScaleData((i * 4) + 0) = width
    sizeAndFrameScaleData((i * 4) + 1) = height
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

  private val refData: js.Array[Float] =
    js.Array(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
      0.0f, 0.0f)

  def init(): LayerRenderer =
    // pre-populate array
    WebGLHelper.attachUBOData(gl2, refData, cloneReferenceUBOBuffer)

    this

  def requiresContextChange(
      d: DisplayObject,
      atlasName: Option[AtlasId],
      currentShader: ShaderId,
      currentUniformHash: js.Array[String]
  ): Boolean = {
    val uniformHash: js.Array[String] = d.shaderUniformData.map(_.uniformHash)

    d.shaderId != currentShader ||
    (uniformHash.nonEmpty && (uniformHash.length != currentUniformHash.length ||
      !uniformHash.sameElements(currentUniformHash))) ||
    d.atlasName != atlasName ||
    lastRenderMode != 0
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var currentProgram: WebGLProgram                           = null
  private given CanEqual[Option[WebGLProgram], Option[WebGLProgram]] = CanEqual.derived

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def setBaseTransform(baseTransform: CheapMatrix4): Unit =
    if currentProgram == null then ()
    else
      gl2.uniformMatrix4fv(
        location = gl2.getUniformLocation(currentProgram, "u_baseTransform"),
        transpose = false,
        value = Float32Array(baseTransform.toJSArray)
      )

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
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
      currentUniformHash: js.Array[String],
      customShaders: js.Dictionary[WebGLProgram],
      baseTransform: CheapMatrix4,
      renderMode: Int
  ): Unit = {
    val shaderHasChanged: Boolean = d.shaderId != currentShader

    // Switch and reference shader
    val activeShader: WebGLProgram =
      if shaderHasChanged then
        try {
          currentProgram = customShaders(d.shaderId.toString)
          setupShader(currentProgram)
          currentProgram
        } catch {
          case _: Throwable =>
            throw new Exception(
              s"Missing entity shader '${d.shaderId}'. Have you remembered to add the shader to the boot sequence or disabled auto-loading of default shaders?"
            )
        }
      else currentProgram

    // Base transform
    if shaderHasChanged then setBaseTransform(baseTransform)
    if shaderHasChanged || lastRenderMode != renderMode then
      lastRenderMode = renderMode
      setMode(renderMode)

    // UBO data
    if d.shaderUniformData.nonEmpty then
      d.shaderUniformData.zipWithIndex.foreach { case (ud, i) =>
        currentUniformHash.lift(i) match
          case Some(hash) if !shaderHasChanged && hash == ud.uniformHash =>
            // This data has already been set on the shader.
            ()

          case _ =>
            val buff = customDataUBOBuffers.getOrElseUpdate(ud.blockName, gl2.createBuffer())

            WebGLHelper.attachUBOData(gl2, ud.data, buff)
            WebGLHelper.bindUBO(
              gl2,
              activeShader,
              RendererWebGL2Constants.customDataBlockOffsetPointer + i,
              buff,
              gl2.getUniformBlockIndex(activeShader, ud.blockName)
            )
      }

    // Atlas
    if d.atlasName != atlasName then
      d.atlasName.flatMap { nextAtlas =>
        textureLocations.find(t => t.name == nextAtlas)
      } match
        case None =>
          ()

        case Some(textureLookup) =>
          gl2.bindTexture(TEXTURE_2D, textureLookup.texture)

    ()
  }

  def setupShader(program: WebGLProgram): Unit = {

    gl2.useProgram(program)

    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.projectionBlockPointer,
      projectionUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoProjectionData")
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.frameDataBlockPointer,
      frameDataUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoFrameData")
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.cloneReferenceDataBlockPointer,
      cloneReferenceUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoCloneReferenceData")
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.lightDataBlockPointer,
      lightDataUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoDynamicLightingData")
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
    gl2.disableVertexAttribArray(2)
    gl2.disableVertexAttribArray(3)
    gl2.disableVertexAttribArray(4)
    gl2.disableVertexAttribArray(5)
    gl2.disableVertexAttribArray(6)

  def enableCloneTileMode(): Unit =
    gl2.disableVertexAttribArray(2)
    gl2.enableVertexAttribArray(3)
    gl2.enableVertexAttribArray(4)
    gl2.enableVertexAttribArray(5)
    gl2.disableVertexAttribArray(6)

  def disableCloneMode(): Unit =
    gl2.enableVertexAttribArray(2)
    gl2.enableVertexAttribArray(3)
    gl2.enableVertexAttribArray(4)
    gl2.enableVertexAttribArray(5)
    gl2.enableVertexAttribArray(6)

  def drawBuffer(instanceCount: Int): Unit =
    if instanceCount > 0 then
      disableCloneMode()

      bindData(translateScaleInstanceArray, translateScaleData)
      bindData(refFlipInstanceArray, refFlipData)
      bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
      bindData(channelOffsets01InstanceArray, channelOffsets01Data)
      bindData(channelOffsets23InstanceArray, channelOffsets23Data)
      bindData(textureSizeAtlasSizeInstanceArray, textureSizeAtlasSizeData)
      bindData(rotationInstanceArray, rotationData)

      gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)

  def drawCloneBuffer(instanceCount: Int): Unit =
    if instanceCount > 0 then
      enableCloneBatchMode()

      bindData(translateScaleInstanceArray, translateScaleData)
      bindData(rotationInstanceArray, rotationData)

      gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)

  def drawCloneTileBuffer(instanceCount: Int): Unit =
    if instanceCount > 0 then
      enableCloneTileMode()

      bindData(translateScaleInstanceArray, translateScaleData)
      bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
      bindData(channelOffsets01InstanceArray, channelOffsets01Data)
      bindData(channelOffsets23InstanceArray, channelOffsets23Data)
      bindData(rotationInstanceArray, rotationData)

      gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)

  def prepareCloneProgramBuffer(): Unit =
    disableCloneMode()
    bindData(translateScaleInstanceArray, translateScaleData)
    bindData(refFlipInstanceArray, refFlipData)
    bindData(sizeAndFrameScaleInstanceArray, sizeAndFrameScaleData)
    bindData(channelOffsets01InstanceArray, channelOffsets01Data)
    bindData(channelOffsets23InstanceArray, channelOffsets23Data)
    bindData(textureSizeAtlasSizeInstanceArray, textureSizeAtlasSizeData)
    bindData(rotationInstanceArray, rotationData)

  def drawSingleCloneProgram(): Unit =
    gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, 1)

  def drawLayer(
      cloneBlankDisplayObjects: => js.Dictionary[DisplayObject],
      displayEntities: => js.Array[DisplayEntity],
      frameBufferComponents: FrameBufferComponents,
      clearColor: RGBA,
      customShaders: => js.Dictionary[WebGLProgram]
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor, true)
    gl2.drawBuffers(frameBufferComponents.colorAttachments)

    gl2.activeTexture(TEXTURE0);

    renderEntities(cloneBlankDisplayObjects, displayEntities, customShaders, CheapMatrix4.identity)
  }

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.null",
      "scalafix:DisableSyntax.while",
      "scalafix:DisableSyntax.var"
    )
  )
  private def renderEntities(
      cloneBlankDisplayObjects: => js.Dictionary[DisplayObject],
      displayEntities: => js.Array[DisplayEntity],
      customShaders: => js.Dictionary[WebGLProgram],
      baseTransform: CheapMatrix4
  ): Unit = {

    val count: Int                          = displayEntities.length
    var i: Int                              = 0
    var batchCount: Int                     = 0
    var atlasName: Option[AtlasId]          = None
    var currentShader: ShaderId             = ShaderId("")
    var currentShaderHash: js.Array[String] = new js.Array()

    // Clones
    var currentCloneId: CloneId        = CloneId("")
    var currentCloneRef: DisplayObject = null

    while (i <= count)
      if i == count then
        drawBuffer(batchCount)
        i += 1
      else if batchCount == maxBatchSize then
        drawBuffer(batchCount)
        batchCount = 0
      else
        displayEntities(i) match {
          case d: DisplayTextLetters if d.letters.isEmpty =>
            i += 1

          case d: DisplayTextLetters =>
            drawBuffer(batchCount)
            batchCount = 0
            atlasName = None
            currentShader = ShaderId("")
            currentShaderHash = new js.Array()
            renderEntities(cloneBlankDisplayObjects, d.letters, customShaders, baseTransform)
            i += 1

          case d: DisplayGroup if d.entities.isEmpty =>
            i += 1

          case d: DisplayGroup =>
            drawBuffer(batchCount)
            batchCount = 0
            atlasName = None
            currentShader = ShaderId("")
            currentShaderHash = new js.Array()
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
              baseTransform,
              0
            )
            batchCount = 0
            atlasName = d.atlasName
            currentShader = d.shaderId
            currentShaderHash = d.shaderUniformData.map(_.uniformHash)

          case d: DisplayObject =>
            updateData(d, batchCount)
            batchCount = batchCount + 1
            i += 1

          case c: DisplayCloneBatch =>
            drawBuffer(batchCount)

            var cloneBlankExists = false
            var refreshCloneUBO  = false

            if c.id.toString != currentCloneId.toString then
              cloneBlankDisplayObjects.get(c.id.toString) match
                case None => ()
                case Some(d) =>
                  currentCloneId = c.id
                  currentCloneRef = d
                  cloneBlankExists = true
                  refreshCloneUBO = true
            else cloneBlankExists = true

            if cloneBlankExists then
              doContextChange(
                currentCloneRef,
                atlasName,
                currentShader,
                currentShaderHash,
                customShaders,
                baseTransform,
                1
              )

              if refreshCloneUBO || currentShader != currentCloneRef.shaderId then uploadRefUBO(currentCloneRef)

              val numberProcessed: Int =
                processCloneBatch(c)

              drawCloneBuffer(numberProcessed)

              batchCount = 0
              atlasName = currentCloneRef.atlasName
              currentShader = currentCloneRef.shaderId
              currentShaderHash = currentCloneRef.shaderUniformData.map(_.uniformHash)

            i += 1

          case c: DisplayCloneTiles =>
            drawBuffer(batchCount)

            var cloneBlankExists = false
            var refreshCloneUBO  = false

            if c.id.toString != currentCloneId.toString then
              cloneBlankDisplayObjects.get(c.id.toString) match
                case None => ()
                case Some(d) =>
                  currentCloneId = c.id
                  currentCloneRef = d
                  cloneBlankExists = true
                  refreshCloneUBO = true
            else cloneBlankExists = true

            if cloneBlankExists then
              doContextChange(
                currentCloneRef,
                atlasName,
                currentShader,
                currentShaderHash,
                customShaders,
                baseTransform,
                2
              )

              if refreshCloneUBO || currentShader != currentCloneRef.shaderId then uploadRefUBO(currentCloneRef)

              val numberProcessed: Int =
                processCloneTiles(c, currentCloneRef)

              drawCloneTileBuffer(numberProcessed)

              batchCount = 0
              atlasName = currentCloneRef.atlasName
              currentShader = currentCloneRef.shaderId
              currentShaderHash = currentCloneRef.shaderUniformData.map(_.uniformHash)

            i += 1

          case c: DisplayMutants =>
            drawBuffer(batchCount)

            var cloneBlankExists = false

            if c.id.toString != currentCloneId.toString then
              cloneBlankDisplayObjects.get(c.id.toString) match
                case None => ()
                case Some(d) =>
                  currentCloneId = c.id
                  currentCloneRef = d
                  cloneBlankExists = true
            else cloneBlankExists = true

            if cloneBlankExists &&
              requiresContextChange(currentCloneRef, atlasName, currentShader, currentShaderHash)
            then
              doContextChange(
                currentCloneRef,
                atlasName,
                currentShader,
                currentShaderHash,
                customShaders,
                baseTransform,
                0
              )

              processMutants(c, currentCloneRef, currentProgram)

              batchCount = 0
              atlasName = currentCloneRef.atlasName
              currentShader = currentCloneRef.shaderId
              currentShaderHash = currentCloneRef.shaderUniformData.map(_.uniformHash)

            i += 1
        }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentRefUBOHash: Int = -1
  private def uploadRefUBO(refDisplayObject: DisplayObject): Unit =
    val code = refDisplayObject.hashCode
    if currentRefUBOHash == code then ()
    else
      val refData: js.Array[Float] =
        js.Array(
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
      currentRefUBOHash = code

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  private def processCloneBatch(c: DisplayCloneBatch): Int = {
    val count: Int = c.cloneData.length
    var i: Int     = 0

    while (i < count) {
      updateCloneData(
        i,
        c.cloneData(i)
      )

      i += 1
    }

    count
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  private def processCloneTiles(
      c: DisplayCloneTiles,
      refDisplayObject: DisplayObject
  ): Int = {
    val count: Int = c.cloneData.length
    var i: Int     = 0

    val atlasWidth  = refDisplayObject.atlasWidth
    val atlasHeight = refDisplayObject.atlasHeight
    val textureX    = refDisplayObject.textureX
    val textureY    = refDisplayObject.textureY
    val c1X         = refDisplayObject.channelOffset1X - refDisplayObject.channelOffset0X
    val c1Y         = refDisplayObject.channelOffset1Y - refDisplayObject.channelOffset0Y
    val c2X         = refDisplayObject.channelOffset2X - refDisplayObject.channelOffset0X
    val c2Y         = refDisplayObject.channelOffset2Y - refDisplayObject.channelOffset0Y
    val c3X         = refDisplayObject.channelOffset3X - refDisplayObject.channelOffset0X
    val c3Y         = refDisplayObject.channelOffset3Y - refDisplayObject.channelOffset0Y

    while (i < count) {
      val clone           = c.cloneData(i)
      val cropWidth       = clone.cropWidth
      val cropHeight      = clone.cropHeight
      val frameScaleX     = cropWidth / atlasWidth
      val frameScaleY     = cropHeight / atlasHeight
      val channelOffset0X = frameScaleX * ((clone.cropX + textureX) / cropWidth)
      val channelOffset0Y = frameScaleY * ((clone.cropY + textureY) / cropHeight)

      updateCloneTileData(
        i = i,
        x = clone.x.toFloat,
        y = clone.y.toFloat,
        rotation = clone.rotation.toFloat,
        scaleX = clone.scaleX.toFloat,
        scaleY = clone.scaleY.toFloat,
        width = cropWidth.toFloat,
        height = cropHeight.toFloat,
        frameScaleX = frameScaleX,
        frameScaleY = frameScaleY,
        channelOffset0X = channelOffset0X,
        channelOffset0Y = channelOffset0Y,
        channelOffset1X = channelOffset0X + c1X,
        channelOffset1Y = channelOffset0Y + c1Y,
        channelOffset2X = channelOffset0X + c2X,
        channelOffset2Y = channelOffset0Y + c2Y,
        channelOffset3X = channelOffset0X + c3X,
        channelOffset3Y = channelOffset0Y + c3Y
      )

      i += 1
    }

    count
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  private def processMutants(
      c: DisplayMutants,
      refDisplayObject: DisplayObject,
      activeShader: WebGLProgram
  ): Unit =
    if (c.cloneData.length > 0) {
      updateData(refDisplayObject, 0)
      prepareCloneProgramBuffer()

      val count: Int                              = c.cloneData.length
      var i: Int                                  = 0
      var currentUniformHash: js.Array[String]    = new js.Array()
      val blockIndexLookup: js.Dictionary[Double] = js.Dictionary()

      while (i < count) {
        val shaderUniformData = c.cloneData(i)

        // UBO data
        if shaderUniformData.nonEmpty then
          shaderUniformData.zipWithIndex.foreach { case (ud, i) =>
            currentUniformHash.lift(i) match
              case Some(hash) if hash == ud.uniformHash =>
                // This data has already been set on the shader.
                ()

              case _ =>
                val blockName = ud.blockName

                val buff = customDataUBOBuffers.getOrElseUpdate(blockName, gl2.createBuffer())

                val blockIndex: Double =
                  blockIndexLookup.get(blockName) match
                    case Some(idx) => idx
                    case None =>
                      val idx = gl2.getUniformBlockIndex(activeShader, blockName)
                      blockIndexLookup.update(blockName, idx)
                      idx

                WebGLHelper.attachUBOData(gl2, ud.data, buff)
                WebGLHelper.bindUBO(
                  gl2,
                  activeShader,
                  RendererWebGL2Constants.customDataBlockOffsetPointer + i,
                  buff,
                  blockIndex
                )
          }
          currentUniformHash = shaderUniformData.map(_.uniformHash)

        drawSingleCloneProgram()

        i += 1
      }
    }

}
