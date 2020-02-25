package indigo.facades

import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLProgram
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._

import scala.scalajs.js
import scala.scalajs.js.annotation._
// import scala.scalajs.js.typedarray._

@js.native
trait WebGL2RenderingContext extends WebGLRenderingContext {

  // A whole bunch of uniform constants..
  // https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Constants#Uniforms
  final val UNIFORM_BUFFER                              = 0x8A11
  final val UNIFORM_BUFFER_BINDING                      = 0x8A28
  final val UNIFORM_BUFFER_START                        = 0x8A29
  final val UNIFORM_BUFFER_SIZE                         = 0x8A2A
  final val MAX_VERTEX_UNIFORM_BLOCKS                   = 0x8A2B
  final val MAX_FRAGMENT_UNIFORM_BLOCKS                 = 0x8A2D
  final val MAX_COMBINED_UNIFORM_BLOCKS                 = 0x8A2E
  final val MAX_UNIFORM_BUFFER_BINDINGS                 = 0x8A2F
  final val MAX_UNIFORM_BLOCK_SIZE                      = 0x8A30
  final val MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS      = 0x8A31
  final val MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS    = 0x8A33
  final val UNIFORM_BUFFER_OFFSET_ALIGNMENT             = 0x8A34
  final val ACTIVE_UNIFORM_BLOCKS                       = 0x8A36
  final val UNIFORM_TYPE                                = 0x8A37
  final val UNIFORM_SIZE                                = 0x8A38
  final val UNIFORM_BLOCK_INDEX                         = 0x8A3A
  final val UNIFORM_OFFSET                              = 0x8A3B
  final val UNIFORM_ARRAY_STRIDE                        = 0x8A3C
  final val UNIFORM_MATRIX_STRIDE                       = 0x8A3D
  final val UNIFORM_IS_ROW_MAJOR                        = 0x8A3E
  final val UNIFORM_BLOCK_BINDING                       = 0x8A3F
  final val UNIFORM_BLOCK_DATA_SIZE                     = 0x8A40
  final val UNIFORM_BLOCK_ACTIVE_UNIFORMS               = 0x8A42
  final val UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES        = 0x8A43
  final val UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER   = 0x8A44
  final val UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = 0x8A46

  final val COLOR_ATTACHMENT1  = 0x8CE1
  final val COLOR_ATTACHMENT2  = 0x8CE2
  final val COLOR_ATTACHMENT3  = 0x8CE3
  final val COLOR_ATTACHMENT4  = 0x8CE4
  final val COLOR_ATTACHMENT5  = 0x8CE5
  final val COLOR_ATTACHMENT6  = 0x8CE6
  final val COLOR_ATTACHMENT7  = 0x8CE7
  final val COLOR_ATTACHMENT8  = 0x8CE8
  final val COLOR_ATTACHMENT9  = 0x8CE9
  final val COLOR_ATTACHMENT10 = 0x8CEA
  final val COLOR_ATTACHMENT11 = 0x8CEB
  final val COLOR_ATTACHMENT12 = 0x8CEC
  final val COLOR_ATTACHMENT13 = 0x8CED
  final val COLOR_ATTACHMENT14 = 0x8CEE
  final val COLOR_ATTACHMENT15 = 0x8CEF

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

@js.native
@JSGlobal
class WebGLVertexArrayObject private[this] () extends js.Object

object ColorAttachments {
  final val COLOR_ATTACHMENT0  = 0x8CE0
  final val COLOR_ATTACHMENT1  = 0x8CE1
  final val COLOR_ATTACHMENT2  = 0x8CE2
  final val COLOR_ATTACHMENT3  = 0x8CE3
  final val COLOR_ATTACHMENT4  = 0x8CE4
  final val COLOR_ATTACHMENT5  = 0x8CE5
  final val COLOR_ATTACHMENT6  = 0x8CE6
  final val COLOR_ATTACHMENT7  = 0x8CE7
  final val COLOR_ATTACHMENT8  = 0x8CE8
  final val COLOR_ATTACHMENT9  = 0x8CE9
  final val COLOR_ATTACHMENT10 = 0x8CEA
  final val COLOR_ATTACHMENT11 = 0x8CEB
  final val COLOR_ATTACHMENT12 = 0x8CEC
  final val COLOR_ATTACHMENT13 = 0x8CED
  final val COLOR_ATTACHMENT14 = 0x8CEE
  final val COLOR_ATTACHMENT15 = 0x8CEF

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
