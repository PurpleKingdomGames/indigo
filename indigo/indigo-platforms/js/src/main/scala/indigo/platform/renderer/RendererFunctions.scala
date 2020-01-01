package indigo.platform.renderer

import indigo.shared.IndigoLogger
import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLProgram, WebGLTexture}
import indigo.shared.datatypes.Matrix4

import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity
import scalajs.js.JSConverters._

object RendererFunctions {

  @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny", "org.wartremover.warts.AsInstanceOf", "org.wartremover.warts.Throw"))
  def shaderProgramSetup(gl: raw.WebGLRenderingContext, layerLabel: String, vertexShaderCode: String, fragmentShaderCode: String): WebGLProgram = {
    //Create a vertex shader program object and compile it
    val vertShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vertShader, vertexShaderCode)
    gl.compileShader(vertShader)

    if (gl.getShaderParameter(vertShader, COMPILE_STATUS).asInstanceOf[Boolean]) {
      IndigoLogger.info(s"$layerLabel vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))
    } else {
      IndigoLogger.info(s"$layerLabel vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))
      IndigoLogger.error(gl.getShaderInfoLog(vertShader));
      gl.deleteShader(vertShader);
      throw new Exception("Fatal: Vertex shader compile error")
    }

    //Create a fragment shader program object and compile it
    val fragShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fragShader, fragmentShaderCode)
    gl.compileShader(fragShader)

    if (gl.getShaderParameter(fragShader, COMPILE_STATUS).asInstanceOf[Boolean]) {
      IndigoLogger.info(s"$layerLabel fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))
    } else {
      IndigoLogger.info(s"$layerLabel fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))
      IndigoLogger.error(gl.getShaderInfoLog(fragShader));
      gl.deleteShader(fragShader);
      throw new Exception("Fatal: Fragment shader compile error")
    }

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    if (gl.getProgramParameter(shaderProgram, LINK_STATUS).asInstanceOf[Boolean]) {
      shaderProgram
    } else {
      IndigoLogger.error(gl.getProgramInfoLog(shaderProgram));
      gl.deleteProgram(shaderProgram);
      throw new Exception("Fatal: Shader program link error")
    }
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

  def setNormalBlend(gl: raw.WebGLRenderingContext): Unit =
    gl.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA)

  def setLightingBlend(gl: raw.WebGLRenderingContext): Unit =
    gl.blendFunc(SRC_ALPHA, DST_ALPHA)

  def organiseImage(gl: raw.WebGLRenderingContext, image: raw.ImageData): WebGLTexture = {
    val texture = createAndBindTexture(gl)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
    gl.generateMipmap(TEXTURE_2D)

    texture
  }

  val sortByDepth: ListBuffer[DisplayEntity] => ListBuffer[DisplayEntity] =
    _.sortWith((d1, d2) => d1.z > d2.z)

  def mat4ToJsArray(mat4d: Matrix4): scalajs.js.Array[Double] =
    mat4d.mat.toJSArray

}
