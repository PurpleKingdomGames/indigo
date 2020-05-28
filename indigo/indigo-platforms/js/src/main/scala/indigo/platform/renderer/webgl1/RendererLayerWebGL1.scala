package indigo.platform.renderer.webgl1

import org.scalajs.dom.raw.WebGLRenderingContext
import indigo.platform.renderer.shared.TextureLookupResult
import scala.collection.mutable
import indigo.shared.display.DisplayEntity
import indigo.platform.renderer.shared.FrameBufferComponents
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLProgram
import indigo.platform.renderer.shared.FrameBufferFunctions
import indigo.platform.renderer.shared.RendererHelper
import indigo.shared.display.DisplayObject
import org.scalajs.dom.raw.WebGLRenderingContext._
import scala.scalajs.js.typedarray.Float32Array
import indigo.shared.EqualTo._
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLTexture
import org.scalajs.dom.raw.WebGLUniformLocation

final class RendererLayerWebGL1(gl: WebGLRenderingContext, textureLocations: List[TextureLookupResult]) {

  private val textureBuffer: WebGLBuffer = gl.createBuffer()

  def drawLayer(
      displayEntities: mutable.ListBuffer[DisplayEntity],
      frameBufferComponents: FrameBufferComponents.SingleOutput,
      clearColor: ClearColor,
      shaderProgram: WebGLProgram,
      projection: scalajs.js.Array[Double]
  ): Unit = {

    FrameBufferFunctions.switchToFramebuffer(gl, frameBufferComponents.frameBuffer, clearColor)

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
    val tintLocation    = gl.getUniformLocation(shaderProgram, "u_tint")
    val textureLocation = gl.getUniformLocation(shaderProgram, "u_texture")

    // gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    RendererFunctions.bindAttibuteBuffer(gl, verticesLocation, 3)

    // Set once
    gl.uniform1i(textureLocation, 0)

    RendererHelper.sortByDepth(displayEntities).foreach {
      case displayObject: DisplayObject =>
        gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
        gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.textureCoordinates(displayObject)), STATIC_DRAW)
        RendererFunctions.bindAttibuteBuffer(gl, texcoordLocation, 2)

        RendererFunctions.setupVertexShaderState(gl, displayObject, translationLocation, rotationLocation, scaleLocation)

        textureLocations.find(t => t.name === displayObject.atlasName).foreach { textureLookup =>
          setupFragmentShaderState(gl, textureLookup.texture, displayObject, tintLocation)
        }

        gl.drawArrays(TRIANGLES, 0, RendererHelper.vertexCount)

      case _ =>
        ()
    }

  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lastTextureName: String = ""

  def setupFragmentShaderState(gl: WebGLRenderingContext, texture: WebGLTexture, displayObject: DisplayObject, tintLocation: WebGLUniformLocation): Unit = {
    if (displayObject.atlasName !== lastTextureName) {
      gl.bindTexture(TEXTURE_2D, texture)
      lastTextureName = displayObject.atlasName
    }

    gl.uniform4f(
      tintLocation,
      displayObject.effects.tint(0).toDouble,
      displayObject.effects.tint(1).toDouble,
      displayObject.effects.tint(2).toDouble,
      displayObject.effects.tint(3).toDouble
    )
  }

}
