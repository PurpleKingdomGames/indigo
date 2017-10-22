package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.util.Logger
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram, WebGLTexture, WebGLUniformLocation}

import scala.scalajs.js.typedarray.Float32Array

object RendererFunctions {

  def createVertexBuffer(gl: raw.WebGLRenderingContext): WebGLBuffer = {
    gl.createBuffer()
  }

  def bindToBuffer(gl: raw.WebGLRenderingContext, vertexBuffer: WebGLBuffer, vertices: scalajs.js.Array[Double]): Unit = {
    //Create a new buffer
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)

    //bind it to the current buffer
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)
  }

  def shaderProgramSetup(gl: raw.WebGLRenderingContext): WebGLProgram = {
    //vertex shader source code
    val vertCode =
      """
        |attribute vec4 coordinates;
        |attribute vec2 a_texcoord;
        |attribute vec4 a_effectValues;
        |
        |uniform mat4 u_matrix;
        |
        |varying vec2 v_texcoord;
        |varying vec4 v_effectValues;
        |
        |void main(void) {
        |  gl_Position = u_matrix * coordinates;
        |
        |  // Pass the texcoord to the fragment shader.
        |  v_texcoord = a_texcoord;
        |  v_effectValues = a_effectValues;
        |}
      """.stripMargin

    //Create a vertex shader program object and compile it
    val vertShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vertShader, vertCode)
    gl.compileShader(vertShader)

    Logger.info("Pixel vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))

    //fragment shader source code
    val fragCode =
      """
        |precision mediump float;
        |
        |// Passed in from the vertex shader.
        |varying vec2 v_texcoord;
        |varying vec4 v_effectValues;
        |
        |// The texture.
        |uniform sampler2D u_texture;
        |
        |void main(void) {
        |   vec4 textureColor = texture2D(u_texture, v_texcoord);
        |   gl_FragColor = textureColor * v_effectValues;
        |}
      """.stripMargin

    //Create a fragment shader program object and compile it
    val fragShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fragShader, fragCode)
    gl.compileShader(fragShader)

    Logger.info("Pixel fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    shaderProgram
  }

  def lightingShaderProgramSetup(gl: raw.WebGLRenderingContext): WebGLProgram = {
    //vertex shader source code
    val vertCode =
      """
        |attribute vec4 coordinates;
        |attribute vec2 a_texcoord;
        |attribute vec4 a_effectValues;
        |
        |uniform mat4 u_matrix;
        |
        |varying vec2 v_texcoord;
        |varying vec4 v_effectValues;
        |
        |void main(void) {
        |  gl_Position = u_matrix * coordinates;
        |
        |  // Pass the texcoord to the fragment shader.
        |  v_texcoord = a_texcoord;
        |  v_effectValues = a_effectValues;
        |}
      """.stripMargin

    //Create a vertex shader program object and compile it
    val vertShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vertShader, vertCode)
    gl.compileShader(vertShader)

    Logger.info("Light vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))

    //fragment shader source code
    val fragCode =
      """
        |precision mediump float;
        |
        |// Passed in from the vertex shader.
        |varying vec2 v_texcoord;
        |varying vec4 v_effectValues;
        |
        |// The texture.
        |uniform sampler2D u_texture;
        |
        |void main(void) {
        |   vec4 textureColor = texture2D(u_texture, v_texcoord);
        |
        |   float average = (textureColor.r + textureColor.g + textureColor.b) / float(3);
        |
        |   gl_FragColor = vec4(textureColor.rgb * v_effectValues.rgb, average * v_effectValues.a);
        |}
      """.stripMargin

    //Create a fragment shader program object and compile it
    val fragShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fragShader, fragCode)
    gl.compileShader(fragShader)

    Logger.info("Light fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    shaderProgram
  }

  def mergeShaderProgramSetup(gl: raw.WebGLRenderingContext): WebGLProgram = {
    //vertex shader source code
    val vertCode =
      """
        |attribute vec4 coordinates;
        |attribute vec2 a_texcoord;
        |attribute vec4 a_effectValues;
        |
        |uniform mat4 u_matrix;
        |
        |varying vec2 v_texcoord;
        |varying vec4 v_effectValues;
        |
        |void main(void) {
        |  gl_Position = u_matrix * coordinates;
        |
        |  // Pass the texcoord to the fragment shader.
        |  v_texcoord = a_texcoord;
        |  v_effectValues = a_effectValues;
        |}
      """.stripMargin

    //Create a vertex shader program object and compile it
    val vertShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vertShader, vertCode)
    gl.compileShader(vertShader)

    Logger.info("Merge vshader compiled: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))

    //fragment shader source code

    val fragCode =
      """
        |precision mediump float;
        |
        |// Passed in from the vertex shader.
        |varying vec2 v_texcoord;
        |varying vec4 v_effectValues;
        |
        |// The textures.
        |uniform sampler2D u_texture_game;
        |uniform sampler2D u_texture_lighting;
        |uniform sampler2D u_texture_ui;
        |
        |void main(void) {
        |   vec4 textureColorGame = texture2D(u_texture_game, v_texcoord);
        |   vec4 textureColorLighting = texture2D(u_texture_lighting, v_texcoord);
        |   vec4 textureColorUi = texture2D(u_texture_ui, v_texcoord);
        |
        |   vec4 gameAndLighting = textureColorGame * textureColorLighting;
        |
        |   gl_FragColor = mix(gameAndLighting, textureColorUi, textureColorUi.a);
        |}
      """.stripMargin

    //Create a fragment shader program object and compile it
    val fragShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fragShader, fragCode)
    gl.compileShader(fragShader)

    Logger.info("Merge fshader compiled: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    shaderProgram
  }

  def bindShaderToBuffer(cNc: ContextAndCanvas, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer, textureBuffer: WebGLBuffer, effectsBuffer: WebGLBuffer): Unit = {

    val gl = cNc.context

    // Vertices
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)

    val coordinatesVar = gl.getAttribLocation(shaderProgram, "coordinates")

    gl.vertexAttribPointer(
      indx = coordinatesVar,
      size = 3,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    gl.enableVertexAttribArray(coordinatesVar)

    // Texture info
    gl.bindBuffer(ARRAY_BUFFER, textureBuffer)

    val texCoOrdLocation = gl.getAttribLocation(shaderProgram, "a_texcoord")
    gl.vertexAttribPointer(
      indx = texCoOrdLocation,
      size = 2,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(texCoOrdLocation)

    // Effects info
    gl.bindBuffer(ARRAY_BUFFER, effectsBuffer)

    val effectValuesLocation = gl.getAttribLocation(shaderProgram, "a_effectValues")
    gl.vertexAttribPointer(
      indx = effectValuesLocation,
      size = 4,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(effectValuesLocation)

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

  private var lastTextureName: String = ""

  def setupFragmentShader(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, texture: WebGLTexture, imageRef: String): Unit = {

    if(imageRef != lastTextureName) {
      gl.bindTexture(TEXTURE_2D, texture)
      lastTextureName = imageRef
    }

    val u_texture = gl.getUniformLocation(shaderProgram, "u_texture")
    gl.uniform1i(u_texture, 0)
  }

  def setupLightingFragmentShader(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, texture: WebGLTexture, imageRef: String): Unit = {

    if(imageRef != lastTextureName) {
      gl.bindTexture(TEXTURE_2D, texture)
      lastTextureName = imageRef
    }

    val u_texture = gl.getUniformLocation(shaderProgram, "u_texture")
    gl.uniform1i(u_texture, 0)

  }

  def setupMergeFragmentShader(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, textureGame: WebGLTexture, textureLighting: WebGLTexture, textureUi: WebGLTexture): Unit = {

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

  private var resizeRun: Boolean = false
  var orthographicProjectionMatrix: Matrix4 = Matrix4.identity
  var orthographicProjectionMatrixNoMag: Matrix4 = Matrix4.identity

  def resize(canvas: html.Canvas, actualWidth: Int, actualHeight: Int, magnification: Int): Unit =
    if (!resizeRun || canvas.width != actualWidth || canvas.height != actualHeight) {
      resizeRun = true
      canvas.width = actualWidth
      canvas.height = actualHeight

      orthographicProjectionMatrix = Matrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification)
      orthographicProjectionMatrixNoMag = Matrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble)
    }

  val flipMatrix: ((Boolean, Boolean)) => Matrix4 = flipValues => {
    flipValues match {
      case (true, true)   => Matrix4.identity.translate(1, 1, 0).scale(-1, -1, -1)
      case (true, false)  => Matrix4.identity.translate(1, 0, 0).scale(-1,  1, -1)
      case (false, true)  => Matrix4.identity.translate(0, 1, 0).scale( 1, -1, -1)
      case (false, false) => Matrix4.identity
    }
  }

  def setupVertexShader(cNc: ContextAndCanvas, shaderProgram: WebGLProgram, projectionMatrix: Matrix4): Unit = {
    val translation: WebGLUniformLocation = cNc.context.getUniformLocation(shaderProgram, "u_matrix")

    cNc.context.uniformMatrix4fv(
      location = translation,
      transpose = false,
      value = projectionMatrix.toJsArray //Matrix4.multiply(matrix4, flipMatrix((displayObject.flipHorizontal, displayObject.flipVertical)))
    )
  }

}