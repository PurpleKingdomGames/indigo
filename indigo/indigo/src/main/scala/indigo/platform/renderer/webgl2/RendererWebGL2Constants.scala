package indigo.platform.renderer.webgl2

object RendererWebGL2Constants {

  // The minimum value of gl.MAX_UNIFORM_BUFFER_BINDINGS we test
  // for during WebGL 2.0 detection is 24. However, 24 should be
  // treated as the MAX number. So these pointers must not exceed
  // that. In practice this means we're allowing 16 offsets / UBOS
  // for blend and custom shaders each, which should be plenty.
  // The first 8 are reserved for internal use.
  val mergeObjectBlockPointer: Int        = 0
  val projectionBlockPointer: Int         = 1
  val frameDataBlockPointer: Int          = 2
  val cloneReferenceDataBlockPointer: Int = 3
  val lightDataBlockPointer: Int          = 4
  val customDataBlockOffsetPointer: Int   = 8

}
