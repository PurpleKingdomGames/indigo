package indigo.platform.renderer

import indigo.shared.display.DisplayObject
import scala.scalajs.js.typedarray.Float32Array
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.metrics.Metrics
import indigo.facades.WebGL2RenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import indigo.platform.shaders.StandardLights
import org.scalajs.dom.raw.WebGLTexture
import indigo.shared.ClearColor
import scala.scalajs.js.JSConverters._
import indigo.shared.scenegraph.Light
import indigo.shared.scenegraph.PointLight
import indigo.shared.scenegraph.DirectionLight
import indigo.shared.datatypes.Radians
import indigo.shared.scenegraph.SpotLight

class RendererLights(gl2: WebGL2RenderingContext) {

  private val lightsShaderProgram: WebGLProgram =
    RendererFunctions.shaderProgramSetup(gl2, "Lights", StandardLights)

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val projectionMatrixUBODataSize: Int = 16
  private val displayObjectUBODataSize: Int    = 16 * 2
  private val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

  val uboData: scalajs.js.Array[Double] =
    List.fill(displayObjectUBODataSize)(0.0d).toJSArray

  def updateStaticUBOData(displayObject: DisplayObject): Unit = {
    uboData(0) = displayObject.x.toDouble
    uboData(1) = displayObject.y.toDouble
    uboData(2) = displayObject.width.toDouble * displayObject.scaleX
    uboData(3) = displayObject.height.toDouble * displayObject.scaleY

    uboData(4) = displayObject.frameX
    uboData(5) = displayObject.frameY
    uboData(6) = displayObject.frameScaleX
    uboData(7) = displayObject.frameScaleY
  }

  def updatePointLightUBOData(light: PointLight, magnification: Int): Unit = {
    uboData(8) = 1.0d                                                // type: PointLight = 1.0d
    uboData(9) = light.attenuation.toDouble * magnification.toDouble // attenuation
    uboData(10) = light.position.x.toDouble                          // position x
    uboData(11) = light.position.y.toDouble                          // position y

    uboData(12) = light.color.r // color r
    uboData(13) = light.color.g // color g
    uboData(14) = light.color.b // color b
    uboData(15) = 0.0d          // rotation

    uboData(16) = 0.0d                           // angle
    uboData(17) = light.height.toDouble / 100.0d // height
    uboData(18) = 0.0d                           // near
    uboData(19) = 0.0d                           // far

    uboData(20) = light.power // power
    uboData(21) = 0.0d
    uboData(22) = 0.0d
    uboData(23) = 0.0d
  }

  def updateDirectionLightUBOData(light: DirectionLight): Unit = {
    uboData(8) = 2.0d  // type: DirectionLight = 2.0d
    uboData(9) = 0.0d  // attenuation
    uboData(10) = 0.0d // position x
    uboData(11) = 0.0d // position y

    uboData(12) = light.color.r                            // color r
    uboData(13) = light.color.g                            // color g
    uboData(14) = light.color.b                            // color b
    uboData(15) = Radians.TAU.value - light.rotation.value // rotation

    uboData(16) = 0.0d                           // angle
    uboData(17) = light.height.toDouble / 100.0d // height
    uboData(18) = 0.0d                           // near
    uboData(19) = 0.0d                           // far

    uboData(20) = light.power // power
    uboData(21) = 0.0d
    uboData(22) = 0.0d
    uboData(23) = 0.0d
  }

  def updateSpotLightUBOData(light: SpotLight, magnification: Int): Unit = {
    uboData(8) = 3.0d                                                // type: SpotLight = 3.0d
    uboData(9) = light.attenuation.toDouble * magnification.toDouble // attenuation
    uboData(10) = light.position.x.toDouble                          // position x
    uboData(11) = light.position.y.toDouble                          // position y

    uboData(12) = light.color.r        // color r
    uboData(13) = light.color.g        // color g
    uboData(14) = light.color.b        // color b
    uboData(15) = light.rotation.value // rotation

    uboData(16) = light.angle.value              // angle
    uboData(17) = light.height.toDouble / 100.0d // height
    uboData(18) = light.near.toDouble            // near
    uboData(19) = light.far.toDouble             // far

    uboData(20) = light.power // power
    uboData(21) = 0.0d
    uboData(22) = 0.0d
    uboData(23) = 0.0d
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.While", "org.wartremover.warts.Null"))
  def drawLayer(
      lights: List[Light],
      projection: scalajs.js.Array[Double],
      frameBufferComponents: FrameBufferComponents,
      gameFrameBuffer: FrameBufferComponents.MultiOutput,
      width: Int,
      height: Int,
      magnification: Int,
      metrics: Metrics
  ): Unit = {

    metrics.record(CurrentDrawLayer.Lights.metricStart)

    FrameBufferFunctions.switchToFramebuffer(gl2, frameBufferComponents.frameBuffer, ClearColor.Black.forceTransparent)
    gl2.drawBuffers(frameBufferComponents.colorAttachments)

    gl2.useProgram(lightsShaderProgram)

    setupLightsFragmentShaderState(gameFrameBuffer)

    // UBO data
    gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(
      gl2.UNIFORM_BUFFER,
      0,
      displayObjectUBOBuffer,
      0,
      uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )

    updateStaticUBOData(RendererHelper.screenDisplayObject(width, height))

    var i: Int = 0

    while (i < lights.length) {
      val light = lights(i)

      updateLightUboData(light, magnification)

      gl2.bufferData(
        ARRAY_BUFFER,
        new Float32Array(projection ++ uboData),
        STATIC_DRAW
      )

      gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

      i = i + 1
    }

    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);

    metrics.record(CurrentDrawLayer.Lights.metricDraw)

    metrics.record(CurrentDrawLayer.Lights.metricEnd)

  }

  def updateLightUboData(light: Light, magnification: Int): Unit =
    light match {
      case light: PointLight =>
        updatePointLightUBOData(light, magnification)

      case light: SpotLight =>
        updateSpotLightUBOData(light, magnification)

      case light: DirectionLight =>
        updateDirectionLightUBOData(light)
    }

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var"))
  def setupLightsFragmentShaderState(game: FrameBufferComponents.MultiOutput): Unit = {

    val uniformTextures: List[(String, WebGLTexture)] =
      List(
        "u_texture_game_albedo"   -> game.albedo,
        "u_texture_game_normal"   -> game.normal,
        "u_texture_game_specular" -> game.specular
      )

    var i: Int = 0

    while (i < uniformTextures.length) {
      val tex = uniformTextures(i)
      RendererHelper.attach(gl2, lightsShaderProgram, i + 1, tex._1, tex._2)
      i = i + 1
    }

    // Reset to TEXTURE0 before the next round of rendering happens.
    gl2.activeTexture(TEXTURE0)
  }

}
