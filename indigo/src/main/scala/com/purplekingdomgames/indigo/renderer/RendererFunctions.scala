package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.util.Logger
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram, WebGLTexture}

import scala.scalajs.js.typedarray.Float32Array

object RendererFunctions {

  def createVertexBuffer[T](gl: raw.WebGLRenderingContext, vertices: scalajs.js.Array[T])(implicit num: Numeric[T]): WebGLBuffer = {
    //Create an empty buffer object and store vertex data
    val vertexBuffer: WebGLBuffer = gl.createBuffer()

    //Create a new buffer
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)

    //bind it to the current buffer
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)

    vertexBuffer
  }

  def shaderProgramSetup(gl: raw.WebGLRenderingContext): WebGLProgram = {
    //vertex shader source code
    val vertCode =
      """
        |attribute vec4 coordinates;
        |attribute vec2 a_texcoord;
        |
        |uniform mat4 u_matrix;
        |
        |varying vec2 v_texcoord;
        |
        |void main(void) {
        |  gl_Position = u_matrix * coordinates;
        |
        |  // Pass the texcoord to the fragment shader.
        |  v_texcoord = a_texcoord;
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
        |
        |// The texture.
        |uniform sampler2D u_texture;
        |uniform float uAlpha;
        |uniform vec3 uTint;
        |uniform vec2 uTexcoordScale;
        |uniform vec2 uTexcoordTranslate;
        |
        |void main(void) {
        |   vec4 textureColor = texture2D(u_texture, (v_texcoord * uTexcoordScale) + uTexcoordTranslate);
        |   gl_FragColor = vec4(textureColor.rgb * uTint, textureColor.a * uAlpha);
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
        |
        |uniform mat4 u_matrix;
        |
        |varying vec2 v_texcoord;
        |
        |void main(void) {
        |  gl_Position = u_matrix * coordinates;
        |
        |  // Pass the texcoord to the fragment shader.
        |  v_texcoord = a_texcoord;
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
        |
        |// The texture.
        |uniform sampler2D u_texture;
        |uniform float uAlpha;
        |uniform vec3 uTint;
        |uniform vec2 uTexcoordScale;
        |uniform vec2 uTexcoordTranslate;
        |
        |void main(void) {
        |   vec4 textureColor = texture2D(u_texture, (v_texcoord * uTexcoordScale) + uTexcoordTranslate);
        |
        |   float average = (textureColor.r + textureColor.g + textureColor.b) / float(3);
        |
        |   gl_FragColor = vec4(textureColor.rgb * uTint, average * uAlpha);
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
        |
        |uniform mat4 u_matrix;
        |
        |varying vec2 v_texcoord;
        |
        |void main(void) {
        |  gl_Position = u_matrix * coordinates;
        |
        |  // Pass the texcoord to the fragment shader.
        |  v_texcoord = a_texcoord;
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

  def bindShaderToBuffer(cNc: ContextAndCanvas, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer, textureBuffer: WebGLBuffer): Unit = {

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

  def setupFragmentShader(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, texture: WebGLTexture, displayObject: DisplayObject): Unit = {

    if(displayObject.imageRef != lastTextureName) {
      gl.bindTexture(TEXTURE_2D, texture)
      lastTextureName = displayObject.imageRef
    }

    val u_texture = gl.getUniformLocation(shaderProgram, "u_texture")
    gl.uniform1i(u_texture, 0)

    val alphaLocation = gl.getUniformLocation(shaderProgram, "uAlpha")
    gl.uniform1f(alphaLocation, displayObject.alpha)

    val tintLocation = gl.getUniformLocation(shaderProgram, "uTint")
    gl.uniform3fv(tintLocation, scalajs.js.Array[Double](displayObject.tintR, displayObject.tintG, displayObject.tintB))

    val texcoordScaleLocation = gl.getUniformLocation(shaderProgram, "uTexcoordScale")
    gl.uniform2fv(texcoordScaleLocation, displayObject.frame.scale.toScalaJSArrayDouble)

    val texcoordTranlsateLocation = gl.getUniformLocation(shaderProgram, "uTexcoordTranslate")
    gl.uniform2fv(texcoordTranlsateLocation, displayObject.frame.translate.toScalaJSArrayDouble)
  }

  def setupLightingFragmentShader(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, texture: WebGLTexture, displayObject: DisplayObject): Unit = {

    if(displayObject.imageRef != lastTextureName) {
      gl.bindTexture(TEXTURE_2D, texture)
      lastTextureName = displayObject.imageRef
    }

    val u_texture = gl.getUniformLocation(shaderProgram, "u_texture")
    gl.uniform1i(u_texture, 0)

    val alphaLocation = gl.getUniformLocation(shaderProgram, "uAlpha")
    gl.uniform1f(alphaLocation, displayObject.alpha)

    val tintLocation = gl.getUniformLocation(shaderProgram, "uTint")
    gl.uniform3fv(tintLocation, scalajs.js.Array[Double](displayObject.tintR, displayObject.tintG, displayObject.tintB))

    val texcoordScaleLocation = gl.getUniformLocation(shaderProgram, "uTexcoordScale")
    gl.uniform2fv(texcoordScaleLocation, displayObject.frame.scale.toScalaJSArrayDouble)

    val texcoordTranlsateLocation = gl.getUniformLocation(shaderProgram, "uTexcoordTranslate")
    gl.uniform2fv(texcoordTranlsateLocation, displayObject.frame.translate.toScalaJSArrayDouble)

  }

  def setupMergeFragmentShader(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, textureGame: WebGLTexture, textureLighting: WebGLTexture, textureUi: WebGLTexture, displayObject: DisplayObject): Unit = {

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

  val flipMatrix: ((Boolean, Boolean)) => Matrix4 = flipValues => {
    flipValues match {
      case (true, true)   => Matrix4.identity.translate(1, 1, 0).scale(-1, -1, -1)
      case (true, false)  => Matrix4.identity.translate(1, 0, 0).scale(-1,  1, -1)
      case (false, true)  => Matrix4.identity.translate(0, 1, 0).scale( 1, -1, -1)
      case (false, false) => Matrix4.identity
    }
  }

  def setupVertexShader(cNc: ContextAndCanvas, shaderProgram: WebGLProgram, displayObject: DisplayObject, magnification: Int): Unit = {
    val translation = cNc.context.getUniformLocation(shaderProgram, "u_matrix")

    val matrix4: Matrix4 =
      Matrix4
        .orthographic(0, cNc.width / magnification, cNc.height / magnification, 0, -10000, 10000)
        .translate(displayObject.x, displayObject.y, displayObject.z)
        .scale(displayObject.width, displayObject.height, 1)

    cNc.context.uniformMatrix4fv(
      location = translation,
      transpose = false,
      value = Matrix4.multiply(matrix4, flipMatrix((displayObject.flipHorizontal, displayObject.flipVertical)))
    )
  }

  def resize(canvas: html.Canvas, actualWidth: Int, actualHeight: Int): Unit =
    if (canvas.width != actualWidth || canvas.height != actualHeight) {
      canvas.width = actualWidth
      canvas.height = actualHeight
    }

}