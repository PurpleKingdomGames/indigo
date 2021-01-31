// package indigo.platform.renderer.webgl2

// import indigo.shared.display.DisplayObject
// import scala.scalajs.js.typedarray.Float32Array
// import org.scalajs.dom.raw.WebGLProgram
// import indigo.facades.WebGL2RenderingContext
// import org.scalajs.dom.raw.WebGLRenderingContext._
// import org.scalajs.dom.raw.WebGLBuffer
// import indigo.platform.shaders.WebGL2StandardLights
// import org.scalajs.dom.raw.WebGLTexture
// import scala.scalajs.js.JSConverters._
// import indigo.shared.scenegraph.Light
// import indigo.shared.scenegraph.PointLight
// import indigo.shared.scenegraph.DirectionLight
// import indigo.shared.datatypes.Radians
// import indigo.shared.datatypes.RGBA
// import indigo.shared.scenegraph.SpotLight
// import indigo.shared.datatypes.Rectangle
// import indigo.platform.renderer.shared.RendererHelper
// import indigo.platform.renderer.shared.WebGLHelper
// import indigo.platform.renderer.shared.FrameBufferFunctions
// import indigo.platform.renderer.shared.FrameBufferComponents

// class RendererLights(gl2: WebGL2RenderingContext) {

//   private val lightsShaderProgram: WebGLProgram =
//     WebGLHelper.shaderProgramSetup(gl2, "Lights", WebGL2StandardLights)

//   private val displayObjectUBOBuffer: WebGLBuffer =
//     gl2.createBuffer()

//   // They're all blocks of 16, it's the only block length allowed in WebGL.
//   private val projectionMatrixUBODataSize: Int = 16
//   private val displayObjectUBODataSize: Int    = 16 * 2
//   private val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

//   val uboData: scalajs.js.Array[Float] =
//     List.fill(displayObjectUBODataSize)(0.0f).toJSArray

//   def updateStaticUBOData(displayObject: DisplayObject): Unit = {
//     uboData(0) = 0.0f                         //displayObject.x.toFloat
//     uboData(1) = 0.0f                         //displayObject.y.toFloat
//     uboData(2) = displayObject.width.toFloat  // * displayObject.scaleX
//     uboData(3) = displayObject.height.toFloat // * displayObject.scaleY

//     uboData(4) = displayObject.frameX
//     uboData(5) = displayObject.frameY
//     uboData(6) = displayObject.frameScaleX
//     uboData(7) = displayObject.frameScaleY
//   }

//   def updatePointLightUBOData(light: PointLight, magnification: Int): Unit = {
//     uboData(8) = 1.0f                                              // type: PointLight = 1.0d
//     uboData(9) = light.attenuation.toFloat * magnification.toFloat // attenuation
//     uboData(10) = light.position.x.toFloat                         // position x
//     uboData(11) = light.position.y.toFloat                         // position y

//     uboData(12) = light.color.r.toFloat // color r
//     uboData(13) = light.color.g.toFloat // color g
//     uboData(14) = light.color.b.toFloat // color b
//     uboData(15) = 0.0f                  // rotation

//     uboData(16) = 0.0f                          // angle
//     uboData(17) = light.height.toFloat / 100.0f // height
//     uboData(18) = 0.0f                          // near
//     uboData(19) = 0.0f                          // far

//     uboData(20) = light.power.toFloat // power
//     uboData(21) = 0.0f
//     uboData(22) = 0.0f
//     uboData(23) = 0.0f
//   }

//   def updateDirectionLightUBOData(light: DirectionLight): Unit = {
//     uboData(8) = 2.0f  // type: DirectionLight = 2.0d
//     uboData(9) = 0.0f  // attenuation
//     uboData(10) = 0.0f // position x
//     uboData(11) = 0.0f // position y

//     uboData(12) = light.color.r.toFloat                                    // color r
//     uboData(13) = light.color.g.toFloat                                    // color g
//     uboData(14) = light.color.b.toFloat                                    // color b
//     uboData(15) = Radians.TAU.value.toFloat - light.rotation.value.toFloat // rotation

//     uboData(16) = 0.0f                          // angle
//     uboData(17) = light.height.toFloat / 100.0f // height
//     uboData(18) = 0.0f                          // near
//     uboData(19) = 0.0f                          // far

//     uboData(20) = light.power.toFloat // power
//     uboData(21) = 0.0f
//     uboData(22) = 0.0f
//     uboData(23) = 0.0f
//   }

//   def updateSpotLightUBOData(light: SpotLight, magnification: Int): Unit = {
//     uboData(8) = 3.0f                                              // type: SpotLight = 3.0d
//     uboData(9) = light.attenuation.toFloat * magnification.toFloat // attenuation
//     uboData(10) = light.position.x.toFloat                         // position x
//     uboData(11) = light.position.y.toFloat                         // position y

//     uboData(12) = light.color.r.toFloat        // color r
//     uboData(13) = light.color.g.toFloat        // color g
//     uboData(14) = light.color.b.toFloat        // color b
//     uboData(15) = light.rotation.value.toFloat // rotation

//     uboData(16) = light.angle.value.toFloat     // angle
//     uboData(17) = light.height.toFloat / 100.0f // height
//     uboData(18) = light.near.toFloat            // near
//     uboData(19) = light.far.toFloat             // far

//     uboData(20) = light.power.toFloat // power
//     uboData(21) = 0.0f
//     uboData(22) = 0.0f
//     uboData(23) = 0.0f
//   }

//   @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
//   def drawLayer(
//       lights: List[Light],
//       projection: scalajs.js.Array[Float],
//       frameBufferComponents: FrameBufferComponents,
//       gameFrameBuffer: FrameBufferComponents.MultiOutput,
//       width: Int,
//       height: Int,
//       magnification: Int
//   ): Unit = {

//     FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, RGBA.Black.makeTransparent, true)
//     gl2.drawBuffers(frameBufferComponents.colorAttachments)

//     val lightsInRange =
//       RendererLights
//         .lightsInRange(lights, Rectangle(0, 0, width, height))
//         .filter(_.power > 0.05)
//     val lightCount: Int = lightsInRange.length;

//     if (lightCount > 0) {
//       gl2.useProgram(lightsShaderProgram)

//       setupLightsFragmentShaderState(gameFrameBuffer)

//       // UBO data
//       gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
//       gl2.bindBufferRange(
//         gl2.UNIFORM_BUFFER,
//         0,
//         displayObjectUBOBuffer,
//         0,
//         uboDataSize * Float32Array.BYTES_PER_ELEMENT
//       )

//       updateStaticUBOData(RendererHelper.screenDisplayObject(width, height))

//       var i: Int = 0

//       while (i < lightCount) {
//         val light = lightsInRange(i)

//         updateLightUboData(light, magnification)

//         gl2.bufferData(
//           ARRAY_BUFFER,
//           new Float32Array(projection ++ uboData),
//           STATIC_DRAW
//         )

//         gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

//         i = i + 1
//       }

//       gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);
//     }

//   }

//   def updateLightUboData(light: Light, magnification: Int): Unit =
//     light match {
//       case light: PointLight =>
//         updatePointLightUBOData(light, magnification)

//       case light: SpotLight =>
//         updateSpotLightUBOData(light, magnification)

//       case light: DirectionLight =>
//         updateDirectionLightUBOData(light)
//     }

//   @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
//   def setupLightsFragmentShaderState(game: FrameBufferComponents.MultiOutput): Unit = {

//     val uniformTextures: List[(String, WebGLTexture)] =
//       List(
//         "u_texture_game_normal"   -> game.normal,
//         "u_texture_game_specular" -> game.specular
//       )

//     var i: Int = 0

//     while (i < uniformTextures.length) {
//       val tex = uniformTextures(i)
//       WebGLHelper.attach(gl2, lightsShaderProgram, i + 1, tex._1, tex._2)
//       i = i + 1
//     }

//     // Reset to TEXTURE0 before the next round of rendering happens.
//     gl2.activeTexture(TEXTURE0)
//   }

// }

// object RendererLights {

//   def lightsInRange(ls: List[Light], viewBounds: Rectangle): List[Light] =
//     ls.filter(l => lightIsInRange(l, viewBounds))

//   def lightIsInRange(light: Light, viewBounds: Rectangle): Boolean =
//     light match {
//       case l: PointLight =>
//         viewBounds.expand(l.attenuation + 1).isPointWithin(l.position)

//       case l: SpotLight =>
//         viewBounds.expand(l.attenuation + 1).isPointWithin(l.position)

//       case _: DirectionLight =>
//         true
//     }

// }
