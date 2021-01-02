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

class RendererLayer(gl2: WebGL2RenderingContext, textureLocations: List[TextureLookupResult], maxBatchSize: Int) {

  // Instance Array Buffers
  private val transformInstanceArray: WebGLBuffer                = gl2.createBuffer()
  private val frameTransformInstanceArray: WebGLBuffer           = gl2.createBuffer()
  private val dimensionsInstanceArray: WebGLBuffer               = gl2.createBuffer()
  private val tintInstanceArray: WebGLBuffer                     = gl2.createBuffer()
  private val gradiantOverlayPositionsInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val gradiantOverlayFromColorInstanceArray: WebGLBuffer = gl2.createBuffer()
  private val gradiantOverlayToColorInstanceArray: WebGLBuffer   = gl2.createBuffer()
  private val borderColorInstanceArray: WebGLBuffer              = gl2.createBuffer()
  private val glowColorInstanceArray: WebGLBuffer                = gl2.createBuffer()
  private val amountsInstanceArray: WebGLBuffer                  = gl2.createBuffer()
  private val rotationAlphaFlipHFlipVInstanceArray: WebGLBuffer  = gl2.createBuffer()
  private val emissiveNormalOffsetsArray: WebGLBuffer            = gl2.createBuffer()
  private val specularOffsetIsLitArray: WebGLBuffer              = gl2.createBuffer()
  private val textureAmountsArray: WebGLBuffer                   = gl2.createBuffer()

  def setupInstanceArray(buffer: WebGLBuffer, location: Int, size: Int): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.enableVertexAttribArray(location)
    gl2.vertexAttribPointer(location, size, FLOAT, false, size * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(location, 1)
  }

  // Instance Data Arrays
  private val transformData: scalajs.js.Array[Float]                = scalajs.js.Array[Float](4f * maxBatchSize)
  private val frameTransformData: scalajs.js.Array[Float]           = scalajs.js.Array[Float](4f * maxBatchSize)
  private val dimensionsData: scalajs.js.Array[Float]               = scalajs.js.Array[Float](4f * maxBatchSize)
  private val tintData: scalajs.js.Array[Float]                     = scalajs.js.Array[Float](4f * maxBatchSize)
  private val gradiantOverlayPositionsData: scalajs.js.Array[Float] = scalajs.js.Array[Float](4f * maxBatchSize)
  private val gradiantOverlayFromColorData: scalajs.js.Array[Float] = scalajs.js.Array[Float](4f * maxBatchSize)
  private val gradiantOverlayToColorData: scalajs.js.Array[Float]   = scalajs.js.Array[Float](4f * maxBatchSize)
  private val borderColorData: scalajs.js.Array[Float]              = scalajs.js.Array[Float](4f * maxBatchSize)
  private val glowColorData: scalajs.js.Array[Float]                = scalajs.js.Array[Float](4f * maxBatchSize)
  private val amountsData: scalajs.js.Array[Float]                  = scalajs.js.Array[Float](4f * maxBatchSize)
  private val rotationAlphaFlipHFlipVData: scalajs.js.Array[Float]  = scalajs.js.Array[Float](4f * maxBatchSize)
  private val emissiveNormalOffsetsData: scalajs.js.Array[Float]    = scalajs.js.Array[Float](4f * maxBatchSize)
  private val specularOffsetIsLitData: scalajs.js.Array[Float]      = scalajs.js.Array[Float](4f * maxBatchSize)
  private val textureAmountsData: scalajs.js.Array[Float]           = scalajs.js.Array[Float](4f * maxBatchSize)

  @inline private def bindData(buffer: WebGLBuffer, data: scalajs.js.Array[Float]): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(data), STATIC_DRAW)
  }

  private def updateData(d: DisplayObject, i: Int): Unit = {
    transformData((i * 4) + 0) = d.x
    transformData((i * 4) + 1) = d.y
    transformData((i * 4) + 2) = d.scaleX
    transformData((i * 4) + 3) = d.scaleY

    frameTransformData((i * 4) + 0) = d.frameX
    frameTransformData((i * 4) + 1) = d.frameY
    frameTransformData((i * 4) + 2) = d.frameScaleX
    frameTransformData((i * 4) + 3) = d.frameScaleY

    dimensionsData((i * 4) + 0) = d.refX
    dimensionsData((i * 4) + 1) = d.refY
    dimensionsData((i * 4) + 2) = d.width
    dimensionsData((i * 4) + 3) = d.height

    //Effects
    tintData((i * 4) + 0) = d.effects.tint(0)
    tintData((i * 4) + 1) = d.effects.tint(1)
    tintData((i * 4) + 2) = d.effects.tint(2)
    tintData((i * 4) + 3) = d.effects.tint(3)

    gradiantOverlayPositionsData((i * 4) + 0) = d.effects.gradiantOverlayPositions(0)
    gradiantOverlayPositionsData((i * 4) + 1) = d.effects.gradiantOverlayPositions(1)
    gradiantOverlayPositionsData((i * 4) + 2) = d.effects.gradiantOverlayPositions(2)
    gradiantOverlayPositionsData((i * 4) + 3) = d.effects.gradiantOverlayPositions(3)

    gradiantOverlayFromColorData((i * 4) + 0) = d.effects.gradiantOverlayFromColor(0)
    gradiantOverlayFromColorData((i * 4) + 1) = d.effects.gradiantOverlayFromColor(1)
    gradiantOverlayFromColorData((i * 4) + 2) = d.effects.gradiantOverlayFromColor(2)
    gradiantOverlayFromColorData((i * 4) + 3) = d.effects.gradiantOverlayFromColor(3)

    gradiantOverlayToColorData((i * 4) + 0) = d.effects.gradiantOverlayToColor(0)
    gradiantOverlayToColorData((i * 4) + 1) = d.effects.gradiantOverlayToColor(1)
    gradiantOverlayToColorData((i * 4) + 2) = d.effects.gradiantOverlayToColor(2)
    gradiantOverlayToColorData((i * 4) + 3) = d.effects.gradiantOverlayToColor(3)

    borderColorData((i * 4) + 0) = d.effects.borderColor(0)
    borderColorData((i * 4) + 1) = d.effects.borderColor(1)
    borderColorData((i * 4) + 2) = d.effects.borderColor(2)
    borderColorData((i * 4) + 3) = d.effects.borderColor(3)

    glowColorData((i * 4) + 0) = d.effects.glowColor(0)
    glowColorData((i * 4) + 1) = d.effects.glowColor(1)
    glowColorData((i * 4) + 2) = d.effects.glowColor(2)
    glowColorData((i * 4) + 3) = d.effects.glowColor(3)

    amountsData((i * 4) + 0) = d.effects.outerBorderAmount
    amountsData((i * 4) + 1) = d.effects.innerBorderAmount
    amountsData((i * 4) + 2) = d.effects.outerGlowAmount
    amountsData((i * 4) + 3) = d.effects.innerGlowAmount

    rotationAlphaFlipHFlipVData((i * 4) + 0) = d.rotation
    rotationAlphaFlipHFlipVData((i * 4) + 1) = d.effects.alpha
    rotationAlphaFlipHFlipVData((i * 4) + 2) = d.flipHorizontal
    rotationAlphaFlipHFlipVData((i * 4) + 3) = d.flipVertical

    emissiveNormalOffsetsData((i * 4) + 0) = d.emissiveOffset.x.toFloat
    emissiveNormalOffsetsData((i * 4) + 1) = d.emissiveOffset.y.toFloat
    emissiveNormalOffsetsData((i * 4) + 2) = d.normalOffset.x.toFloat
    emissiveNormalOffsetsData((i * 4) + 3) = d.normalOffset.y.toFloat

    specularOffsetIsLitData((i * 4) + 0) = d.specularOffset.x.toFloat
    specularOffsetIsLitData((i * 4) + 1) = d.specularOffset.y.toFloat
    specularOffsetIsLitData((i * 4) + 2) = d.isLit
    specularOffsetIsLitData((i * 4) + 3) = 1.0f

    textureAmountsData((i * 4) + 0) = d.albedoAmount
    textureAmountsData((i * 4) + 1) = d.emissiveAmount
    textureAmountsData((i * 4) + 2) = d.normalAmount
    textureAmountsData((i * 4) + 3) = d.specularAmount
  }

  private def overwriteFromDisplayBatchClone(cloneData: DisplayCloneBatchData, i: Int): Unit = {
    transformData((i * 4) + 0) = cloneData.x
    transformData((i * 4) + 1) = cloneData.y
    transformData((i * 4) + 2) = cloneData.scaleX
    transformData((i * 4) + 3) = cloneData.scaleY

    rotationAlphaFlipHFlipVData((i * 4) + 0) = cloneData.rotation
    rotationAlphaFlipHFlipVData((i * 4) + 1) = cloneData.alpha
    rotationAlphaFlipHFlipVData((i * 4) + 2) = cloneData.flipHorizontal
    rotationAlphaFlipHFlipVData((i * 4) + 3) = cloneData.flipVertical
  }

  // @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def drawLayer(
      projection: scalajs.js.Array[Double],
      cloneBlankDisplayObjects: Map[String, DisplayObject],
      displayEntities: ListBuffer[DisplayEntity],
      frameBufferComponents: FrameBufferComponents,
      clearColor: RGBA,
      shaderProgram: WebGLProgram
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor)
    gl2.drawBuffers(frameBufferComponents.colorAttachments)

    gl2.useProgram(shaderProgram)

    val textureLocation = gl2.getUniformLocation(shaderProgram, "u_textureDiffuse")
    gl2.uniform1i(textureLocation, 0)

    // Projection
    val projectionLocation = gl2.getUniformLocation(shaderProgram, "u_projection")
    gl2.uniformMatrix4fv(projectionLocation, false, projection)

    // Instance attributes
    // vec4 a_transform
    setupInstanceArray(transformInstanceArray, 1, 4)
    // vec2 a_frameTransform
    setupInstanceArray(frameTransformInstanceArray, 2, 4)
    // float a_dimensions
    setupInstanceArray(dimensionsInstanceArray, 3, 4)
    // vec4 a_tint
    setupInstanceArray(tintInstanceArray, 4, 4)
    // vec4 a_gradiantPositions
    setupInstanceArray(gradiantOverlayPositionsInstanceArray, 5, 4)
    // vec4 a_gradiantOverlayFromColor
    setupInstanceArray(gradiantOverlayFromColorInstanceArray, 6, 4)
    // vec4 a_gradiantOverlayToColor
    setupInstanceArray(gradiantOverlayToColorInstanceArray, 7, 4)
    // vec4 a_borderColor
    setupInstanceArray(borderColorInstanceArray, 8, 4)
    // vec4 a_glowColor
    setupInstanceArray(glowColorInstanceArray, 9, 4)
    // vec4 a_amounts
    setupInstanceArray(amountsInstanceArray, 10, 4)
    // vec4 a_rotationAlphaFlipHFlipV --
    setupInstanceArray(rotationAlphaFlipHFlipVInstanceArray, 11, 4)
    // vec4 a_emissiveNormalOffsets --
    setupInstanceArray(emissiveNormalOffsetsArray, 12, 4)
    // vec4 a_specularOffsetIsLit --
    setupInstanceArray(specularOffsetIsLitArray, 13, 4)
    // vec4 a_textureAmounts --
    setupInstanceArray(textureAmountsArray, 14, 4)
    //

    val sorted: ListBuffer[DisplayEntity] =
      RendererHelper.sortByDepth(displayEntities)

    @inline def drawBuffer(instanceCount: Int): Unit =
      if (instanceCount > 0) {
        bindData(transformInstanceArray, transformData)
        bindData(frameTransformInstanceArray, frameTransformData)
        bindData(dimensionsInstanceArray, dimensionsData)
        bindData(tintInstanceArray, tintData)
        bindData(gradiantOverlayPositionsInstanceArray, gradiantOverlayPositionsData)
        bindData(gradiantOverlayFromColorInstanceArray, gradiantOverlayFromColorData)
        bindData(gradiantOverlayToColorInstanceArray, gradiantOverlayToColorData)
        bindData(borderColorInstanceArray, borderColorData)
        bindData(glowColorInstanceArray, glowColorData)
        bindData(amountsInstanceArray, amountsData)
        bindData(rotationAlphaFlipHFlipVInstanceArray, rotationAlphaFlipHFlipVData)
        bindData(emissiveNormalOffsetsArray, emissiveNormalOffsetsData)
        bindData(specularOffsetIsLitArray, specularOffsetIsLitData)
        bindData(textureAmountsArray, textureAmountsData)

        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
      }

    @tailrec
    def rec(remaining: List[DisplayEntity], batchCount: Int, atlasName: String): Unit =
      remaining match {
        case Nil =>
          drawBuffer(batchCount)

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

          rec(remaining, 0, d.atlasName)

        case _ if batchCount == maxBatchSize =>
          drawBuffer(batchCount)
          rec(remaining, 0, atlasName)

        case (d: DisplayObject) :: ds =>
          updateData(d, batchCount)
          rec(ds, batchCount + 1, atlasName)

        case (c: DisplayClone) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName)

            case Some(refDisplayObject) =>
              updateData(refDisplayObject, batchCount)
              overwriteFromDisplayBatchClone(DisplayClone.asBatchData(c), batchCount)
              rec(ds, batchCount + 1, atlasName)
          }

        case (c: DisplayCloneBatch) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, atlasName)

            case Some(refDisplayObject) =>
              val numberProcessed: Int =
                processCloneBatch(c, refDisplayObject, batchCount)

              rec(ds, batchCount + numberProcessed, atlasName)
          }

      }

    rec(sorted.toList, 0, "")

  }

  // @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.While"))
  private def processCloneBatch(c: DisplayCloneBatch, refDisplayObject: DisplayObject, batchCount: Int): Int = {

    val count: Int = c.clones.length
    var i: Int     = 0

    while (i < count) {
      updateData(refDisplayObject, batchCount + i)
      overwriteFromDisplayBatchClone(c.clones(i), batchCount + i)
      i += 1
    }

    count
  }

}
