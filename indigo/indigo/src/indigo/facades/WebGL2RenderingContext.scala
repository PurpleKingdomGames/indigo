package indigo.facades

import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.*

@nowarn
@js.native
trait WebGL2RenderingContext extends WebGLRenderingContext {

  // A whole bunch of uniform constants..
  // https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Constants#Uniforms
  val UNIFORM_BUFFER: Int                              = js.native // 0x8a11
  val UNIFORM_BUFFER_BINDING: Int                      = js.native // 0x8a28
  val UNIFORM_BUFFER_START: Int                        = js.native // 0x8a29
  val UNIFORM_BUFFER_SIZE: Int                         = js.native // 0x8a2a
  val MAX_VERTEX_UNIFORM_BLOCKS: Int                   = js.native // 0x8a2b
  val MAX_FRAGMENT_UNIFORM_BLOCKS: Int                 = js.native // 0x8a2d
  val MAX_COMBINED_UNIFORM_BLOCKS: Int                 = js.native // 0x8a2e
  val MAX_UNIFORM_BUFFER_BINDINGS: Int                 = js.native // 0x8a2f
  val MAX_UNIFORM_BLOCK_SIZE: Int                      = js.native // 0x8a30
  val MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS: Int      = js.native // 0x8a31
  val MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS: Int    = js.native // 0x8a33
  val UNIFORM_BUFFER_OFFSET_ALIGNMENT: Int             = js.native // 0x8a34
  val ACTIVE_UNIFORM_BLOCKS: Int                       = js.native // 0x8a36
  val UNIFORM_TYPE: Int                                = js.native // 0x8a37
  val UNIFORM_SIZE: Int                                = js.native // 0x8a38
  val UNIFORM_BLOCK_INDEX: Int                         = js.native // 0x8a3a
  val UNIFORM_OFFSET: Int                              = js.native // 0x8a3b
  val UNIFORM_ARRAY_STRIDE: Int                        = js.native // 0x8a3c
  val UNIFORM_MATRIX_STRIDE: Int                       = js.native // 0x8a3d
  val UNIFORM_IS_ROW_MAJOR: Int                        = js.native // 0x8a3e
  val UNIFORM_BLOCK_BINDING: Int                       = js.native // 0x8a3f
  val UNIFORM_BLOCK_DATA_SIZE: Int                     = js.native // 0x8a40
  val UNIFORM_BLOCK_ACTIVE_UNIFORMS: Int               = js.native // 0x8a42
  val UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES: Int        = js.native // 0x8a43
  val UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER: Int   = js.native // 0x8a44
  val UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER: Int = js.native // 0x8a46
  val MAX_3D_TEXTURE_SIZE: Int                         = js.native // 0x8073
  val MAX_DRAW_BUFFERS: Int                            = js.native // 0x8824
  val MAX_COLOR_ATTACHMENTS: Int                       = js.native // 0x8cdf
  val MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int              = js.native // 0x8b4c
  val MAX_FRAGMENT_INPUT_COMPONENTS: Int               = js.native // 0x9125

  val READ_FRAMEBUFFER: Int = js.native // 0x8CA8
  val DRAW_FRAMEBUFFER: Int = js.native // 0x8CA9

  val COLOR_ATTACHMENT1: Int  = js.native // 0x8ce1
  val COLOR_ATTACHMENT2: Int  = js.native // 0x8ce2
  val COLOR_ATTACHMENT3: Int  = js.native // 0x8ce3
  val COLOR_ATTACHMENT4: Int  = js.native // 0x8ce4
  val COLOR_ATTACHMENT5: Int  = js.native // 0x8ce5
  val COLOR_ATTACHMENT6: Int  = js.native // 0x8ce6
  val COLOR_ATTACHMENT7: Int  = js.native // 0x8ce7
  val COLOR_ATTACHMENT8: Int  = js.native // 0x8ce8
  val COLOR_ATTACHMENT9: Int  = js.native // 0x8ce9
  val COLOR_ATTACHMENT10: Int = js.native // 0x8cea
  val COLOR_ATTACHMENT11: Int = js.native // 0x8ceb
  val COLOR_ATTACHMENT12: Int = js.native // 0x8cec
  val COLOR_ATTACHMENT13: Int = js.native // 0x8ced
  val COLOR_ATTACHMENT14: Int = js.native // 0x8cee
  val COLOR_ATTACHMENT15: Int = js.native // 0x8cef

  // WebGL2 Blend equations
  val MIN: Int = js.native // 0x8007
  val MAX: Int = js.native // 0x8008

  def uniformBlockBinding(program: WebGLProgram, uniformBlockIndex: Double, uniformBlockBinding: Int): Unit =
    js.native

  def bindBufferBase(target: Int, index: Int, buffer: WebGLBuffer): Unit =
    js.native

  def getUniformBlockIndex(program: WebGLProgram, uniformBlockName: String): Double =
    js.native

  def bindBufferRange(target: Int, index: Int, buffer: WebGLBuffer, offset: Int, size: Int): Unit =
    js.native

  def drawArraysInstanced(mode: Int, first: Int, count: Int, instanceCount: Int): Unit =
    js.native

  def vertexAttribDivisor(index: Int, divisor: Int): Unit =
    js.native

  def createVertexArray(): WebGLVertexArrayObject =
    js.native

  def bindVertexArray(vertexArray: WebGLVertexArrayObject): Unit =
    js.native

  def drawBuffers(buffers: scalajs.js.Array[Int]): Unit =
    js.native

  def blitFramebuffer(
      srcX0: Int,
      srcY0: Int,
      srcX1: Int,
      srcY1: Int,
      dstX0: Int,
      dstY0: Int,
      dstX1: Int,
      dstY1: Int,
      mask: Int,
      filter: Int
  ): Unit =
    js.native

}

object WebGL2RenderingContext {
  // A whole bunch of uniform constants..
  // https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Constants#Uniforms
  val UNIFORM_BUFFER: Int                              = 0x8a11
  val UNIFORM_BUFFER_BINDING: Int                      = 0x8a28
  val UNIFORM_BUFFER_START: Int                        = 0x8a29
  val UNIFORM_BUFFER_SIZE: Int                         = 0x8a2a
  val MAX_VERTEX_UNIFORM_BLOCKS: Int                   = 0x8a2b
  val MAX_FRAGMENT_UNIFORM_BLOCKS: Int                 = 0x8a2d
  val MAX_COMBINED_UNIFORM_BLOCKS: Int                 = 0x8a2e
  val MAX_UNIFORM_BUFFER_BINDINGS: Int                 = 0x8a2f
  val MAX_UNIFORM_BLOCK_SIZE: Int                      = 0x8a30
  val MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS: Int      = 0x8a31
  val MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS: Int    = 0x8a33
  val UNIFORM_BUFFER_OFFSET_ALIGNMENT: Int             = 0x8a34
  val ACTIVE_UNIFORM_BLOCKS: Int                       = 0x8a36
  val UNIFORM_TYPE: Int                                = 0x8a37
  val UNIFORM_SIZE: Int                                = 0x8a38
  val UNIFORM_BLOCK_INDEX: Int                         = 0x8a3a
  val UNIFORM_OFFSET: Int                              = 0x8a3b
  val UNIFORM_ARRAY_STRIDE: Int                        = 0x8a3c
  val UNIFORM_MATRIX_STRIDE: Int                       = 0x8a3d
  val UNIFORM_IS_ROW_MAJOR: Int                        = 0x8a3e
  val UNIFORM_BLOCK_BINDING: Int                       = 0x8a3f
  val UNIFORM_BLOCK_DATA_SIZE: Int                     = 0x8a40
  val UNIFORM_BLOCK_ACTIVE_UNIFORMS: Int               = 0x8a42
  val UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES: Int        = 0x8a43
  val UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER: Int   = 0x8a44
  val UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER: Int = 0x8a46
  val MAX_3D_TEXTURE_SIZE: Int                         = 0x8073
  val MAX_DRAW_BUFFERS: Int                            = 0x8824
  val MAX_COLOR_ATTACHMENTS: Int                       = 0x8cdf
  val MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int              = 0x8b4c
  val MAX_FRAGMENT_INPUT_COMPONENTS: Int               = 0x9125
  val MAX_VARYING_VECTORS: Int                         = 0x8dfc
  val MIN: Int                                         = 0x8007
  val MAX: Int                                         = 0x8008

  val READ_FRAMEBUFFER: Int = 0x8ca8
  val DRAW_FRAMEBUFFER: Int = 0x8ca9
}

@nowarn
@js.native
@JSGlobal
class WebGLVertexArrayObject private[this] () extends js.Object

object ColorAttachments {
  val COLOR_ATTACHMENT0: Int  = 0x8ce0
  val COLOR_ATTACHMENT1: Int  = 0x8ce1
  val COLOR_ATTACHMENT2: Int  = 0x8ce2
  val COLOR_ATTACHMENT3: Int  = 0x8ce3
  val COLOR_ATTACHMENT4: Int  = 0x8ce4
  val COLOR_ATTACHMENT5: Int  = 0x8ce5
  val COLOR_ATTACHMENT6: Int  = 0x8ce6
  val COLOR_ATTACHMENT7: Int  = 0x8ce7
  val COLOR_ATTACHMENT8: Int  = 0x8ce8
  val COLOR_ATTACHMENT9: Int  = 0x8ce9
  val COLOR_ATTACHMENT10: Int = 0x8cea
  val COLOR_ATTACHMENT11: Int = 0x8ceb
  val COLOR_ATTACHMENT12: Int = 0x8cec
  val COLOR_ATTACHMENT13: Int = 0x8ced
  val COLOR_ATTACHMENT14: Int = 0x8cee
  val COLOR_ATTACHMENT15: Int = 0x8cef

  def intToColorAttachment(i: Int): Int =
    i match {
      case 0  => COLOR_ATTACHMENT0
      case 1  => COLOR_ATTACHMENT1
      case 2  => COLOR_ATTACHMENT2
      case 3  => COLOR_ATTACHMENT3
      case 4  => COLOR_ATTACHMENT4
      case 5  => COLOR_ATTACHMENT5
      case 6  => COLOR_ATTACHMENT6
      case 7  => COLOR_ATTACHMENT7
      case 8  => COLOR_ATTACHMENT8
      case 9  => COLOR_ATTACHMENT9
      case 10 => COLOR_ATTACHMENT10
      case 11 => COLOR_ATTACHMENT11
      case 12 => COLOR_ATTACHMENT12
      case 13 => COLOR_ATTACHMENT13
      case 14 => COLOR_ATTACHMENT14
      case 15 => COLOR_ATTACHMENT15
      case _  => COLOR_ATTACHMENT0
    }
}
