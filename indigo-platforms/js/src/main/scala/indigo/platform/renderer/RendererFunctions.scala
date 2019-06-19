package indigo.platform.renderer

import indigo.shared.IndigoLogger
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram, WebGLTexture}
import indigo.shared.datatypes.Matrix4

import indigo.shared.EqualTo._

import scala.scalajs.js.typedarray.Float32Array
import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame

object RendererFunctions {

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
      imageRef = "",
      alpha = 1,
      tintR = 1,
      tintG = 1,
      tintB = 1,
      flipHorizontal = false,
      flipVertical = true,
      frame = SpriteSheetFrame.defaultOffset
    )

  def createVertexBuffer(gl: raw.WebGLRenderingContext): WebGLBuffer =
    gl.createBuffer()

  def bindToBuffer(gl: raw.WebGLRenderingContext, vertexBuffer: WebGLBuffer, vertices: scalajs.js.Array[Double]): Unit = {
    //Create a new buffer
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)

    //bind it to the current buffer
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)
  }

  @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
  def shaderProgramSetup(gl: raw.WebGLRenderingContext, layerLabel: String, vertexShaderCode: String, fragmentShaderCode: String): WebGLProgram = {

    //Create a vertex shader program object and compile it
    val vertShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vertShader, vertexShaderCode)
    gl.compileShader(vertShader)

    IndigoLogger.info(s"$layerLabel vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))

    //Create a fragment shader program object and compile it
    val fragShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fragShader, fragmentShaderCode)
    gl.compileShader(fragShader)

    IndigoLogger.info(s"$layerLabel fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    shaderProgram
  }

  def bindAttibuteBuffer(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, attributeName: String, buffer: WebGLBuffer, size: Int): Unit = {
    gl.bindBuffer(ARRAY_BUFFER, buffer)

    val location = gl.getAttribLocation(shaderProgram, attributeName)

    gl.vertexAttribPointer(
      indx = location,
      size = size,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    gl.enableVertexAttribArray(location)
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

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lastTextureName: String = ""

  def setupFragmentShaderState(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, texture: WebGLTexture, displayObject: DisplayObject): Unit = {

    if (displayObject.imageRef !== lastTextureName) {
      gl.bindTexture(TEXTURE_2D, texture)
      lastTextureName = displayObject.imageRef
    }

    gl.uniform4f(
      gl.getUniformLocation(shaderProgram, "u_tint"),
      displayObject.tintR.toDouble,
      displayObject.tintG.toDouble,
      displayObject.tintB.toDouble,
      displayObject.alpha.toDouble
    )

    gl.uniform1i(
      gl.getUniformLocation(shaderProgram, "u_texture"),
      0
    )
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
  var orthographicProjectionMatrix: Matrix4 = Matrix4.identity
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixNoMag: Matrix4 = Matrix4.identity

  def resize(canvas: html.Canvas, actualWidth: Int, actualHeight: Int, magnification: Int): Unit =
    if (!resizeRun || (canvas.width !== actualWidth) || (canvas.height !== actualHeight)) {
      resizeRun = true
      canvas.width = actualWidth
      canvas.height = actualHeight

      orthographicProjectionMatrix = Matrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification)
      orthographicProjectionMatrixNoMag = Matrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble)
    }

  val flipMatrix: ((Boolean, Boolean)) => Matrix4 = {
    case (true, true)   => Matrix4.identity.translate(1, 1, 0).scale(-1, -1, -1)
    case (true, false)  => Matrix4.identity.translate(1, 0, 0).scale(-1, 1, -1)
    case (false, true)  => Matrix4.identity.translate(0, 1, 0).scale(1, -1, -1)
    case (false, false) => Matrix4.identity
  }

  def setupVertexShaderState(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, projectionMatrix: Matrix4, displayObject: DisplayObject): Unit = {
    // Projection
    gl.uniformMatrix4fv(
      location = gl.getUniformLocation(shaderProgram, "u_projection"),
      transpose = false,
      value = mat4ToJsArray(projectionMatrix)
    )

    // Position
    gl.uniform2f(
      gl.getUniformLocation(shaderProgram, "u_translation"),
      displayObject.x.toDouble,
      displayObject.y.toDouble
    )

    // Rotation
    gl.uniform1f(
      gl.getUniformLocation(shaderProgram, "u_rotation"),
      0.0d
    )

    // Scale
    gl.uniform2f(
      gl.getUniformLocation(shaderProgram, "u_scale"),
      displayObject.width.toDouble,
      displayObject.height.toDouble
    )

  }

  def mat4ToJsArray(mat4d: Matrix4): scalajs.js.Array[Double] = {
    val a = new scalajs.js.Array[Double]()
    mat4d.mat.foreach(d => a.push(d))
    a
  }

  def textureCoordinates(d: DisplayObject): scalajs.js.Array[Double] = {
    val tx1 = if (d.flipHorizontal) 1 - d.frame.translate.x else d.frame.translate.x
    val tx2 = if (d.flipHorizontal) 1 - (d.frame.scale.x + d.frame.translate.x) else d.frame.scale.x + d.frame.translate.x
    val ty1 = if (d.flipVertical) 1 - d.frame.translate.y else d.frame.translate.y
    val ty2 = if (d.flipVertical) 1 - (d.frame.scale.y + d.frame.translate.y) else d.frame.scale.y + d.frame.translate.y

    scalajs.js.Array[Double](
      tx1,
      ty1,
      tx1,
      ty2,
      tx2,
      ty1,
      tx1,
      ty2,
      tx2,
      ty1,
      tx2,
      ty2
    )
  }

}
