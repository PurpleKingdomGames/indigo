package indigo.platform.renderer.webgl1

import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLProgram, WebGLTexture}
import indigo.shared.datatypes.Matrix4

import indigo.shared.display.DisplayObject
import org.scalajs.dom.raw.WebGLUniformLocation

object RendererFunctions {

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

  val flipMatrix: ((Boolean, Boolean)) => Matrix4 = {
    case (true, true)   => Matrix4.identity.translate(1, 1, 0).scale(-1, -1, -1)
    case (true, false)  => Matrix4.identity.translate(1, 0, 0).scale(-1, 1, -1)
    case (false, true)  => Matrix4.identity.translate(0, 1, 0).scale(1, -1, -1)
    case (false, false) => Matrix4.identity
  }

  def setupVertexShaderState(
      gl: raw.WebGLRenderingContext,
      displayObject: DisplayObject,
      transformLocation: WebGLUniformLocation,
      dimensions: WebGLUniformLocation,
      rotationAlphaFlipLocation: WebGLUniformLocation,
      frameTransform: WebGLUniformLocation
  ): Unit = {

    gl.uniform4f(
      transformLocation,
      displayObject.x.toDouble,
      displayObject.y.toDouble,
      displayObject.scaleX.toDouble,
      displayObject.scaleY.toDouble
    )

    gl.uniform4f(
      dimensions,
      displayObject.refX.toDouble,
      displayObject.refY.toDouble,
      displayObject.width.toDouble,
      displayObject.height.toDouble
    )

    gl.uniform4f(
      rotationAlphaFlipLocation,
      displayObject.rotation.toDouble,
      displayObject.effects.alpha.toDouble,
      displayObject.effects.flipHorizontal.toDouble,
      displayObject.effects.flipVertical.toDouble
    )

    gl.uniform4f(
      frameTransform,
      displayObject.frameX.toDouble,
      displayObject.frameY.toDouble,
      displayObject.frameScaleX.toDouble,
      displayObject.frameScaleY.toDouble
    )
  }

}
