package com.example.scalajsgame

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}
import org.scalajs.dom.{html, raw}

import scala.scalajs.js.JSApp
import scala.scalajs.js.typedarray.Float32Array

object MyGame extends JSApp {

  val viewportSize = 256

  def createCanvas(): html.Canvas = {

    val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
    dom.document.body.appendChild(canvas)
    canvas.width = viewportSize
    canvas.height = viewportSize

    canvas
  }

  def initGL(canvas: html.Canvas): raw.WebGLRenderingContext = {
    canvas.getContext("webgl").asInstanceOf[raw.WebGLRenderingContext]
  }

  def createTriangle(): scalajs.js.Array[Double] = {
    scalajs.js.Array[Double](
      -0.5,0.5,0.0,
      -0.5,-0.5,0.0,
      0.5,-0.5,0.0
    )
  }

  def createVertexBuffer(gl: raw.WebGLRenderingContext, vertices: scalajs.js.Array[Double]): WebGLBuffer = {
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

  def bucketOfShaders(gl: raw.WebGLRenderingContext): WebGLProgram = {
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

    gl.useProgram(shaderProgram)

    shaderProgram
  }

  def bindShaderToBuffer(gl: raw.WebGLRenderingContext, vertexBuffer: WebGLBuffer, shaderProgram: WebGLProgram): Unit = {
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

  def transformTriangle(gl: raw.WebGLRenderingContext, shaderProgram: WebGLProgram): Unit = {
    val Tx = 0.0 //0.5
    val Ty = 0.0 //0.5
    val Tz = 0.0
    val translation = gl.getUniformLocation(shaderProgram, "translation")
    gl.uniform4f(translation, Tx, Ty, Tz, 0.0)
  }

  def drawScene(canvas: html.Canvas, gl: raw.WebGLRenderingContext): Unit = {
    gl.clearColor(0.5, 0.5, 0.5, 0.9)
    gl.enable(DEPTH_TEST)

    gl.clear(COLOR_BUFFER_BIT)
    gl.viewport(0, 0, canvas.width, canvas.height)
    gl.drawArrays(TRIANGLES, 0, 3)
  }

  def main(): Unit = {

    val canvas: html.Canvas = createCanvas()
    val gl: raw.WebGLRenderingContext = initGL(canvas)
    val vertexBuffer: WebGLBuffer = createVertexBuffer(gl, createTriangle())
    val shaderProgram = bucketOfShaders(gl)

    bindShaderToBuffer(gl, vertexBuffer, shaderProgram)

    drawScene(canvas, gl)

  }

}

object Engine {
  
}

//REFERENCE
////////////

// V2

/*
val viewportSize = 256

def main(): Unit = {

  println("Starting up")

  val gl = initGL()

  val progDyn = setupShaders(gl)

  val buffer = setupBuffers(gl)

  drawScene(gl, buffer, progDyn)

}

def initGL(): raw.WebGLRenderingContext = {

  val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
  dom.document.body.appendChild(canvas)
  canvas.width = viewportSize
  canvas.height = viewportSize

  val gl: raw.WebGLRenderingContext = canvas.getContext("webgl").asInstanceOf[raw.WebGLRenderingContext]
  gl.viewport(0, 0, viewportSize, viewportSize)
  gl.clearColor(0.4, 0.0, 0.5, 0.8)
  gl.clear(COLOR_BUFFER_BIT)
  gl.enable(DEPTH_TEST)

  gl
}

def setupShaders(gl: raw.WebGLRenderingContext): scalajs.js.Dynamic = {

  val vertexShader =
    """
      |attribute vec4 Position;
      |
      |uniform vec4 u_ModelView;
      |
      |void main(void) {
      |    gl_Position = u_ModelView * Position;
      |}
    """.stripMargin

  val fragmentShader =
    """
      |precision mediump float;
      |
      |void main(void) {
      |    gl_FragColor = vec4(0.9, 0.3, 0.6, 1.0);
      |}
    """.stripMargin

  val vShader = gl.createShader(VERTEX_SHADER)
  gl.shaderSource(vShader, vertexShader)
  gl.compileShader(vShader)

  println("Vertex compile status: " + gl.getShaderParameter(vShader, COMPILE_STATUS))

  val fShader = gl.createShader(FRAGMENT_SHADER)
  gl.shaderSource(fShader, fragmentShader)
  gl.compileShader(fShader)

  println("Fragment compile status: " + gl.getShaderParameter(fShader, COMPILE_STATUS))

  val program = gl.createProgram()
  gl.attachShader(program, vShader)
  gl.attachShader(program, fShader)
  gl.linkProgram(program)

  println("Program link status: " + gl.getProgramParameter(program, LINK_STATUS))

  gl.useProgram(program)

  val progDyn = program.asInstanceOf[scalajs.js.Dynamic]

  progDyn.positionLocation = gl.getAttribLocation(program, "Position")

  gl.enableVertexAttribArray(progDyn.positionLocation.asInstanceOf[Int])

//    progDyn.u_PerspLocation = gl.getUniformLocation(program, "u_Persp")
  progDyn.u_ModelViewLocation = gl.getUniformLocation(program, "u_ModelView")

  progDyn
}

def setupBuffers(gl: raw.WebGLRenderingContext): WebGLBuffer = {

  val vertices = scalajs.js.Array[Float]()
  vertices.push(
     0.0f,  1.0f, 0.0f,
    -1.0f, -1.0f, 0.0f,
     1.0f, -1.0f, 0.0f
  )

  val buffer = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, buffer)
  gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)

  buffer
}

def drawScene(gl: raw.WebGLRenderingContext, buffer: WebGLBuffer, progDyn: scalajs.js.Dynamic): Unit = {

  val pMatrix = Matrix4d()//.perspective(45f, (viewportSize / viewportSize).asInstanceOf[Double], 0.1f, 100f, false)
  //val mvMatrix = Matrix4d().translate(0f, 0f, -4f)

  //println("p: " + pMatrix)
  //println("m: " + mvMatrix)

  gl.viewport(0, 0, viewportSize, viewportSize)
  gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT)

  //Pass triangle position to vertex shader
  gl.bindBuffer(ARRAY_BUFFER, buffer)
  gl.vertexAttribPointer(
    indx = progDyn.positionLocation.asInstanceOf[Int],
    size = 3,
    `type` = FLOAT,
    normalized = false,
    stride = 0,
    offset = 0
  )

  val perspLocation = progDyn.u_PerspLocation.asInstanceOf[raw.WebGLUniformLocation]
  val moveLocation = progDyn.u_ModelViewLocation.asInstanceOf[raw.WebGLUniformLocation]

  //Pass model view projection matrix to vertex shader
  gl.uniformMatrix4fv(perspLocation, false, pMatrix)
  //gl.uniformMatrix4fv(progDyn.u_ModelViewLocation.asInstanceOf[raw.WebGLUniformLocation], false, mvMatrix)

  gl.uniform4f(moveLocation, 0d, 0d, -4d, 0d)

  //Draw our lovely triangle
  gl.drawArrays(TRIANGLES, 0, 3)

}
*/

/// V1

/*
def main(): Unit = {
  println("Hello world!")

  val vertexShader =
    """
      |void main()
      |{
      |    // Transforming The Vertex
      |    //gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
      |}
    """.stripMargin

  val fragmentShader =
    """
      |void main()
      |{
      |    // Setting Each Pixel To Red
      |    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
      |}
    """.stripMargin

  val canvas: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
  dom.document.body.appendChild(canvas)
  canvas.width = 256
  canvas.height = 256

  val gl: raw.WebGLRenderingContext = canvas.getContext("webgl").asInstanceOf[raw.WebGLRenderingContext]
  gl.clearColor(0.4, 0.0, 0.5, 0.8)
  gl.clear(COLOR_BUFFER_BIT)

  val vShader = gl.createShader(VERTEX_SHADER)
//    val vertText = "gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex" //"attribute vec2 position;gl_Position = vec4(position, 0, 1);"
  gl.shaderSource(vShader, vertexShader)
  gl.compileShader(vShader)
  println(gl.getShaderParameter(vShader, COMPILE_STATUS))

  //    val fragText = "precision highp float;uniform vec4 color;gl_FragColor = vec4(0, 1, 0, 1);"
  val fShader = gl.createShader(FRAGMENT_SHADER)
  gl.shaderSource(fShader, fragmentShader)
  gl.compileShader(fShader)

  val program = gl.createProgram()
  gl.attachShader(program, vShader)
  gl.attachShader(program, fShader)
  gl.linkProgram(program)

  val tempVertices: scalajs.js.Array[Float] = scalajs.js.Array[Float]()
  tempVertices.push(-0.3f,-0.3f,   0.3f,-0.3f,  0.0f,0.3f,  0.2f,0.2f,   0.6f, 0.6f,   0.4f, -0.4f)

  val vertices: Float32Array = new Float32Array(tempVertices)

  val buffer = gl.createBuffer()
  gl.bindBuffer(ARRAY_BUFFER, buffer)
  gl.bufferData(ARRAY_BUFFER, vertices, STATIC_DRAW)

  gl.useProgram(program)
//    val progDyn = program.asInstanceOf[scalajs.js.Dynamic]
//    progDyn.color = gl.getUniformLocation(program, "color")
//    val temp2 = scalajs.js.Array[Double]()
//    temp2.push(0f, 1f, 0.5f, 1.0f)
//    gl.uniform4fv(progDyn.color.asInstanceOf[raw.WebGLUniformLocation], temp2)
//
//    progDyn.position = gl.getAttribLocation(program, "position")
//    gl.enableVertexAttribArray(progDyn.position.asInstanceOf[Int])
//    gl.vertexAttribPointer(progDyn.position.asInstanceOf[Int], 2, FLOAT, false, 0, 0)
  gl.drawArrays(TRIANGLES, 0, vertices.length / 2)
}
*/