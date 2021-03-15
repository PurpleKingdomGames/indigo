package indigo.platform.renderer.webgl2

import indigo.shared.display.DisplayObject
import org.scalajs.dom.raw.WebGLProgram
import indigo.facades.WebGL2RenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLTexture
import indigo.shared.datatypes.RGBA
import indigo.platform.renderer.shared.RendererHelper
import indigo.platform.renderer.shared.WebGLHelper
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import scala.collection.mutable.HashMap
import indigo.shared.shader.ShaderId
import indigo.shared.display.DisplayObjectUniformData

class LayerMergeRenderer(gl2: WebGL2RenderingContext, frameDataUBOBuffer: => WebGLBuffer) {

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val blendDataUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val displayObjectUBODataSize: Int = 16

  private val uboData: Array[Float] =
    Array.fill(displayObjectUBODataSize)(0.0f)

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

  def setupShader(program: WebGLProgram, projection: Array[Float], width: Int, height: Int): Unit = {

    gl2.useProgram(program)

    updateUBOData(RendererHelper.screenDisplayObject(width, height))

    WebGLHelper.attachUBOData(gl2, projection ++ uboData, displayObjectUBOBuffer)
    WebGLHelper.bindUBO(gl2, program, "IndigoMergeData", RendererWebGL2Constants.mergeObjectBlockPointer, displayObjectUBOBuffer)
    WebGLHelper.bindUBO(gl2, program, "IndigoFrameData", RendererWebGL2Constants.frameDataBlockPointer, frameDataUBOBuffer)
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def merge(
      projection: Array[Float],
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      dstFrameBuffer: FrameBufferComponents.SingleOutput,
      targetFrameBuffer: Option[FrameBufferComponents.SingleOutput],
      width: Int,
      height: Int,
      clearColor: RGBA,
      isCanvasMerge: Boolean,
      defaultShaderProgram: WebGLProgram,
      customShaders: HashMap[ShaderId, WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: Option[DisplayObjectUniformData]
  ): Unit = {

    if (isCanvasMerge)
      FrameBufferFunctions.switchToCanvas(gl2, clearColor)

    targetFrameBuffer.foreach { target =>
      FrameBufferFunctions.switchToFramebuffer(gl2, target.frameBuffer, RGBA.Zero, true)
    }

    // Switch and reference shader
    val activeShader: WebGLProgram =
      customShaders.get(shaderId) match {
        case Some(s) =>
          setupShader(s, projection, width, height)
          s

        case None =>
          setupShader(defaultShaderProgram, projection, width, height)
          defaultShaderProgram
      }

    // UBO data
    shaderUniformData.foreach { ud =>
      if (ud.uniformHash.nonEmpty) {
        WebGLHelper.attachUBOData(gl2, ud.data, blendDataUBOBuffer)
        WebGLHelper.bindUBO(gl2, activeShader, ud.blockName, RendererWebGL2Constants.blendDataBlockPointer, blendDataUBOBuffer)
      }
    }

    // Assign src and dst channels
    setupMergeFragmentShaderState(activeShader, srcFrameBuffer, dstFrameBuffer)

    gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);

  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def setupMergeFragmentShaderState(
      program: WebGLProgram,
      src: FrameBufferComponents.SingleOutput,
      dst: FrameBufferComponents.SingleOutput
  ): Unit = {

    val uniformTextures: List[(String, WebGLTexture)] =
      List(
        "u_channel_0" -> src.diffuse,
        "u_channel_1" -> dst.diffuse
      )

    var i: Int = 0

    while (i < uniformTextures.length) {
      val tex = uniformTextures(i)
      WebGLHelper.attach(gl2, program, i + 1, tex._1, tex._2)
      i = i + 1
    }
  }

}
