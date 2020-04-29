package indigo.platform.renderer

import indigo.shared.display.DisplayObject
import indigo.shared.display.SpriteSheetFrame
import indigo.shared.datatypes.Vector2
import indigo.shared.display.DisplayEffects
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.facades.WebGL2RenderingContext
import org.scalajs.dom.raw.WebGLProgram
import org.scalajs.dom.raw.WebGLTexture

object RendererHelper {

  def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
      rotation = 0,
      scaleX = 1,
      scaleY = 1,
      atlasName = "",
      frame = SpriteSheetFrame.defaultOffset,
      albedoAmount = 1.0f,
      emissiveOffset = Vector2.zero,
      emissiveAmount = 0.0f,
      normalOffset = Vector2.zero,
      normalAmount = 0.0f,
      specularOffset = Vector2.zero,
      specularAmount = 0.0f,
      isLit = 0.0f,
      refX = 0,
      refY = 0,
      effects = DisplayEffects.default
    )

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def intToTextureLocation: Int => Int = {
    case 0  => TEXTURE0
    case 1  => TEXTURE1
    case 2  => TEXTURE2
    case 3  => TEXTURE3
    case 4  => TEXTURE4
    case 5  => TEXTURE5
    case 6  => TEXTURE6
    case 7  => TEXTURE7
    case 8  => TEXTURE8
    case 9  => TEXTURE9
    case 10 => TEXTURE10
    case 11 => TEXTURE11
    case 12 => TEXTURE12
    case 13 => TEXTURE13
    case 14 => TEXTURE14
    case 15 => TEXTURE15
    case 16 => TEXTURE16
    case 17 => TEXTURE17
    case 18 => TEXTURE18
    case 19 => TEXTURE19
    case 20 => TEXTURE20
    case 21 => TEXTURE21
    case 22 => TEXTURE22
    case 23 => TEXTURE23
    case 24 => TEXTURE24
    case 25 => TEXTURE25
    case 26 => TEXTURE26
    case 27 => TEXTURE27
    case 28 => TEXTURE28
    case 29 => TEXTURE29
    case 30 => TEXTURE30
    case 31 => TEXTURE31
    case _  => throw new Exception("Cannot assign > 32 texture locations.")
  }

  def attach(gl2: WebGL2RenderingContext, shaderProgram: WebGLProgram, location: Int, uniformName: String, texture: WebGLTexture): Unit = {
    gl2.uniform1i(gl2.getUniformLocation(shaderProgram, uniformName), location)
    gl2.activeTexture(intToTextureLocation(location))
    gl2.bindTexture(TEXTURE_2D, texture)
  }

}
