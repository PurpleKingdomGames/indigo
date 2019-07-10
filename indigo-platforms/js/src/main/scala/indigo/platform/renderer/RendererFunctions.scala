package indigo.platform.renderer

import indigo.shared.IndigoLogger
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLProgram, WebGLTexture}
import indigo.shared.datatypes.Matrix4

import indigo.shared.EqualTo._

import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame

object RendererFunctions {

  val VertexAtrributeLocation = 0
  val TextureAtrributeLocation = 1
  val InstanceAtrributeLocation = 2

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
      rotation = 0,
      scaleX = 1,
      scaleY = 1,
      imageRef = "",
      alpha = 1,
      tintR = 1,
      tintG = 1,
      tintB = 1,
      flipHorizontal = false,
      flipVertical = false,
      frame = SpriteSheetFrame.defaultOffset
    )

  val vertices: scalajs.js.Array[Double] = {
    val vert0 = scalajs.js.Array[Double](-0.5, -0.5, 1.0d)
    val vert1 = scalajs.js.Array[Double](-0.5, 0.5, 1.0d)
    val vert2 = scalajs.js.Array[Double](0.5, -0.5, 1.0d)
    val vert3 = scalajs.js.Array[Double](0.5, 0.5, 1.0d)

    vert0 ++ vert1 ++ vert2 ++ vert3
  }

  val textureCoordinates: scalajs.js.Array[Double] = {
    val tx0 = scalajs.js.Array[Double](0.0, 1.0)
    val tx1 = scalajs.js.Array[Double](0.0, 0.0)
    val tx2 = scalajs.js.Array[Double](1.0, 1.0)
    val tx3 = scalajs.js.Array[Double](1.0, 0.0)

    tx0 ++ tx1 ++ tx2 ++ tx3
  }

  val vertexCount: Int =
    vertices.length

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

  def bindAttibuteBuffer(gl: raw.WebGLRenderingContext, attributeLocation: Int, size: Int): Unit = {
    gl.enableVertexAttribArray(attributeLocation)
    gl.vertexAttribPointer(
      indx = attributeLocation,
      size = size,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
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

  def organiseImage(gl: raw.WebGLRenderingContext, image: raw.ImageData): WebGLTexture = {
    val texture = createAndBindTexture(gl)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
    gl.generateMipmap(TEXTURE_2D)

    texture
  }

  val sortByDepth: List[DisplayObject] => List[DisplayObject] =
    displayObjects => displayObjects.sortWith((d1, d2) => d1.z > d2.z)

  def setupMergeFragmentShaderState(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, textureGame: WebGLTexture, textureLighting: WebGLTexture, textureUi: WebGLTexture): Unit = {
    val u_texture_game = gl.getUniformLocation(shaderProgram, "u_texture_game")
    gl.uniform1i(u_texture_game, 1)
    gl.activeTexture(TEXTURE1)
    gl.bindTexture(TEXTURE_2D, textureGame)

    val u_texture_lighting = gl.getUniformLocation(shaderProgram, "u_texture_lighting")
    gl.uniform1i(u_texture_lighting, 2)
    gl.activeTexture(TEXTURE2)
    gl.bindTexture(TEXTURE_2D, textureLighting)

    val u_texture_ui = gl.getUniformLocation(shaderProgram, "u_texture_ui")
    gl.uniform1i(u_texture_ui, 3)
    gl.activeTexture(TEXTURE3)
    gl.bindTexture(TEXTURE_2D, textureUi)

    // Reset to TEXTURE0 before the next round of rendering happens.
    gl.activeTexture(TEXTURE0)
  }

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
    }

  def mat4ToJsArray(mat4d: Matrix4): scalajs.js.Array[Double] = {
    val a = new scalajs.js.Array[Double]()
    mat4d.mat.foreach(d => a.push(d))
    a
  }

  val uboData: scalajs.js.Array[Double] =
    scalajs.js.Array[Double](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

  def updateUBOData(displayObject: DisplayObject): Unit = {
    uboData(0) = displayObject.x.toDouble
    uboData(1) = displayObject.y.toDouble
    uboData(2) = displayObject.width.toDouble * displayObject.scaleX
    uboData(3) = displayObject.height.toDouble * displayObject.scaleY

    uboData(4) = displayObject.tintR.toDouble
    uboData(5) = displayObject.tintG.toDouble
    uboData(6) = displayObject.tintB.toDouble
    uboData(7) = displayObject.alpha.toDouble

    uboData(8) = displayObject.frame.translate.x
    uboData(9) = displayObject.frame.translate.y
    uboData(10) = displayObject.frame.scale.x
    uboData(11) = displayObject.frame.scale.y

    uboData(12) = displayObject.rotation
    uboData(13) = if(displayObject.flipHorizontal) -1.0d else 1.0d
    uboData(14) = if(displayObject.flipVertical) 1.0d else -1.0d
  }

  // Must equal the number of elements in the makeUBOData(...) array
  val projectionMatrixUBODataSize: Int = 16
  val displayObjectUBODataSize: Int = 16
  val uboDataSize: Int = projectionMatrixUBODataSize + displayObjectUBODataSize

}
