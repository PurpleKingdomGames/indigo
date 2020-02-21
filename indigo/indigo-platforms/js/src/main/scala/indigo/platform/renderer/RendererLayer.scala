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
import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayClone
import indigo.shared.display.DisplayCloneBatch
import indigo.shared.display.DisplayCloneBatchData

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
  private val alphaInstanceArray: WebGLBuffer            = gl2.createBuffer()
  private val refInstanceArray: WebGLBuffer              = gl2.createBuffer()
  private val sizeInstanceArray: WebGLBuffer             = gl2.createBuffer()

  def setupInstanceArray(buffer: WebGLBuffer, location: Int, size: Int): Unit = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.enableVertexAttribArray(location)
    gl2.vertexAttribPointer(location, size, FLOAT, false, size * Float32Array.BYTES_PER_ELEMENT, 0)
    gl2.vertexAttribDivisor(location, 1)
  }

  // Instance Data Arrays
  private val translationData: scalajs.js.Array[Double]      = scalajs.js.Array[Double](2d * maxBatchSize)
  private val scaleData: scalajs.js.Array[Double]            = scalajs.js.Array[Double](2d * maxBatchSize)
  private val frameTranslationData: scalajs.js.Array[Double] = scalajs.js.Array[Double](2d * maxBatchSize)
  private val frameScaleData: scalajs.js.Array[Double]       = scalajs.js.Array[Double](2d * maxBatchSize)
  private val rotationData: scalajs.js.Array[Double]         = scalajs.js.Array[Double](1d * maxBatchSize)
  private val refData: scalajs.js.Array[Double]              = scalajs.js.Array[Double](2d * maxBatchSize)
  private val sizeData: scalajs.js.Array[Double]             = scalajs.js.Array[Double](2d * maxBatchSize)

  private val tintData: scalajs.js.Array[Double]                     = scalajs.js.Array[Double](4d * maxBatchSize)
  private val colorOverlayData: scalajs.js.Array[Double]             = scalajs.js.Array[Double](4d * maxBatchSize)
  private val gradiantOverlayFromData: scalajs.js.Array[Double]      = scalajs.js.Array[Double](2d * maxBatchSize)
  private val gradiantOverlayToData: scalajs.js.Array[Double]        = scalajs.js.Array[Double](2d * maxBatchSize)
  private val gradiantOverlayFromColorData: scalajs.js.Array[Double] = scalajs.js.Array[Double](4d * maxBatchSize)
  private val gradiantOverlayToColorData: scalajs.js.Array[Double]   = scalajs.js.Array[Double](4d * maxBatchSize)
  private val outerBorderColorData: scalajs.js.Array[Double]         = scalajs.js.Array[Double](4d * maxBatchSize)
  private val outerBorderAmountData: scalajs.js.Array[Double]        = scalajs.js.Array[Double](1d * maxBatchSize)
  private val innerBorderColorData: scalajs.js.Array[Double]         = scalajs.js.Array[Double](4d * maxBatchSize)
  private val innerBorderAmountData: scalajs.js.Array[Double]        = scalajs.js.Array[Double](1d * maxBatchSize)
  private val outerGlowColorData: scalajs.js.Array[Double]           = scalajs.js.Array[Double](4d * maxBatchSize)
  private val outerGlowAmountData: scalajs.js.Array[Double]          = scalajs.js.Array[Double](1d * maxBatchSize)
  private val innerGlowColorData: scalajs.js.Array[Double]           = scalajs.js.Array[Double](4d * maxBatchSize)
  private val innerGlowAmountData: scalajs.js.Array[Double]          = scalajs.js.Array[Double](1d * maxBatchSize)
  private val blurData: scalajs.js.Array[Double]                     = scalajs.js.Array[Double](1d * maxBatchSize)
  private val alphaData: scalajs.js.Array[Double]                    = scalajs.js.Array[Double](1d * maxBatchSize)
  private val hFlipData: scalajs.js.Array[Double]                    = scalajs.js.Array[Double](1d * maxBatchSize)
  private val vFlipData: scalajs.js.Array[Double]                    = scalajs.js.Array[Double](1d * maxBatchSize)

  @inline private def bindData(buffer: WebGLBuffer, data: scalajs.js.Array[Double]) = {
    gl2.bindBuffer(ARRAY_BUFFER, buffer)
    gl2.bufferData(ARRAY_BUFFER, new Float32Array(data), STATIC_DRAW)
  }

  private def updateData(d: DisplayObject, i: Int): Unit = {
    translationData((i * 2) + 0) = d.x
    translationData((i * 2) + 1) = d.y

    scaleData((i * 2) + 0) = d.scaleX
    scaleData((i * 2) + 1) = d.scaleY

    frameTranslationData((i * 2) + 0) = d.frameX
    frameTranslationData((i * 2) + 1) = d.frameY

    frameScaleData((i * 2) + 0) = d.frameScaleX
    frameScaleData((i * 2) + 1) = d.frameScaleY

    rotationData(i) = d.rotation

    refData((i * 2) + 0) = d.refX
    refData((i * 2) + 1) = d.refY

    sizeData((i * 2) + 0) = d.width
    sizeData((i * 2) + 1) = d.height

    //Effects
    tintData((i * 4) + 0) = d.effects.tint(0)
    tintData((i * 4) + 1) = d.effects.tint(1)
    tintData((i * 4) + 2) = d.effects.tint(2)
    tintData((i * 4) + 3) = d.effects.tint(3)

    colorOverlayData((i * 4) + 0) = d.effects.colorOverlay(0)
    colorOverlayData((i * 4) + 1) = d.effects.colorOverlay(1)
    colorOverlayData((i * 4) + 2) = d.effects.colorOverlay(2)
    colorOverlayData((i * 4) + 3) = d.effects.colorOverlay(3)

    gradiantOverlayFromData((i * 2) + 0) = d.effects.gradiantOverlayFrom(0)
    gradiantOverlayFromData((i * 2) + 1) = d.effects.gradiantOverlayFrom(1)

    gradiantOverlayToData((i * 2) + 0) = d.effects.gradiantOverlayTo(0)
    gradiantOverlayToData((i * 2) + 1) = d.effects.gradiantOverlayTo(1)

    gradiantOverlayFromColorData((i * 4) + 0) = d.effects.gradiantOverlayFromColor(0)
    gradiantOverlayFromColorData((i * 4) + 1) = d.effects.gradiantOverlayFromColor(1)
    gradiantOverlayFromColorData((i * 4) + 2) = d.effects.gradiantOverlayFromColor(2)
    gradiantOverlayFromColorData((i * 4) + 3) = d.effects.gradiantOverlayFromColor(3)

    gradiantOverlayToColorData((i * 4) + 0) = d.effects.gradiantOverlayToColor(0)
    gradiantOverlayToColorData((i * 4) + 1) = d.effects.gradiantOverlayToColor(1)
    gradiantOverlayToColorData((i * 4) + 2) = d.effects.gradiantOverlayToColor(2)
    gradiantOverlayToColorData((i * 4) + 3) = d.effects.gradiantOverlayToColor(3)

    outerBorderColorData((i * 4) + 0) = d.effects.outerBorderColor(0)
    outerBorderColorData((i * 4) + 1) = d.effects.outerBorderColor(1)
    outerBorderColorData((i * 4) + 2) = d.effects.outerBorderColor(2)
    outerBorderColorData((i * 4) + 3) = d.effects.outerBorderColor(3)
    outerBorderAmountData(i) = d.effects.outerBorderAmount

    innerBorderColorData((i * 4) + 0) = d.effects.innerBorderColor(0)
    innerBorderColorData((i * 4) + 1) = d.effects.innerBorderColor(1)
    innerBorderColorData((i * 4) + 2) = d.effects.innerBorderColor(2)
    innerBorderColorData((i * 4) + 3) = d.effects.innerBorderColor(3)
    innerBorderAmountData(i) = d.effects.innerBorderAmount

    outerGlowColorData((i * 4) + 0) = d.effects.outerGlowColor(0)
    outerGlowColorData((i * 4) + 1) = d.effects.outerGlowColor(1)
    outerGlowColorData((i * 4) + 2) = d.effects.outerGlowColor(2)
    outerGlowColorData((i * 4) + 3) = d.effects.outerGlowColor(3)
    outerGlowAmountData(i) = d.effects.outerGlowAmount

    innerGlowColorData((i * 4) + 0) = d.effects.innerGlowColor(0)
    innerGlowColorData((i * 4) + 1) = d.effects.innerGlowColor(1)
    innerGlowColorData((i * 4) + 2) = d.effects.innerGlowColor(2)
    innerGlowColorData((i * 4) + 3) = d.effects.innerGlowColor(3)
    innerGlowAmountData(i) = d.effects.innerGlowAmount

    blurData(i) = d.effects.blur

    alphaData(i) = d.effects.alpha

    hFlipData(i) = d.effects.flipHorizontal
    vFlipData(i) = d.effects.flipVertical
  }

  private def overwriteFromDisplayBatchClone(cloneData: DisplayCloneBatchData, i: Int): Unit = {
    translationData((i * 2) + 0) = cloneData.x
    translationData((i * 2) + 1) = cloneData.y
    scaleData((i * 2) + 0) = cloneData.scaleX
    scaleData((i * 2) + 1) = cloneData.scaleY
    rotationData(i) = cloneData.rotation
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def drawLayer(
      projection: scalajs.js.Array[Double],
      cloneBlankDisplayObjects: Map[String, DisplayObject],
      displayEntities: ListBuffer[DisplayEntity],
      frameBufferComponents: FrameBufferComponents,
      clearColor: ClearColor,
      shaderProgram: WebGLProgram,
      layer: CurrentDrawLayer,
      metrics: Metrics
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, clearColor)
    gl2.drawBuffers(frameBufferComponents.colorAttachments)

    gl2.useProgram(shaderProgram)

    val textureLocation1 = gl2.getUniformLocation(shaderProgram, "u_textureDiffuse")
    gl2.uniform1i(textureLocation1, 0)
    val textureLocation2 = gl2.getUniformLocation(shaderProgram, "u_textureEmission")
    gl2.uniform1i(textureLocation2, 1)
    val textureLocation3 = gl2.getUniformLocation(shaderProgram, "u_textureNormal")
    gl2.uniform1i(textureLocation3, 2)
    val textureLocation4 = gl2.getUniformLocation(shaderProgram, "u_textureSpecular")
    gl2.uniform1i(textureLocation4, 3)

    // Projection
    val projectionLocation = gl2.getUniformLocation(shaderProgram, "u_projection")
    gl2.uniformMatrix4fv(projectionLocation, false, projection)

    // Instance attributes
    // vec2 a_translation
    setupInstanceArray(translationInstanceArray, 2, 2)
    // vec2 a_scale
    setupInstanceArray(scaleInstanceArray, 3, 2)
    // vec2 a_frameTranslation
    setupInstanceArray(frameTranslationInstanceArray, 5, 2)
    // vec2 a_frameScale
    setupInstanceArray(frameScaleInstanceArray, 6, 2)
    // float a_rotation
    setupInstanceArray(rotationInstanceArray, 7, 1)
    // float a_ref
    setupInstanceArray(refInstanceArray, 11, 2)
    // float a_size
    setupInstanceArray(sizeInstanceArray, 12, 2)
    // vec4 a_tint
    setupInstanceArray(tintInstanceArray, 4, 4)
    // float a_alpha
    setupInstanceArray(alphaInstanceArray, 10, 1)
    // float a_fliph
    setupInstanceArray(hFlipInstanceArray, 8, 1)
    // float a_flipv
    setupInstanceArray(vFlipInstanceArray, 9, 1)
    //

    val sorted: ListBuffer[DisplayEntity] =
      RendererFunctions.sortByDepth(displayEntities)

    @inline def drawBuffer(instanceCount: Int): Unit =
      if (instanceCount > 0) {
        bindData(translationInstanceArray, translationData)
        bindData(scaleInstanceArray, scaleData)
        bindData(frameTranslationInstanceArray, frameTranslationData)
        bindData(frameScaleInstanceArray, frameScaleData)
        bindData(rotationInstanceArray, rotationData)
        bindData(refInstanceArray, refData)
        bindData(sizeInstanceArray, sizeData)
        bindData(tintInstanceArray, tintData)
        bindData(alphaInstanceArray, alphaData)
        bindData(hFlipInstanceArray, hFlipData)
        bindData(vFlipInstanceArray, vFlipData)

        gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, instanceCount)
        metrics.record(layer.metricDraw)
      }

    metrics.record(layer.metricStart)

    @tailrec
    def rec(remaining: List[DisplayEntity], batchCount: Int, textureHash: String): Unit =
      remaining match {
        case Nil =>
          drawBuffer(batchCount)

        case (d: DisplayObject) :: _ if d.textureHash !== textureHash =>
          drawBuffer(batchCount)

          // Diffuse
          textureLocations.find(t => t.name === d.diffuseRef) match {
            case None =>
              gl2.activeTexture(TEXTURE0);
              gl2.bindTexture(TEXTURE_2D, null)

            case Some(textureLookup) =>
              gl2.activeTexture(TEXTURE0);
              gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          // Emission
          textureLocations.find(t => t.name === d.emissionRef) match {
            case None =>
              gl2.activeTexture(TEXTURE1);
              gl2.bindTexture(TEXTURE_2D, null)

            case Some(textureLookup) =>
              gl2.activeTexture(TEXTURE1);
              gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          // Normal
          textureLocations.find(t => t.name === d.normalRef) match {
            case None =>
              gl2.activeTexture(TEXTURE2);
              gl2.bindTexture(TEXTURE_2D, null)

            case Some(textureLookup) =>
              gl2.activeTexture(TEXTURE2);
              gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          // Specular
          textureLocations.find(t => t.name === d.specularRef) match {
            case None =>
              gl2.activeTexture(TEXTURE3);
              gl2.bindTexture(TEXTURE_2D, null)

            case Some(textureLookup) =>
              gl2.activeTexture(TEXTURE3);
              gl2.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          rec(remaining, 0, d.diffuseRef)

        case _ if batchCount === maxBatchSize =>
          drawBuffer(batchCount)
          rec(remaining, 0, textureHash)

        case (d: DisplayObject) :: ds =>
          updateData(d, batchCount)
          rec(ds, batchCount + 1, textureHash)

        case (c: DisplayClone) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, textureHash)

            case Some(refDisplayObject) =>
              updateData(refDisplayObject, batchCount)
              overwriteFromDisplayBatchClone(c.asBatchData, batchCount)
              rec(ds, batchCount + 1, textureHash)
          }

        case (c: DisplayCloneBatch) :: ds =>
          cloneBlankDisplayObjects.get(c.id) match {
            case None =>
              rec(ds, batchCount, textureHash)

            case Some(refDisplayObject) =>
              val numberProcessed: Int =
                processCloneBatch(c, refDisplayObject, batchCount)

              rec(ds, batchCount + numberProcessed, textureHash)
          }

      }

    rec(sorted.toList, 0, "")

    metrics.record(layer.metricEnd)

  }

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.While"))
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
