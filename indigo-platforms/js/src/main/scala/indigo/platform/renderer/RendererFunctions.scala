package indigo.platform.renderer

import indigo.shared.IndigoLogger
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLProgram, WebGLTexture}
import indigo.shared.datatypes.Matrix4

import indigo.shared.EqualTo._

import indigo.shared.display.DisplayObject
import scala.collection.mutable.ListBuffer
import indigo.shared.display.DisplayEntity

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

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrix: scalajs.js.Array[Double] = mat4ToJsArray(Matrix4.identity)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixNoMag: scalajs.js.Array[Double] = mat4ToJsArray(Matrix4.identity)

  def resize(canvas: html.Canvas, actualWidth: Int, actualHeight: Int, magnification: Int): Unit =
    if (!resizeRun || (canvas.width !== actualWidth) || (canvas.height !== actualHeight)) {
      resizeRun = true
      canvas.width = actualWidth
      canvas.height = actualHeight

      orthographicProjectionMatrix = mat4ToJsArray(Matrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification))
      orthographicProjectionMatrixNoMag = mat4ToJsArray(Matrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble))

      ()
    }

  def mat4ToJsArray(mat4d: Matrix4): scalajs.js.Array[Double] = {
    val a = new scalajs.js.Array[Double]()
    mat4d.mat.foreach(d => a.push(d))
    a
  }

  def updateUBOData(displayObject: DisplayObject): scalajs.js.Array[Double] = {
    val uboData = scalajs.js.Array[Double]()

    uboData(0) = displayObject.x.toDouble
    uboData(1) = displayObject.y.toDouble
    uboData(2) = displayObject.width.toDouble * displayObject.scaleX
    uboData(3) = displayObject.height.toDouble * displayObject.scaleY

    uboData(4) = displayObject.tintR.toDouble
    uboData(5) = displayObject.tintG.toDouble
    uboData(6) = displayObject.tintB.toDouble
    uboData(7) = displayObject.alpha.toDouble

    uboData(8) = displayObject.frameX
    uboData(9) = displayObject.frameY
    uboData(10) = displayObject.frameScaleX
    uboData(11) = displayObject.frameScaleY

    uboData(12) = displayObject.rotation
    uboData(13) = displayObject.flipHorizontal
    uboData(14) = displayObject.flipVertical
    uboData(15) = 0d

    uboData
  }

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  val projectionMatrixUBODataSize: Int = 16
  val displayObjectUBODataSize: Int    = 16
  val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

}
