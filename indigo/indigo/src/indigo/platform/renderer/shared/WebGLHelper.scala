package indigo.platform.renderer.shared

import indigo.facades.WebGL2RenderingContext
import indigo.shared.IndigoLogger
import indigo.shared.scenegraph.BlendFactor
import indigo.shared.shader.RawShaderCode
import org.scalajs.dom.ImageData
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.*
import org.scalajs.dom.WebGLTexture

import scala.scalajs.js.typedarray.Float32Array

object WebGLHelper {

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def shaderProgramSetup(gl: WebGLRenderingContext, layerLabel: String, shader: RawShaderCode): WebGLProgram = {
    // Create a vertex shader program object and compile it
    val vertShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vertShader, shader.vertex)
    gl.compileShader(vertShader)

    if (gl.getShaderParameter(vertShader, COMPILE_STATUS).asInstanceOf[Boolean])
      IndigoLogger.info(s"$layerLabel vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))
    else {
      IndigoLogger.info(s"$layerLabel vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))
      IndigoLogger.error(gl.getShaderInfoLog(vertShader));
      gl.deleteShader(vertShader);
      throw new Exception(s"Fatal: Vertex shader compile error ($layerLabel)")
    }

    // Create a fragment shader program object and compile it
    val fragShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fragShader, shader.fragment)
    gl.compileShader(fragShader)

    if (gl.getShaderParameter(fragShader, COMPILE_STATUS).asInstanceOf[Boolean])
      IndigoLogger.info(s"$layerLabel fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))
    else {
      IndigoLogger.info(s"$layerLabel fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))
      IndigoLogger.error(gl.getShaderInfoLog(fragShader));
      gl.deleteShader(fragShader);
      throw new Exception(s"Fatal: Fragment shader compile error ($layerLabel)")
    }

    // Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    if (gl.getProgramParameter(shaderProgram, LINK_STATUS).asInstanceOf[Boolean]) shaderProgram
    else {
      IndigoLogger.error(gl.getProgramInfoLog(shaderProgram));
      gl.deleteProgram(shaderProgram);
      throw new Exception(s"Fatal: RawShaderCode program link error ($layerLabel)")
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def attachUBOData(gl2: WebGL2RenderingContext, data: scalajs.js.Array[Float], buffer: WebGLBuffer): Unit = {
    gl2.bindBuffer(gl2.UNIFORM_BUFFER, buffer)
    gl2.bufferData(
      gl2.UNIFORM_BUFFER,
      (Math.ceil(data.length.toDouble / 16).toInt * 16) * Float32Array.BYTES_PER_ELEMENT,
      DYNAMIC_DRAW
    )
    gl2.bufferSubData(gl2.UNIFORM_BUFFER, 0, new Float32Array(data))
    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);
  }

  def bindUBO(
      gl2: WebGL2RenderingContext,
      activeShader: WebGLProgram,
      blockPointer: Int,
      buffer: WebGLBuffer,
      uniformBlockIndex: Double
  ): Unit = {
    gl2.bindBufferBase(gl2.UNIFORM_BUFFER, blockPointer, buffer)
    gl2.uniformBlockBinding(activeShader, uniformBlockIndex, blockPointer)
  }

  private val textureLocationsLookUp: scalajs.js.Array[Int] =
    scalajs.js.Array(
      TEXTURE0,
      TEXTURE1,
      TEXTURE2,
      TEXTURE3,
      TEXTURE4,
      TEXTURE5,
      TEXTURE6,
      TEXTURE7,
      TEXTURE8,
      TEXTURE9,
      TEXTURE10,
      TEXTURE11,
      TEXTURE12,
      TEXTURE13,
      TEXTURE14,
      TEXTURE15,
      TEXTURE16,
      TEXTURE17,
      TEXTURE18,
      TEXTURE19,
      TEXTURE20,
      TEXTURE21,
      TEXTURE22,
      TEXTURE23,
      TEXTURE24,
      TEXTURE25,
      TEXTURE26,
      TEXTURE27,
      TEXTURE28,
      TEXTURE29,
      TEXTURE30,
      TEXTURE31
    )

  val intToTextureLocation: Int => Int = index => textureLocationsLookUp(index)

  def attach(
      gl: WebGLRenderingContext,
      shaderProgram: WebGLProgram,
      location: Int,
      uniformName: String,
      texture: WebGLTexture
  ): Unit = {
    gl.uniform1i(gl.getUniformLocation(shaderProgram, uniformName), location)
    gl.activeTexture(intToTextureLocation(location))
    gl.bindTexture(TEXTURE_2D, texture)
  }

  // Blend Equations
  def setBlendAdd(gl: WebGLRenderingContext): Unit =
    gl.blendEquation(FUNC_ADD)

  def setBlendSubtract(gl: WebGLRenderingContext): Unit =
    gl.blendEquation(FUNC_SUBTRACT)

  def setBlendReverseSubtract(gl: WebGLRenderingContext): Unit =
    gl.blendEquation(FUNC_REVERSE_SUBTRACT)

  def setBlendMin(gl2: WebGL2RenderingContext): Unit =
    gl2.blendEquation(gl2.MIN)
  def setBlendDarken(gl2: WebGL2RenderingContext): Unit =
    setBlendMin(gl2)

  def setBlendMax(gl2: WebGL2RenderingContext): Unit =
    gl2.blendEquation(gl2.MAX)
  def setBlendLighten(gl2: WebGL2RenderingContext): Unit =
    setBlendMax(gl2)

  // Blend Modes
  def setAlphaBlend(gl: WebGLRenderingContext): Unit =
    gl.blendFunc(SRC_ALPHA, DST_ALPHA)

  def setNormalBlend(gl: WebGLRenderingContext): Unit =
    gl.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)

  def setLightsBlend(gl: WebGLRenderingContext): Unit =
    gl.blendFunc(SRC_ALPHA, ONE)

  def convertBlendFactor(bf: BlendFactor): Int =
    bf match {
      case BlendFactor.Zero             => ZERO
      case BlendFactor.One              => ONE
      case BlendFactor.SrcColor         => SRC_COLOR
      case BlendFactor.DstColor         => DST_COLOR
      case BlendFactor.SrcAlpha         => SRC_ALPHA
      case BlendFactor.DstAlpha         => DST_ALPHA
      case BlendFactor.OneMinusSrcColor => ONE_MINUS_SRC_COLOR
      case BlendFactor.OneMinusDstColor => ONE_MINUS_DST_COLOR
      case BlendFactor.OneMinusSrcAlpha => ONE_MINUS_SRC_ALPHA
      case BlendFactor.OneMinusDstAlpha => ONE_MINUS_DST_ALPHA
      case BlendFactor.SrcAlphaSaturate => SRC_ALPHA_SATURATE
    }

  def setBlendFunc(gl: WebGLRenderingContext, src: BlendFactor, dst: BlendFactor): Unit =
    gl.blendFunc(convertBlendFactor(src), convertBlendFactor(dst))

  def organiseImage(gl: WebGLRenderingContext, image: ImageData): WebGLTexture = {
    val texture = createAndBindTexture(gl)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
    gl.generateMipmap(TEXTURE_2D)

    texture
  }

  def createAndBindTexture(gl: WebGLRenderingContext): WebGLTexture = {
    val texture = gl.createTexture()
    gl.bindTexture(TEXTURE_2D, texture)

    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, NEAREST)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, NEAREST)

    texture
  }

}
