package purple.renderer

import com.example.scalajsgame._
import org.scalajs.dom
import org.scalajs.dom.{html, raw}
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}
import org.scalajs.dom.raw.WebGLRenderingContext._

import scala.scalajs.js.typedarray.Float32Array
import scala.language.implicitConversions


/*
A typical WebGL program basically follows this structure

At Init time

    create all shaders and programs and look up locations
    create buffers and upload vertex data
    create textures and upload texture data

At Render Time

    clear and set the viewport and other global state (enable depth testing, turn on culling, etc..)
    For each thing you want to draw
        call gl.useProgram for the program needed to draw.
        setup attributes for the thing you want to draw
            for each attribute call gl.bindBuffer, gl.vertexAttribPointer, gl.enableVertexAttribArray
        setup uniforms for the thing you want to draw
            call gl.uniformXXX for each uniform
            call gl.activeTexture and gl.bindTexture to assign textures to texture units.
        call gl.drawArrays or gl.drawElements

 */

object Engine {

  //TODO: Remove later when I bring in the fold?
  private var renderableThings: List[RenderableThing] = Nil

  def createCanvas(name: String, width: Int, height: Int): html.Canvas = {

    val canvas: html.Canvas = dom.document.createElement(name).asInstanceOf[html.Canvas]
    dom.document.body.appendChild(canvas)
    canvas.width = width
    canvas.height = height

    canvas
  }

  def setupContextAndCanvas(canvas: html.Canvas): ContextAndCanvas = {
    ContextAndCanvas(
      context = canvas.getContext("webgl").asInstanceOf[raw.WebGLRenderingContext],
      canvas = canvas,
      width = canvas.clientWidth,
      height = canvas.clientHeight,
      aspect = canvas.clientWidth.toFloat / canvas.clientHeight.toFloat
    )
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
        |
        |void main(void) {
        |   //gl_FragColor = vec4(0.9, 0.3, 0.6, 1.0);
        |   gl_FragColor = texture2D(u_texture, v_texcoord);
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

  private def bindShaderToBuffer(gl: raw.WebGLRenderingContext, renderableThing: RenderableThing): Unit = {
    // Vertices
    gl.bindBuffer(ARRAY_BUFFER, renderableThing.vertexBuffer)

    val coordinatesVar = gl.getAttribLocation(renderableThing.shaderProgram, "coordinates")

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
    gl.bindBuffer(ARRAY_BUFFER, renderableThing.textureBuffer)

    val texcoordLocation = gl.getAttribLocation(renderableThing.shaderProgram, "a_texcoord")
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

  private def organiseImage(gl: raw.WebGLRenderingContext, image: html.Image): Unit = {

    val texture = gl.createTexture()

    gl.bindTexture(TEXTURE_2D, texture)

    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_S, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_WRAP_T, CLAMP_TO_EDGE)
    gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)

    gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, image)
    gl.generateMipmap(TEXTURE_2D)
  }

  private def applyTextureLocation(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram): Unit = {
    val u_texture = gl.getUniformLocation(shaderProgram, "u_texture")
    gl.uniform1i(u_texture, 0)
  }

  var tmpX: Double = 0
  var tmpY: Double = 0
  var angle: Double = 0

  private def transformDisplayObject(cNc: ContextAndCanvas, shaderProgram: WebGLProgram, displayObject: DisplayObject): Unit = {
    val translation = cNc.context.getUniformLocation(shaderProgram, "u_matrix")

    //Temporary just to get some movement going.
    tmpX = Math.sin(angle) * 0.5
    tmpY = Math.cos(angle) * 0.5
    angle = angle + 0.01

    val matrix4: Matrix4 =
      Matrix4
        .orthographic(0, cNc.width, cNc.height, 0, -1, 1)
        .translate(displayObject.x, displayObject.y, 0)
        .scale(displayObject.width, displayObject.height, 1)
    //        .translate(tmpX, tmpY, 0.0)

    cNc.context.uniformMatrix4fv(
      location = translation,
      transpose = false,
      value = matrix4
    )
  }

  def drawScene(implicit cNc: ContextAndCanvas): Unit = {
    cNc.context.clearColor(0, 0, 0, 1)
    cNc.context.enable(DEPTH_TEST)
    cNc.context.viewport(0, 0, cNc.width, cNc.height)
    cNc.context.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)
    cNc.context.enable(BLEND)

    dom.window.requestAnimationFrame(Engine.renderLoop(cNc))
  }

  private def resize(canvas: html.Canvas, actualWidth: Int, actualHeight: Int): Unit = {
    if (canvas.width != actualWidth || canvas.height != actualHeight) {
      canvas.width = actualWidth
      canvas.height = actualHeight
    }
  }


  private def renderLoop(cNc: ContextAndCanvas): Double => Unit = (time: Double) => {

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight)

    cNc.context.clear(COLOR_BUFFER_BIT)

    renderableThings.foreach { renderableThing =>

      // Use Program
      cNc.context.useProgram(renderableThing.shaderProgram)

      // Setup attributes
      bindShaderToBuffer(cNc.context, renderableThing)

      // Setup Uniforms
      transformDisplayObject(cNc, renderableThing.shaderProgram, renderableThing.displayObject)
      applyTextureLocation(cNc.context, renderableThing.shaderProgram)

      // Draw
      cNc.context.drawArrays(renderableThing.displayObject.mode, 0, renderableThing.displayObject.vertexCount)
    }

    dom.window.requestAnimationFrame(Engine.renderLoop(cNc))
  }

  def addTriangle(triangle: Triangle2D)(implicit cNc: ContextAndCanvas): Unit = addDisplayObject(triangle)

  def addRectangle(rectangle: Rectangle2D)(implicit cNc: ContextAndCanvas): Unit = addDisplayObject(rectangle)

  private def addDisplayObject(displayObject: DisplayObject)(implicit cNc: ContextAndCanvas): Unit = {

    val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.vertices)
    val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.textureCoordinates)

    organiseImage(cNc.context, displayObject.image)

    val shaderProgram = bucketOfShaders(cNc.context)

    renderableThings = RenderableThing(displayObject, shaderProgram, vertexBuffer, textureBuffer) :: renderableThings
  }

}

object ContextAndCanvas {
  implicit def canvasToContextAndCanvas(c: html.Canvas): ContextAndCanvas = {
    Engine.setupContextAndCanvas(c)
  }
}
case class ContextAndCanvas(context: raw.WebGLRenderingContext, canvas: html.Canvas, width: Int, height: Int, aspect: Float)
