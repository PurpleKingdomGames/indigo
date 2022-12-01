package indigo.platform.renderer.webgl2

import indigo.facades.WebGL2RenderingContext
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.WebGLHelper
import indigo.shared.datatypes.RGBA
import indigo.shared.display.DisplayObjectUniformData
import indigo.shared.shader.ShaderId
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext._
import org.scalajs.dom.WebGLTexture

import scala.scalajs.js.JSConverters._

class LayerMergeRenderer(gl2: WebGL2RenderingContext, frameDataUBOBuffer: => WebGLBuffer):

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val customDataUBOBuffers: scalajs.js.Dictionary[WebGLBuffer] =
    scalajs.js.Dictionary.empty[WebGLBuffer]

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val displayObjectUBODataSize: Int = 16

  private val uboData: scalajs.js.Array[Float] =
    Array.fill(displayObjectUBODataSize)(0.0f).toJSArray

  def setupShader(program: WebGLProgram, projection: scalajs.js.Array[Float], width: Int, height: Int): Unit = {

    gl2.useProgram(program)

    uboData(0) = width.toFloat
    uboData(1) = height.toFloat

    WebGLHelper.attachUBOData(gl2, projection ++ uboData, displayObjectUBOBuffer)
    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.mergeObjectBlockPointer,
      displayObjectUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoMergeData")
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      RendererWebGL2Constants.frameDataBlockPointer,
      frameDataUBOBuffer,
      gl2.getUniformBlockIndex(program, "IndigoFrameData")
    )
  }

  private given CanEqual[Option[WebGLProgram], Option[WebGLProgram]] = CanEqual.derived

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.throw"))
  def merge(
      projection: scalajs.js.Array[Float],
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      dstFrameBuffer: FrameBufferComponents.SingleOutput,
      targetFrameBuffer: Option[FrameBufferComponents.SingleOutput],
      width: Int,
      height: Int,
      clearColor: RGBA,
      isCanvasMerge: Boolean,
      customShaders: scalajs.js.Dictionary[WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: scalajs.js.Array[DisplayObjectUniformData]
  ): Unit = {

    if (isCanvasMerge)
      FrameBufferFunctions.switchToCanvas(gl2, clearColor)

    targetFrameBuffer.foreach { target =>
      FrameBufferFunctions.switchToFramebuffer(gl2, target.frameBuffer, RGBA.Zero, true)
    }

    // Switch and reference shader
    val activeShader: WebGLProgram =
      customShaders.get(shaderId.toString) match {
        case Some(s) =>
          setupShader(s, projection, width, height)
          s

        case None =>
          throw new Exception(
            s"Missing blend shader '${shaderId}'. Have you remembered to add the shader to the boot sequence or disabled auto-loading of default shaders?"
          )
      }

    // UBO data
    shaderUniformData.zipWithIndex.foreach { case (ud, i) =>
      if (ud.uniformHash.nonEmpty) {
        val buff = customDataUBOBuffers.getOrElseUpdate(ud.uniformHash, gl2.createBuffer())
        WebGLHelper.attachUBOData(gl2, ud.data, buff)
        WebGLHelper.bindUBO(
          gl2,
          activeShader,
          RendererWebGL2Constants.customDataBlockOffsetPointer + i,
          buff,
          gl2.getUniformBlockIndex(activeShader, ud.blockName)
        )
      }
    }

    // Assign src and dst channels
    setupMergeFragmentShaderState(activeShader, srcFrameBuffer, dstFrameBuffer)

    gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);

  }

  def setupMergeFragmentShaderState(
      program: WebGLProgram,
      src: FrameBufferComponents.SingleOutput,
      dst: FrameBufferComponents.SingleOutput
  ): Unit =
    WebGLHelper.attach(gl2, program, 0, "SRC_CHANNEL", src.diffuse)
    WebGLHelper.attach(gl2, program, 1, "DST_CHANNEL", dst.diffuse)
