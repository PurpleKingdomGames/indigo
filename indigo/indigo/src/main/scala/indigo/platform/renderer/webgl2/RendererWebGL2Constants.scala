package indigo.platform.renderer.webgl2

object RendererWebGL2Constants {

  // The minimum value of gl.MAX_UNIFORM_BUFFER_BINDINGS we test
  // for during WebGL 2.0 detection is 24. However, 24 should be
  // treated as the MAX number. So these pointers must not exceed
  // that. In practice this means we're allowing 8 offsets / UBOS
  // for blend and custom shaders each. The first 8 are reserved
  // for internal use.
  val mergeObjectBlockPointer: Int      = 0
  val projectionBlockPointer: Int       = 1
  val frameDataBlockPointer: Int        = 2
  val lightDataBlockPointer: Int        = 3
  val blendDataBlockOffsetPointer: Int  = 8
  val customDataBlockOffsetPointer: Int = 16

}
