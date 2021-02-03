package indigo.platform.renderer.webgl2

import indigo.shared.display.DisplayObject
import scala.scalajs.js.typedarray.Float32Array
import org.scalajs.dom.raw.WebGLProgram
import indigo.facades.WebGL2RenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import indigo.shaders.WebGL2LayerMerge
import org.scalajs.dom.raw.WebGLTexture
import scala.scalajs.js.JSConverters._
import indigo.shared.datatypes.RGBA
import indigo.platform.renderer.shared.RendererHelper
import indigo.platform.renderer.shared.WebGLHelper
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents

class LayerMergeRenderer(gl2: WebGL2RenderingContext) {

  private val mergeShaderProgram: WebGLProgram =
    WebGLHelper.shaderProgramSetup(gl2, "Layer Merge", WebGL2LayerMerge)

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val projectionMatrixUBODataSize: Int = 16
  private val displayObjectUBODataSize: Int    = 16 * 2
  private val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

  private val uboData: scalajs.js.Array[Float] =
    List.fill(displayObjectUBODataSize)(0.0f).toJSArray

  def updateUBOData(
      displayObject: DisplayObject
  ): Unit = {
    uboData(0) = 0.0f
    uboData(1) = 0.0f
    uboData(2) = displayObject.width.toFloat
    uboData(3) = displayObject.height.toFloat

    uboData(4) = displayObject.channelOffset0X
    uboData(5) = displayObject.channelOffset0X
    uboData(6) = displayObject.frameScaleX.toFloat
    uboData(7) = displayObject.frameScaleY.toFloat
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def merge(
      projection: scalajs.js.Array[Float],
      layerFrameBuffer: FrameBufferComponents.SingleOutput,
      backFrameBuffer: Option[FrameBufferComponents.SingleOutput],
      width: Int,
      height: Int,
      clearColor: RGBA
  ): Unit = {

    backFrameBuffer match {
      case Some(buffer) =>
        FrameBufferFunctions.switchToFramebuffer(gl2, buffer.frameBuffer, clearColor, false)

      case None =>
        FrameBufferFunctions.switchToCanvas(gl2, clearColor)
    }

    gl2.useProgram(mergeShaderProgram)

    updateUBOData(
      RendererHelper.screenDisplayObject(width, height)
    )

    // UBO data
    gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(
      gl2.UNIFORM_BUFFER,
      0,
      displayObjectUBOBuffer,
      0,
      uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl2.bufferData(
      ARRAY_BUFFER,
      new Float32Array(projection ++ uboData),
      STATIC_DRAW
    )

    setupMergeFragmentShaderState(
      layerFrameBuffer
    )

    gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);

  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def setupMergeFragmentShaderState(
      layer: FrameBufferComponents.SingleOutput
  ): Unit = {

    val uniformTextures: List[(String, WebGLTexture)] =
      List(
        "u_texture_layer" -> layer.diffuse
      )

    var i: Int = 0

    while (i < uniformTextures.length) {
      val tex = uniformTextures(i)
      WebGLHelper.attach(gl2, mergeShaderProgram, i + 1, tex._1, tex._2)
      i = i + 1
    }

    // Reset to TEXTURE0 before the next round of rendering happens.
    gl2.activeTexture(TEXTURE0)
  }

}
