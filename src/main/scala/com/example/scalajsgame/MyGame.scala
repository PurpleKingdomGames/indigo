package com.example.scalajsgame

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}
import org.scalajs.dom.{html, raw}

import scala.scalajs.js.JSApp
import scala.scalajs.js.typedarray.Float32Array

import scala.language.implicitConversions

object MyGame extends JSApp {

  val viewportSize = 256

  def main(): Unit = {

    implicit val cnc: ContextAndCanvas = Engine.createCanvas("canvas", viewportSize, viewportSize)

    Engine.addRectangle(Rectangle2D(0, 0))
//    Engine.addTriangle(Triangle2D(0, 0))

    Engine.drawScene

  }

}

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

  private var renderableThings: List[RenderableThing] = Nil

  def createCanvas(name: String, width: Int, height: Int): html.Canvas = {

    val canvas: html.Canvas = dom.document.createElement(name).asInstanceOf[html.Canvas]
    dom.document.body.appendChild(canvas)
    canvas.width = width
    canvas.height = height

    canvas
  }

  def setupContextAndCanvas(canvas: html.Canvas): ContextAndCanvas = {
    ContextAndCanvas(canvas.getContext("webgl").asInstanceOf[raw.WebGLRenderingContext], canvas)
  }

  private def createVertexBuffer(gl: raw.WebGLRenderingContext, vertices: scalajs.js.Array[Double]): WebGLBuffer = {
    //Create an empty buffer object and store vertex data
    val vertexBuffer: WebGLBuffer = gl.createBuffer()

    //Create a new buffer
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)

    //bind it to the current buffer
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)

    // Pass the buffer data
    gl.bindBuffer(ARRAY_BUFFER, null)

    vertexBuffer
  }

  private def bucketOfShaders(gl: raw.WebGLRenderingContext): WebGLProgram = {
    //vertex shader source code
    val vertCode =
      """
        |attribute vec4 coordinates;
        |uniform vec4 translation;
        |void main(void) {
        |  gl_Position = coordinates + translation;
        |}
      """.stripMargin

    //Create a vertex shader program object and compile it
    val vertShader = gl.createShader(VERTEX_SHADER)
    gl.shaderSource(vertShader, vertCode)
    gl.compileShader(vertShader)

    //fragment shader source code
    val fragCode =
      """
        |void main(void) {
        |   gl_FragColor = vec4(0.9, 0.3, 0.6, 1.0);
        |}
      """.stripMargin

    //Create a fragment shader program object and compile it
    val fragShader = gl.createShader(FRAGMENT_SHADER)
    gl.shaderSource(fragShader, fragCode)
    gl.compileShader(fragShader)

    //Create and use combined shader program
    val shaderProgram = gl.createProgram()
    gl.attachShader(shaderProgram, vertShader)
    gl.attachShader(shaderProgram, fragShader)
    gl.linkProgram(shaderProgram)

    shaderProgram
  }

  private def bindShaderToBuffer(gl: raw.WebGLRenderingContext, vertexBuffer: WebGLBuffer, shaderProgram: WebGLProgram): Unit = {
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
  }

  private def transformDisplayObject(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram, displayObject: DisplayObject): Unit = {
    val translation = gl.getUniformLocation(shaderProgram, "translation")
    gl.uniform4f(translation, displayObject.x, displayObject.y, 0.0, 0.0)
  }

  def drawScene(implicit cNc: ContextAndCanvas): Unit = {
    cNc.context.clearColor(0.5, 0.5, 0.5, 0.9)
    cNc.context.enable(DEPTH_TEST)
    cNc.context.clear(COLOR_BUFFER_BIT)
    cNc.context.viewport(0, 0, cNc.canvas.width, cNc.canvas.height)

    renderableThings.foreach { renderableThing =>

      cNc.context.useProgram(renderableThing.shaderProgram)

      bindShaderToBuffer(cNc.context, renderableThing.vertexBuffer, renderableThing.shaderProgram)
      transformDisplayObject(cNc.context, renderableThing.shaderProgram, renderableThing.displayObject)

      cNc.context.drawArrays(renderableThing.displayObject.mode, 0, renderableThing.displayObject.count)
    }

  }

  def addTriangle(triangle: Triangle2D)(implicit cNc: ContextAndCanvas): Unit = addDisplayObject(triangle)

  def addRectangle(rectangle: Rectangle2D)(implicit cNc: ContextAndCanvas): Unit = addDisplayObject(rectangle)

  private def addDisplayObject(displayObject: DisplayObject)(implicit cNc: ContextAndCanvas): Unit = {
    val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context, displayObject.vertices)
    val shaderProgram = bucketOfShaders(cNc.context)

    renderableThings = RenderableThing(displayObject, shaderProgram, vertexBuffer) :: renderableThings
  }

}

object ContextAndCanvas {
  implicit def canvasToContextAndCanvas(c: html.Canvas): ContextAndCanvas = {
    Engine.setupContextAndCanvas(c)
  }
}
case class ContextAndCanvas(context: raw.WebGLRenderingContext, canvas: html.Canvas)

sealed trait DisplayObject {
  val x: Int
  val y: Int
  val vertices: scalajs.js.Array[Double]
  val count: Int
  val mode: Int //YUK! Wrap this in a real type?
}

case class Triangle2D(x: Int, y: Int) extends DisplayObject {
  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    0,1,0,
    0,0,0,
    1,0,0
  )
  val count: Int = 3
  val mode: Int = TRIANGLES
}

case class Rectangle2D(x: Int, y: Int) extends DisplayObject {

  /*
  B--D
  |\ |
  | \|
  A--C
   */
  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    0,0,0,
    0,1,0,
    1,0,0,
    1,1,0
  )
  val count: Int = 4
  val mode: Int = TRIANGLE_STRIP
}

case class RenderableThing(displayObject: DisplayObject, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer)
