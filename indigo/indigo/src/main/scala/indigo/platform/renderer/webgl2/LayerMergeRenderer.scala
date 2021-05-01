package indigo.platform.renderer.webgl2

import org.scalajs.dom.raw.WebGLProgram
import indigo.facades.WebGL2RenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLTexture
import indigo.shared.datatypes.RGBA
import indigo.platform.renderer.shared.WebGLHelper
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.FrameBufferComponents
import scala.collection.mutable.HashMap
import indigo.shared.shader.ShaderId
import indigo.shared.display.DisplayObjectUniformData

class LayerMergeRenderer(gl2: WebGL2RenderingContext, frameDataUBOBuffer: => WebGLBuffer) {

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()
  private val customDataUBOBuffers: HashMap[String, WebGLBuffer] =
    HashMap.empty[String, WebGLBuffer]

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val displayObjectUBODataSize: Int = 16

  private val uboData: Array[Float] =
    Array.fill(displayObjectUBODataSize)(0.0f)

  def setupShader(program: WebGLProgram, projection: Array[Float], width: Int, height: Int): Unit = {

    gl2.useProgram(program)

    uboData(0) = width.toFloat
    uboData(1) = height.toFloat

    WebGLHelper.attachUBOData(gl2, projection ++ uboData, displayObjectUBOBuffer)
    WebGLHelper.bindUBO(
      gl2,
      program,
      "IndigoMergeData",
      RendererWebGL2Constants.mergeObjectBlockPointer,
      displayObjectUBOBuffer
    )
    WebGLHelper.bindUBO(
      gl2,
      program,
      "IndigoFrameData",
      RendererWebGL2Constants.frameDataBlockPointer,
      frameDataUBOBuffer
    )
  }

  private given CanEqual[Option[WebGLProgram], Option[WebGLProgram]] = CanEqual.derived

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.throw"))
  def merge(
      projection: Array[Float],
      srcFrameBuffer: FrameBufferComponents.SingleOutput,
      dstFrameBuffer: FrameBufferComponents.SingleOutput,
      targetFrameBuffer: Option[FrameBufferComponents.SingleOutput],
      width: Int,
      height: Int,
      clearColor: RGBA,
      isCanvasMerge: Boolean,
      customShaders: HashMap[ShaderId, WebGLProgram],
      shaderId: ShaderId,
      shaderUniformData: List[DisplayObjectUniformData]
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
          ud.blockName,
          RendererWebGL2Constants.blendDataBlockOffsetPointer + i,
          buff
        )
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
        "SRC_CHANNEL" -> src.diffuse,
        "DST_CHANNEL" -> dst.diffuse
      )

    var i: Int = 0

    while (i < uniformTextures.length) {
      val tex = uniformTextures(i)
      WebGLHelper.attach(gl2, program, i + 1, tex._1, tex._2)
      i = i + 1
    }
  }

}
