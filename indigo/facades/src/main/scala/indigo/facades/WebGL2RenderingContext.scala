package indigo.facades

import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLProgram
import org.scalajs.dom.raw.WebGLRenderingContext

import scala.scalajs.js
import scala.scalajs.js.annotation._

import scala.annotation.nowarn

@nowarn
@js.native
trait WebGL2RenderingContext extends WebGLRenderingContext {

  // A whole bunch of uniform constants..
  // https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Constants#Uniforms
  final val UNIFORM_BUFFER: Int                              = js.native // 0x8a11
  final val UNIFORM_BUFFER_BINDING: Int                      = js.native // 0x8a28
  final val UNIFORM_BUFFER_START: Int                        = js.native // 0x8a29
  final val UNIFORM_BUFFER_SIZE: Int                         = js.native // 0x8a2a
  final val MAX_VERTEX_UNIFORM_BLOCKS: Int                   = js.native // 0x8a2b
  final val MAX_FRAGMENT_UNIFORM_BLOCKS: Int                 = js.native // 0x8a2d
  final val MAX_COMBINED_UNIFORM_BLOCKS: Int                 = js.native // 0x8a2e
  final val MAX_UNIFORM_BUFFER_BINDINGS: Int                 = js.native // 0x8a2f
  final val MAX_UNIFORM_BLOCK_SIZE: Int                      = js.native // 0x8a30
  final val MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS: Int      = js.native // 0x8a31
  final val MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS: Int    = js.native // 0x8a33
  final val UNIFORM_BUFFER_OFFSET_ALIGNMENT: Int             = js.native // 0x8a34
  final val ACTIVE_UNIFORM_BLOCKS: Int                       = js.native // 0x8a36
  final val UNIFORM_TYPE: Int                                = js.native // 0x8a37
  final val UNIFORM_SIZE: Int                                = js.native // 0x8a38
  final val UNIFORM_BLOCK_INDEX: Int                         = js.native // 0x8a3a
  final val UNIFORM_OFFSET: Int                              = js.native // 0x8a3b
  final val UNIFORM_ARRAY_STRIDE: Int                        = js.native // 0x8a3c
  final val UNIFORM_MATRIX_STRIDE: Int                       = js.native // 0x8a3d
  final val UNIFORM_IS_ROW_MAJOR: Int                        = js.native // 0x8a3e
  final val UNIFORM_BLOCK_BINDING: Int                       = js.native // 0x8a3f
  final val UNIFORM_BLOCK_DATA_SIZE: Int                     = js.native // 0x8a40
  final val UNIFORM_BLOCK_ACTIVE_UNIFORMS: Int               = js.native // 0x8a42
  final val UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES: Int        = js.native // 0x8a43
  final val UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER: Int   = js.native // 0x8a44
  final val UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER: Int = js.native // 0x8a46
  final val MAX_3D_TEXTURE_SIZE: Int                         = js.native // 0x8073
  final val MAX_DRAW_BUFFERS: Int                            = js.native // 0x8824
  final val MAX_COLOR_ATTACHMENTS: Int                       = js.native // 0x8cdf
  final val MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int              = js.native // 0x8b4c
  final val MAX_FRAGMENT_INPUT_COMPONENTS: Int               = js.native // 0x9125

  final val COLOR_ATTACHMENT1: Int  = js.native // 0x8ce1
  final val COLOR_ATTACHMENT2: Int  = js.native // 0x8ce2
  final val COLOR_ATTACHMENT3: Int  = js.native // 0x8ce3
  final val COLOR_ATTACHMENT4: Int  = js.native // 0x8ce4
  final val COLOR_ATTACHMENT5: Int  = js.native // 0x8ce5
  final val COLOR_ATTACHMENT6: Int  = js.native // 0x8ce6
  final val COLOR_ATTACHMENT7: Int  = js.native // 0x8ce7
  final val COLOR_ATTACHMENT8: Int  = js.native // 0x8ce8
  final val COLOR_ATTACHMENT9: Int  = js.native // 0x8ce9
  final val COLOR_ATTACHMENT10: Int = js.native // 0x8cea
  final val COLOR_ATTACHMENT11: Int = js.native // 0x8ceb
  final val COLOR_ATTACHMENT12: Int = js.native // 0x8cec
  final val COLOR_ATTACHMENT13: Int = js.native // 0x8ced
  final val COLOR_ATTACHMENT14: Int = js.native // 0x8cee
  final val COLOR_ATTACHMENT15: Int = js.native // 0x8cef

  def getUniformBlockIndex(program: WebGLProgram, uniformBlockName: String): Int =
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

}

object WebGL2RenderingContext {
  // A whole bunch of uniform constants..
  // https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Constants#Uniforms
  final val UNIFORM_BUFFER: Int                              = 0x8a11
  final val UNIFORM_BUFFER_BINDING: Int                      = 0x8a28
  final val UNIFORM_BUFFER_START: Int                        = 0x8a29
  final val UNIFORM_BUFFER_SIZE: Int                         = 0x8a2a
  final val MAX_VERTEX_UNIFORM_BLOCKS: Int                   = 0x8a2b
  final val MAX_FRAGMENT_UNIFORM_BLOCKS: Int                 = 0x8a2d
  final val MAX_COMBINED_UNIFORM_BLOCKS: Int                 = 0x8a2e
  final val MAX_UNIFORM_BUFFER_BINDINGS: Int                 = 0x8a2f
  final val MAX_UNIFORM_BLOCK_SIZE: Int                      = 0x8a30
  final val MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS: Int      = 0x8a31
  final val MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS: Int    = 0x8a33
  final val UNIFORM_BUFFER_OFFSET_ALIGNMENT: Int             = 0x8a34
  final val ACTIVE_UNIFORM_BLOCKS: Int                       = 0x8a36
  final val UNIFORM_TYPE: Int                                = 0x8a37
  final val UNIFORM_SIZE: Int                                = 0x8a38
  final val UNIFORM_BLOCK_INDEX: Int                         = 0x8a3a
  final val UNIFORM_OFFSET: Int                              = 0x8a3b
  final val UNIFORM_ARRAY_STRIDE: Int                        = 0x8a3c
  final val UNIFORM_MATRIX_STRIDE: Int                       = 0x8a3d
  final val UNIFORM_IS_ROW_MAJOR: Int                        = 0x8a3e
  final val UNIFORM_BLOCK_BINDING: Int                       = 0x8a3f
  final val UNIFORM_BLOCK_DATA_SIZE: Int                     = 0x8a40
  final val UNIFORM_BLOCK_ACTIVE_UNIFORMS: Int               = 0x8a42
  final val UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES: Int        = 0x8a43
  final val UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER: Int   = 0x8a44
  final val UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER: Int = 0x8a46
  final val MAX_3D_TEXTURE_SIZE: Int                         = 0x8073
  final val MAX_DRAW_BUFFERS: Int                            = 0x8824
  final val MAX_COLOR_ATTACHMENTS: Int                       = 0x8cdf
  final val MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int              = 0x8b4c
  final val MAX_FRAGMENT_INPUT_COMPONENTS: Int               = 0x9125
  final val MAX_VARYING_VECTORS: Int                         = 0x8dfc
}

@nowarn
@js.native
@JSGlobal
class WebGLVertexArrayObject private[this] () extends js.Object

object ColorAttachments {
  final val COLOR_ATTACHMENT0: Int  = 0x8ce0
  final val COLOR_ATTACHMENT1: Int  = 0x8ce1
  final val COLOR_ATTACHMENT2: Int  = 0x8ce2
  final val COLOR_ATTACHMENT3: Int  = 0x8ce3
  final val COLOR_ATTACHMENT4: Int  = 0x8ce4
  final val COLOR_ATTACHMENT5: Int  = 0x8ce5
  final val COLOR_ATTACHMENT6: Int  = 0x8ce6
  final val COLOR_ATTACHMENT7: Int  = 0x8ce7
  final val COLOR_ATTACHMENT8: Int  = 0x8ce8
  final val COLOR_ATTACHMENT9: Int  = 0x8ce9
  final val COLOR_ATTACHMENT10: Int = 0x8cea
  final val COLOR_ATTACHMENT11: Int = 0x8ceb
  final val COLOR_ATTACHMENT12: Int = 0x8cec
  final val COLOR_ATTACHMENT13: Int = 0x8ced
  final val COLOR_ATTACHMENT14: Int = 0x8cee
  final val COLOR_ATTACHMENT15: Int = 0x8cef

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
