package indigo.platform.renderer.webgl1

import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.shared.display.DisplayObject
import indigo.shared.EqualTo._
import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.datatypes.Matrix4
import scala.scalajs.js.typedarray.Float32Array
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.shared.platform.ProcessedSceneData
import scala.collection.mutable
import indigo.shared.display.DisplayEntity

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
final class RendererWebGL1(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends Renderer {

  def screenWidth: Int                      = 0 //TODO
  def screenHeight: Int                     = 0 //TODO
  def orthographicProjectionMatrix: Matrix4 = RendererFunctions.orthographicProjectionMatrix

  import RendererFunctions._
  import indigo.platform.shaders._

  private val gl: WebGLRenderingContext =
    cNc.context

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, organiseImage(gl, li.data))
    }

  val vertices: scalajs.js.Array[Float] = {
    val xd: Float = -0.5f
    val yd: Float = -0.5f
    val zd: Float = 1.0f
    val wd: Float = 1.0f
    val hd: Float = 1.0f

    scalajs.js.Array[Float](
      xd,
      yd,
      zd,
      xd,
      hd + yd,
      zd,
      wd + xd,
      yd,
      zd,
      xd,
      hd + yd,
      zd,
      wd + xd,
      yd,
      zd,
      wd + xd,
      hd + yd,
      zd
    )
  }

  private val vertexCount: Int = vertices.length / 3

  private val vertexBuffer: WebGLBuffer  = gl.createBuffer()
  private val textureBuffer: WebGLBuffer = gl.createBuffer()

  private val standardShaderProgram = shaderProgramSetup(gl, "Pixel", WebGL1StandardPixelArt.vertex, WebGL1StandardPixelArt.fragment)
  private val lightingShaderProgram = shaderProgramSetup(gl, "Lighting", WebGL1StandardLightingPixelArt.vertex, WebGL1StandardLightingPixelArt.fragment)
  private val mergeShaderProgram    = shaderProgramSetup(gl, "Merge", WebGL1StandardMerge.vertex, WebGL1StandardMerge.fragment)

  private val gameFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, cNc.width.toDouble, cNc.height.toDouble)
    gl.enable(BLEND)
    gl.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)

    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)
  }

  def drawScene(sceneData: ProcessedSceneData): Unit = {

    resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight, cNc.magnification)

    drawLayer(
      sceneData.gameLayerDisplayObjects,
      Some(gameFrameBuffer),
      config.clearColor,
      standardShaderProgram,
      sceneData.gameProjection,
      false
    )

    drawLayer(
      sceneData.lightingLayerDisplayObjects,
      Some(lightingFrameBuffer),
      sceneData.clearColor,
      lightingShaderProgram,
      sceneData.lightingProjection,
      false
    )

    drawLayer(
      sceneData.uiLayerDisplayObjects,
      Some(uiFrameBuffer),
      ClearColor.Black.forceTransparent,
      standardShaderProgram,
      sceneData.uiProjection,
      false
    )

    drawLayer(
      mutable.ListBuffer(screenDisplayObject(cNc.width, cNc.height)),
      None,
      config.clearColor,
      mergeShaderProgram,
      RendererFunctions.orthographicProjectionMatrixNoMag,
      true
    )
  }

  def drawLayer(
      displayEntities: mutable.ListBuffer[DisplayEntity],
      frameBufferComponents: Option[FrameBufferComponents],
      clearColor: ClearColor,
      shaderProgram: WebGLProgram,
      projectionMatrix: Matrix4,
      isMerge: Boolean
  ): Unit = {

    frameBufferComponents match {
      case Some(fb) =>
        FrameBufferFunctions.switchToFramebuffer(gl, fb.frameBuffer, clearColor)

      case None =>
        FrameBufferFunctions.switchToCanvas(gl, config.clearColor)
    }

    gl.useProgram(shaderProgram)

    gl.uniformMatrix4fv(
      location = gl.getUniformLocation(shaderProgram, "u_projection"),
      transpose = false,
      value = mat4ToJsArray(projectionMatrix)
    )

    // Attribute locations
    val verticesLocation = gl.getAttribLocation(shaderProgram, "a_vertices")
    val texcoordLocation = gl.getAttribLocation(shaderProgram, "a_texcoord")

    // Uniform locations (vertex)
    val translationLocation = gl.getUniformLocation(shaderProgram, "u_translation")
    val rotationLocation    = gl.getUniformLocation(shaderProgram, "u_rotation")
    val scaleLocation       = gl.getUniformLocation(shaderProgram, "u_scale")

    // Uniform locations (fragment)
    val tintLocation    = gl.getUniformLocation(shaderProgram, "u_tint")
    val textureLocation = gl.getUniformLocation(shaderProgram, "u_texture")

    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    bindAttibuteBuffer(gl, verticesLocation, 3)

    // Set once
    gl.uniform1i(textureLocation, 0)

    sortByDepth(displayEntities).foreach {
      case displayObject: DisplayObject =>
        gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
        gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.textureCoordinates(displayObject)), STATIC_DRAW)
        bindAttibuteBuffer(gl, texcoordLocation, 2)

        setupVertexShaderState(gl, displayObject, translationLocation, rotationLocation, scaleLocation)

        if (isMerge)
          setupMergeFragmentShaderState(gl, mergeShaderProgram, gameFrameBuffer.texture, lightingFrameBuffer.texture, uiFrameBuffer.texture)
        else
          textureLocations.find(t => t.name === displayObject.atlasName).foreach { textureLookup =>
            setupFragmentShaderState(gl, textureLookup.texture, displayObject, tintLocation)
          }

        gl.drawArrays(TRIANGLES, 0, vertexCount)

      case _ =>
        ()
    }

  }

}
