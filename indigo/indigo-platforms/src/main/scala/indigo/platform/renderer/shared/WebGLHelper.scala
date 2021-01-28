package indigo.platform.renderer.shared

import indigo.shared.IndigoLogger
import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLTexture
import org.scalajs.dom.raw.WebGLProgram

import indigo.shared.display.Shader
import org.scalajs.dom.raw.WebGLRenderingContext

object WebGLHelper {

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf", "scalafix:DisableSyntax.throw"))
  def shaderProgramSetup(gl: raw.WebGLRenderingContext, layerLabel: String, shader: Shader): WebGLProgram = {
    //Create a vertex shader program object and compile it
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

    //Create a fragment shader program object and compile it
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

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    if (gl.getProgramParameter(shaderProgram, LINK_STATUS).asInstanceOf[Boolean])
      shaderProgram
    else {
      IndigoLogger.error(gl.getProgramInfoLog(shaderProgram));
      gl.deleteProgram(shaderProgram);
      throw new Exception(s"Fatal: Shader program link error ($layerLabel)")
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def intToTextureLocation: Int => Int = {
    case 0  => TEXTURE0
    case 1  => TEXTURE1
    case 2  => TEXTURE2
    case 3  => TEXTURE3
    case 4  => TEXTURE4
    case 5  => TEXTURE5
    case 6  => TEXTURE6
    case 7  => TEXTURE7
    case 8  => TEXTURE8
    case 9  => TEXTURE9
    case 10 => TEXTURE10
    case 11 => TEXTURE11
    case 12 => TEXTURE12
    case 13 => TEXTURE13
    case 14 => TEXTURE14
    case 15 => TEXTURE15
    case 16 => TEXTURE16
    case 17 => TEXTURE17
    case 18 => TEXTURE18
    case 19 => TEXTURE19
    case 20 => TEXTURE20
    case 21 => TEXTURE21
    case 22 => TEXTURE22
    case 23 => TEXTURE23
    case 24 => TEXTURE24
    case 25 => TEXTURE25
    case 26 => TEXTURE26
    case 27 => TEXTURE27
    case 28 => TEXTURE28
    case 29 => TEXTURE29
    case 30 => TEXTURE30
    case 31 => TEXTURE31
    case _  => throw new Exception("Cannot assign > 32 texture locations.")
  }

  def attach(gl: WebGLRenderingContext, shaderProgram: WebGLProgram, location: Int, uniformName: String, texture: WebGLTexture): Unit = {
    gl.uniform1i(gl.getUniformLocation(shaderProgram, uniformName), location)
    gl.activeTexture(intToTextureLocation(location))
    gl.bindTexture(TEXTURE_2D, texture)
  }

  def setAlphaBlend(gl: raw.WebGLRenderingContext): Unit =
    gl.blendFunc(SRC_ALPHA, DST_ALPHA)

  def setNormalBlend(gl: raw.WebGLRenderingContext): Unit =
    gl.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA)

  def setLightingBlend(gl: raw.WebGLRenderingContext): Unit =
    gl.blendFunc(SRC_ALPHA, DST_ALPHA)

  def setDistortionBlend(gl: raw.WebGLRenderingContext): Unit =
    gl.blendFunc(SRC_ALPHA, DST_ALPHA)

  def setLightsBlend(gl: raw.WebGLRenderingContext): Unit =
    gl.blendFunc(SRC_ALPHA, ONE)

  def organiseImage(gl: raw.WebGLRenderingContext, image: raw.ImageData): WebGLTexture = {
    val texture = createAndBindTexture(gl)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
    gl.generateMipmap(TEXTURE_2D)

    texture
  }

  def createAndBindTexture(gl: raw.WebGLRenderingContext): WebGLTexture = {
    val texture = gl.createTexture()
    gl.bindTexture(TEXTURE_2D, texture)

    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, NEAREST)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, NEAREST)

    texture
  }

}
