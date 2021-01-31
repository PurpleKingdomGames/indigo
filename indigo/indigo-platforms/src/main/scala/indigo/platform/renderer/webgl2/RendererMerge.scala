// package indigo.platform.renderer.webgl2

// import indigo.shared.display.DisplayObject
// import scala.scalajs.js.typedarray.Float32Array
// import org.scalajs.dom.raw.WebGLProgram
// import indigo.facades.WebGL2RenderingContext
// import org.scalajs.dom.raw.WebGLRenderingContext._
// import org.scalajs.dom.raw.WebGLBuffer
// import indigo.platform.shaders.WebGL2StandardMerge
// import org.scalajs.dom.raw.WebGLTexture
// import scala.scalajs.js.JSConverters._
// import indigo.shared.datatypes.RGBA
// import indigo.platform.renderer.shared.RendererHelper
// import indigo.platform.renderer.shared.WebGLHelper
// import indigo.platform.renderer.shared.FrameBufferFunctions
// import indigo.platform.renderer.shared.FrameBufferComponents

// class RendererMerge(gl2: WebGL2RenderingContext) {

//   private val mergeShaderProgram: WebGLProgram =
//     WebGLHelper.shaderProgramSetup(gl2, "Merge", WebGL2StandardMerge)

//   private val displayObjectUBOBuffer: WebGLBuffer =
//     gl2.createBuffer()

//   // They're all blocks of 16, it's the only block length allowed in WebGL.
//   private val projectionMatrixUBODataSize: Int = 16
//   private val displayObjectUBODataSize: Int    = 16 * 2
//   private val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

//   private val uboData: scalajs.js.Array[Float] =
//     List.fill(displayObjectUBODataSize)(0.0f).toJSArray

//   def updateUBOData(
//       displayObject: DisplayObject,
//       gameOverlay: RGBA,
//       uiOverlay: RGBA,
//       gameLayerTint: RGBA,
//       lightingLayerTint: RGBA,
//       uiLayerTint: RGBA,
//       gameLayerSaturation: Double,
//       lightingLayerSaturation: Double,
//       uiLayerSaturation: Double
//   ): Unit = {
//     uboData(0) = 0.0f
//     uboData(1) = 0.0f
//     uboData(2) = displayObject.width.toFloat
//     uboData(3) = displayObject.height.toFloat

//     uboData(4) = displayObject.frameX.toFloat
//     uboData(5) = displayObject.frameY.toFloat
//     uboData(6) = displayObject.frameScaleX.toFloat
//     uboData(7) = displayObject.frameScaleY.toFloat

//     uboData(8) = gameOverlay.r.toFloat
//     uboData(9) = gameOverlay.g.toFloat
//     uboData(10) = gameOverlay.b.toFloat
//     uboData(11) = gameOverlay.a.toFloat

//     uboData(12) = uiOverlay.r.toFloat
//     uboData(13) = uiOverlay.g.toFloat
//     uboData(14) = uiOverlay.b.toFloat
//     uboData(15) = uiOverlay.a.toFloat

//     uboData(16) = gameLayerTint.r.toFloat
//     uboData(17) = gameLayerTint.g.toFloat
//     uboData(18) = gameLayerTint.b.toFloat
//     uboData(19) = gameLayerTint.a.toFloat

//     uboData(20) = lightingLayerTint.r.toFloat
//     uboData(21) = lightingLayerTint.g.toFloat
//     uboData(22) = lightingLayerTint.b.toFloat
//     uboData(23) = lightingLayerTint.a.toFloat

//     uboData(24) = uiLayerTint.r.toFloat
//     uboData(25) = uiLayerTint.g.toFloat
//     uboData(26) = uiLayerTint.b.toFloat
//     uboData(27) = uiLayerTint.a.toFloat

//     uboData(28) = gameLayerSaturation.toFloat
//     uboData(29) = lightingLayerSaturation.toFloat
//     uboData(30) = uiLayerSaturation.toFloat
//     // uboData(31) = 0d
//   }

//   @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
//   def drawLayer(
//       projection: scalajs.js.Array[Float],
//       gameFrameBuffer: FrameBufferComponents.MultiOutput,
//       lightsFrameBuffer: FrameBufferComponents.SingleOutput,
//       lightingFrameBuffer: FrameBufferComponents.SingleOutput,
//       distortionFrameBuffer: FrameBufferComponents.SingleOutput,
//       uiFrameBuffer: FrameBufferComponents.SingleOutput,
//       width: Int,
//       height: Int,
//       clearColor: RGBA,
//       gameOverlay: RGBA,
//       uiOverlay: RGBA,
//       gameLayerTint: RGBA,
//       lightingLayerTint: RGBA,
//       uiLayerTint: RGBA,
//       gameLayerSaturation: Double,
//       lightingLayerSaturation: Double,
//       uiLayerSaturation: Double
//   ): Unit = {

//     FrameBufferFunctions.switchToCanvas(gl2, clearColor)

//     gl2.useProgram(mergeShaderProgram)

//     updateUBOData(
//       RendererHelper.screenDisplayObject(width, height),
//       gameOverlay,
//       uiOverlay,
//       gameLayerTint,
//       lightingLayerTint,
//       uiLayerTint,
//       gameLayerSaturation,
//       lightingLayerSaturation,
//       uiLayerSaturation
//     )

//     // UBO data
//     gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
//     gl2.bindBufferRange(
//       gl2.UNIFORM_BUFFER,
//       0,
//       displayObjectUBOBuffer,
//       0,
//       uboDataSize * Float32Array.BYTES_PER_ELEMENT
//     )
//     gl2.bufferData(
//       ARRAY_BUFFER,
//       new Float32Array(projection ++ uboData),
//       STATIC_DRAW
//     )

//     setupMergeFragmentShaderState(
//       gameFrameBuffer,
//       lightsFrameBuffer,
//       lightingFrameBuffer,
//       distortionFrameBuffer,
//       uiFrameBuffer
//     )

//     gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

//     gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);

//   }

//   @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
//   def setupMergeFragmentShaderState(
//       game: FrameBufferComponents.MultiOutput,
//       textureLights: FrameBufferComponents.SingleOutput,
//       textureLighting: FrameBufferComponents.SingleOutput,
//       textureDistortion: FrameBufferComponents.SingleOutput,
//       textureUi: FrameBufferComponents.SingleOutput
//   ): Unit = {

//     val uniformTextures: List[(String, WebGLTexture)] =
//       List(
//         "u_texture_game_albedo"   -> game.albedo,
//         "u_texture_game_emissive" -> game.emissive,
//         "u_texture_lights"        -> textureLights.diffuse,
//         "u_texture_lighting"      -> textureLighting.diffuse,
//         "u_texture_distortion"    -> textureDistortion.diffuse,
//         "u_texture_ui"            -> textureUi.diffuse
//       )

//     var i: Int = 0

//     while (i < uniformTextures.length) {
//       val tex = uniformTextures(i)
//       WebGLHelper.attach(gl2, mergeShaderProgram, i + 1, tex._1, tex._2)
//       i = i + 1
//     }

//     // Reset to TEXTURE0 before the next round of rendering happens.
//     gl2.activeTexture(TEXTURE0)
//   }

// }
