package com.purplekingdomgames.indigo.renderer

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram, WebGLTexture}
import org.scalajs.dom.{html, raw}

import scala.language.implicitConversions
import scala.scalajs.js.typedarray.Float32Array

object Renderer {

  private var renderer: Option[Renderer] = None

  def apply(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): Renderer = {
    renderer match {
      case Some(r) => r
      case None =>
        val cNc = setupContextAndCanvas(canvas, config.magnification)

        val r = new Renderer(config, loadedTextureAssets, cNc)
        r.init()

        renderer = Some(r)
        renderer.get
    }
  }

  def createCanvas(width: Int, height: Int): html.Canvas = {
    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
    dom.document.body.appendChild(canvas)
    canvas.width = width
    canvas.height = height

    canvas
  }

  private def getContext(canvas: html.Canvas) =
    (canvas.getContext("webgl")|| canvas.getContext("experimental-webgl")).asInstanceOf[raw.WebGLRenderingContext]

  def setupContextAndCanvas(canvas: html.Canvas, magnification: Int): ContextAndCanvas = {
    ContextAndCanvas(
      context = getContext(canvas),
      canvas = canvas,
      width = canvas.clientWidth,
      height = canvas.clientHeight,
      aspect = canvas.clientWidth.toFloat / canvas.clientHeight.toFloat,
      magnification = magnification
    )
  }

}

final case class RendererConfig(viewport: Viewport, clearColor: ClearColor, magnification: Int)
final case class Viewport(width: Int, height: Int)
final case class ClearColor(r: Double, g: Double, b: Double, a: Double) {
  def forceOpaque: ClearColor = this.copy(a = 1d)
  def alphaAsPercent: Int = (a * 100).toInt
  def alphaPercent(percent: Int): ClearColor = this.copy(a = percent.toDouble * 0.01)
  def withR(v: Double): ClearColor = this.copy(r = v)
  def withG(v: Double): ClearColor = this.copy(g = v)
  def withB(v: Double): ClearColor = this.copy(b = v)
  def withA(v: Double): ClearColor = this.copy(a = v)
}

object ClearColor {
  def apply(r: Double, g: Double, b: Double): ClearColor = ClearColor(r, g, b, 1d)

  val Red: ClearColor = ClearColor(1, 0, 0)
  val Green: ClearColor = ClearColor(0, 1, 0)
  val Blue: ClearColor = ClearColor(0, 0, 1)
}

final case class TextureLookup(name: String, texture: WebGLTexture)

final class Renderer(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) {

  private val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, Rectangle2D.vertices)
  private val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context, Rectangle2D.textureCoordinates)

  private val shaderProgram = bucketOfShaders(cNc.context)

  private val textureLocations: List[TextureLookup] =
    loadedTextureAssets.map { li =>
      TextureLookup(li.name, organiseImage(cNc.context, li.data))
    }

  def init(): Unit = {
    cNc.context.disable(DEPTH_TEST)
    cNc.context.viewport(0, 0, cNc.width, cNc.height)
    cNc.context.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
    cNc.context.enable(BLEND)
  }

  private def createVertexBuffer[T](gl: raw.WebGLRenderingContext, vertices: scalajs.js.Array[T])(implicit num: Numeric[T]): WebGLBuffer = {
    //Create an empty buffer object and store vertex data
    val vertexBuffer: WebGLBuffer = gl.createBuffer()

    //Create a new buffer
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)

    //bind it to the current buffer
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)

    vertexBuffer
  }

  private def bucketOfShaders(gl: raw.WebGLRenderingContext): WebGLProgram = {
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

    //println("vert: " + gl.getShaderParameter(vertShader, COMPILE_STATUS))

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

    //println("frag: " + gl.getShaderParameter(fragShader, COMPILE_STATUS))

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    shaderProgram
  }

  private def bindShaderToBuffer(gl: raw.WebGLRenderingContext): Unit = {
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

    val texcoordLocation = gl.getAttribLocation(shaderProgram, "a_texcoord")
    gl.vertexAttribPointer(
      indx = texcoordLocation,
      size = 2,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(texcoordLocation)

  }

  private def organiseImage(gl: raw.WebGLRenderingContext, image: html.Image): WebGLTexture = {

    val texture = gl.createTexture()

    gl.bindTexture(TEXTURE_2D, texture)

    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, NEAREST)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, NEAREST)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
    gl.generateMipmap(TEXTURE_2D)

    texture
  }

  private var lastTextureName: String = ""

  private def setupFragmentShader(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, texture: WebGLTexture, displayObject: DisplayObject): Unit = {

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

  private val flipMatrix: ((Boolean, Boolean)) => Matrix4 = flipValues => {
    flipValues match {
      case (true, true)   => Matrix4.identity.translate(1, 1, 0).scale(-1, -1, -1)
      case (true, false)  => Matrix4.identity.translate(1, 0, 0).scale(-1,  1, -1)
      case (false, true)  => Matrix4.identity.translate(0, 1, 0).scale( 1, -1, -1)
      case (false, false) => Matrix4.identity
    }
  }

  private def setupVertexShader(cNc: ContextAndCanvas, shaderProgram: WebGLProgram, displayObject: DisplayObject): Unit = {
    val translation = cNc.context.getUniformLocation(shaderProgram, "u_matrix")

    val matrix4: Matrix4 =
      Matrix4
        .orthographic(0, cNc.width / cNc.magnification, cNc.height / cNc.magnification, 0, -10000, 10000)
        .translate(displayObject.x, displayObject.y, displayObject.z)
        .scale(displayObject.width, displayObject.height, 1)

    cNc.context.uniformMatrix4fv(
      location = translation,
      transpose = false,
      value = Matrix4.multiply(matrix4, flipMatrix((displayObject.flipHorizontal, displayObject.flipVertical)))
    )
  }

  def drawScene(displayLayerList: List[DisplayLayer]): Unit = {
    cNc.context.clear(COLOR_BUFFER_BIT)
    cNc.context.clearColor(config.clearColor.r, config.clearColor.g, config.clearColor.b, config.clearColor.a)

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight)

    //TEMP
    //TODO: FrameBuffers.
    val displayObjectList = displayLayerList.foldLeft(List.empty[DisplayObject])(_ ++ _.displayObjects)

    //TODO: This sort should be done on a layer by layer basis once we have framebuffers.
    displayObjectList.sortBy(d => (d.z, d.imageRef)).foreach { displayObject =>

      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>

        // Use Program
        cNc.context.useProgram(shaderProgram)

        // Setup attributes
        bindShaderToBuffer(cNc.context)

        // Setup Uniforms
        setupVertexShader(cNc, shaderProgram, displayObject)
        setupFragmentShader(cNc.context, shaderProgram, textureLookup.texture, displayObject)

        // Draw
        cNc.context.drawArrays(Rectangle2D.mode, 0, Rectangle2D.vertexCount)
      }

    }
  }

  private def resize(canvas: html.Canvas, actualWidth: Int, actualHeight: Int): Unit = {
    if (canvas.width != actualWidth || canvas.height != actualHeight) {
      canvas.width = actualWidth
      canvas.height = actualHeight
    }
  }

}

case class ContextAndCanvas(context: raw.WebGLRenderingContext, canvas: html.Canvas, width: Int, height: Int, aspect: Float, magnification: Int)

case class LoadedTextureAsset(name: String, data: html.Image)