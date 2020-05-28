package indigo.platform.renderer.webgl1

import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLProgram, WebGLTexture}
import indigo.shared.datatypes.Matrix4

import indigo.shared.EqualTo._

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
      translationLocation: WebGLUniformLocation,
      rotationLocation: WebGLUniformLocation,
      scaleLocation: WebGLUniformLocation,
      frameTransform: WebGLUniformLocation
  ): Unit = {
    gl.uniform2f(translationLocation, displayObject.x.toDouble, displayObject.y.toDouble)
    gl.uniform1f(rotationLocation, displayObject.rotation.toDouble)
    gl.uniform2f(scaleLocation, displayObject.width.toDouble, displayObject.height.toDouble)
    gl.uniform4f(
      frameTransform,
      displayObject.frameX.toDouble,
      displayObject.frameY.toDouble,
      displayObject.frameScaleX.toDouble,
      displayObject.frameScaleY.toDouble
    )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lastTextureName: String = ""

  def setupFragmentShaderState(gl: raw.WebGLRenderingContext, texture: WebGLTexture, displayObject: DisplayObject, tintLocation: WebGLUniformLocation): Unit = {
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

  val textureCoordinates/*( d: DisplayObject )*/: scalajs.js.Array[Float] = {
    // val tx1 = if (d.effects.flipHorizontal > 0) 1 - d.frameX else d.frameX
    // val tx2 = if (d.effects.flipHorizontal > 0) 1 - (d.frameScaleX + d.frameX) else d.frameScaleX + d.frameX
    // val ty1 = if (d.effects.flipVertical > 0) 1 - d.frameY else d.frameY
    // val ty2 = if (d.effects.flipVertical > 0) 1 - (d.frameScaleY + d.frameY) else d.frameScaleY + d.frameY

    val tx1 = 0.0f
    val tx2 = 1.0f
    val ty1 = 0.0f
    val ty2 = 1.0f
    // scalajs.js.Array[Float](
    //   tx1,
    //   ty1,

    //   tx1,
    //   ty2,

    //   tx2,
    //   ty1,

    //   tx1,
    //   ty2,

    //   tx2,
    //   ty1,

    //   tx2,
    //   ty2
    // )

    val vert0 = scalajs.js.Array[Float](tx1, ty1)
    val vert1 = scalajs.js.Array[Float](tx1, ty2) // == vert 3
    val vert2 = scalajs.js.Array[Float](tx2, ty1) // == vert 4
    val vert3 = scalajs.js.Array[Float](tx1, ty2)
    val vert4 = scalajs.js.Array[Float](tx2, ty1)
    val vert5 = scalajs.js.Array[Float](tx2, ty2)

    vert0 ++ vert1 ++ vert2 ++ vert3 ++ vert4 ++ vert5
  }

}
