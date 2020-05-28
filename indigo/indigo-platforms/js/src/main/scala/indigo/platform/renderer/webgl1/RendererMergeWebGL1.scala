package indigo.platform.renderer.webgl1

import org.scalajs.dom.raw.WebGLRenderingContext
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLProgram
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.RendererHelper
import indigo.shared.display.DisplayObject
import org.scalajs.dom.raw.WebGLRenderingContext._
import scala.scalajs.js.typedarray.Float32Array
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLTexture

final class RendererMergeWebGL1(gl: WebGLRenderingContext) {

  private val textureBuffer: WebGLBuffer = gl.createBuffer()

  def drawLayer(
      clearColor: ClearColor,
      width: Int,
      height: Int,
      shaderProgram: WebGLProgram,
      projection: scalajs.js.Array[Double],
      gameFrameBuffer: FrameBufferComponents.SingleOutput,
      lightingFrameBuffer: FrameBufferComponents.SingleOutput,
      uiFrameBuffer: FrameBufferComponents.SingleOutput
  ): Unit = {

    val displayObject: DisplayObject = RendererHelper.screenDisplayObject(width, height)

    FrameBufferFunctions.switchToCanvas(gl, clearColor)

    gl.useProgram(shaderProgram)

    gl.uniformMatrix4fv(
      location = gl.getUniformLocation(shaderProgram, "u_projection"),
      transpose = false,
      value = projection
    )

    // Attribute locations
    val verticesLocation = gl.getAttribLocation(shaderProgram, "a_vertices")
    val texcoordLocation = gl.getAttribLocation(shaderProgram, "a_texcoord")

    // Uniform locations (vertex)
    val translationLocation = gl.getUniformLocation(shaderProgram, "u_translation")
    val rotationLocation    = gl.getUniformLocation(shaderProgram, "u_rotation")
    val scaleLocation       = gl.getUniformLocation(shaderProgram, "u_scale")

    // Uniform locations (fragment)
    val textureLocation = gl.getUniformLocation(shaderProgram, "u_texture")

    // gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    RendererFunctions.bindAttibuteBuffer(gl, verticesLocation, 3)

    // Set once
    gl.uniform1i(textureLocation, 0)

    gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.textureCoordinates(displayObject)), STATIC_DRAW)

    RendererFunctions.bindAttibuteBuffer(gl, texcoordLocation, 2)

    RendererFunctions.setupVertexShaderState(gl, displayObject, translationLocation, rotationLocation, scaleLocation)

    setupMergeFragmentShaderState(
      gl,
      shaderProgram,
      gameFrameBuffer.diffuse,
      lightingFrameBuffer.diffuse,
      uiFrameBuffer.diffuse
    )

    gl.drawArrays(TRIANGLES, 0, RendererHelper.vertexCount)

  }

  def setupMergeFragmentShaderState(gl: WebGLRenderingContext, shaderProgram: WebGLProgram, textureGame: WebGLTexture, textureLighting: WebGLTexture, textureUi: WebGLTexture): Unit = {

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

}
