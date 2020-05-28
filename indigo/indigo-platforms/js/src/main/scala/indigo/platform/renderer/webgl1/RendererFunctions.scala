package indigo.platform.renderer.webgl1

import org.scalajs.dom.raw
import org.scalajs.dom.raw.WebGLRenderingContext._

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

  def setupVertexShaderState(
      gl: raw.WebGLRenderingContext,
      displayObject: DisplayObject,
      translationLocation: WebGLUniformLocation,
      rotationLocation: WebGLUniformLocation,
      scaleLocation: WebGLUniformLocation
  ): Unit = {
    gl.uniform2f(translationLocation, displayObject.x.toDouble, displayObject.y.toDouble)
    gl.uniform1f(rotationLocation, 0.0d)
    gl.uniform2f(scaleLocation, displayObject.width.toDouble, displayObject.height.toDouble)
  }

  def textureCoordinates(d: DisplayObject): scalajs.js.Array[Float] = {
    val tx1 = if (d.effects.flipHorizontal > 0) 1 - d.frameX else d.frameX
    val tx2 = if (d.effects.flipHorizontal > 0) 1 - (d.frameScaleX + d.frameX) else d.frameScaleX + d.frameX
    val ty1 = if (d.effects.flipVertical > 0) 1 - d.frameY else d.frameY
    val ty2 = if (d.effects.flipVertical > 0) 1 - (d.frameScaleY + d.frameY) else d.frameScaleY + d.frameY

    scalajs.js.Array[Float](
      tx1,
      ty1,
      tx1,
      ty2,
      tx2,
      ty1,
      tx1,
      ty2,
      tx2,
      ty1,
      tx2,
      ty2
    )
  }

}
