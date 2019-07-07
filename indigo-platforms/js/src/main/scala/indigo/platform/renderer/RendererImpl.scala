package indigo.platform.renderer

import indigo.shared.datatypes.AmbientLight
import indigo.shared.metrics._
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.shared.display.{DisplayObject, Displayable}
import indigo.shared.EqualTo._
import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.raw.WebGLProgram
import scala.scalajs.js.typedarray.Float32Array
import indigo.facades.WebGL2RenderingContext
import indigo.platform.shaders._
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.GameTime
import indigo.shared.platform.AssetMapping
import indigo.platform.DisplayObjectConversions
import indigo.shared.AnimationsRegister

final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends Renderer {

  private val gl: WebGLRenderingContext =
    cNc.context

  private val gl2: WebGL2RenderingContext =
    gl.asInstanceOf[WebGL2RenderingContext]

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, RendererFunctions.organiseImage(gl, li.data))
    }

  private val vertexBuffer: WebGLBuffer           = gl.createBuffer()
  private val textureBuffer: WebGLBuffer          = gl.createBuffer()
  private val displayObjectUBOBuffer: WebGLBuffer = gl.createBuffer()

  private val standardShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Pixel", StandardPixelArtVert.shader, StandardPixelArtFrag.shader)

  private val lightingShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Lighting", StandardLightingPixelArtVert.shader, StandardLightingPixelArtFrag.shader)

  private val mergeShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Merge", StandardMergeVert.shader, StandardMergeFrag.shader)

  private val gameFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)

    // Vertex
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.vertices), STATIC_DRAW)

    List(standardShaderProgram, lightingShaderProgram, mergeShaderProgram).foreach { shaderProgram =>
      val verticesLocation = gl.getAttribLocation(shaderProgram, "a_vertices")
      RendererFunctions.bindAttibuteBuffer(gl, verticesLocation, 3)
    }

    // Bind texture coords
    gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.textureCoordinates), STATIC_DRAW)

    List(standardShaderProgram, lightingShaderProgram, mergeShaderProgram).foreach { shaderProgram =>
      val texcoordLocation = gl.getAttribLocation(shaderProgram, "a_texcoord")
      RendererFunctions.bindAttibuteBuffer(gl, texcoordLocation, 2)
    }
  }

  def setNormalBlend(): Unit =
    gl.blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA)

  def setLightingBlend(): Unit =
    gl.blendFunc(SRC_ALPHA, DST_ALPHA)

  def drawScene(gameTime: GameTime, scene: SceneUpdateFragment, assetMapping: AssetMapping, metrics: Metrics): Unit = {

    val displayable: Displayable =
      viewToDisplayable(gameTime, scene, assetMapping, metrics)

    RendererFunctions.resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight, cNc.magnification)

    metrics.record(DrawGameLayerStartMetric)
    setNormalBlend()
    drawLayer(displayable.game, Some(gameFrameBuffer), config.clearColor, standardShaderProgram, CurrentDrawLayer.Game, metrics)
    metrics.record(DrawGameLayerEndMetric)

    metrics.record(DrawLightingLayerStartMetric)
    setLightingBlend()
    drawLayer(
      displayable.lighting,
      Some(lightingFrameBuffer),
      AmbientLight.toClearColor(displayable.ambientLight),
      lightingShaderProgram,
      CurrentDrawLayer.Lighting,
      metrics
    )
    metrics.record(DrawLightingLayerEndMetric)

    metrics.record(DrawUiLayerStartMetric)
    setNormalBlend()
    drawLayer(displayable.ui, Some(uiFrameBuffer), ClearColor.Black.forceTransparent, standardShaderProgram, CurrentDrawLayer.UI, metrics)
    metrics.record(DrawUiLayerEndMetric)

    metrics.record(RenderToWindowStartMetric)
    setNormalBlend()
    drawLayer(List(RendererFunctions.screenDisplayObject(cNc.width, cNc.height)), None, config.clearColor, mergeShaderProgram, CurrentDrawLayer.Merge, metrics)
    metrics.record(RenderToWindowEndMetric)

    // Finally, persist animation states...
    persistAnimationStates(metrics)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  def drawLayer(
      displayObjects: List[DisplayObject],
      frameBufferComponents: Option[FrameBufferComponents],
      clearColor: ClearColor,
      shaderProgram: WebGLProgram,
      layer: CurrentDrawLayer,
      metrics: Metrics
  ): Unit = {

    frameBufferComponents match {
      case Some(fb) =>
        FrameBufferFunctions.switchToFramebuffer(gl, fb.frameBuffer, clearColor)

      case None =>
        FrameBufferFunctions.switchToCanvas(gl, config.clearColor)
    }

    gl.useProgram(shaderProgram)

    // Projection
    val projectionMatrix: scalajs.js.Array[Double] =
      if (layer.isMerge) RendererFunctions.orthographicProjectionMatrixNoMag
      else RendererFunctions.orthographicProjectionMatrix

    // Texture attribute and uniform
    val textureLocation = gl.getUniformLocation(shaderProgram, "u_texture")
    gl.uniform1i(textureLocation, 0)

    // Bind UBO buffer
    gl.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(
      gl2.UNIFORM_BUFFER,
      0,
      displayObjectUBOBuffer,
      0,
      RendererFunctions.uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )

    var lastTextureName: String = ""

    RendererFunctions.sortByDepth(displayObjects).foreach { displayObject =>
      metrics.record(layer.metricStart)

      // Set all the uniforms
      RendererFunctions.updateUBOData(displayObject)
      gl.bufferData(
        ARRAY_BUFFER,
        new Float32Array(projectionMatrix ++ RendererFunctions.uboData),
        STATIC_DRAW
      )

      // If needed, update texture state
      layer match {
        case CurrentDrawLayer.Merge =>
          RendererFunctions.setupMergeFragmentShaderState(
            gl,
            mergeShaderProgram,
            gameFrameBuffer.texture,
            lightingFrameBuffer.texture,
            uiFrameBuffer.texture
          )

          gl.drawArrays(TRIANGLE_STRIP, 0, 4)

        case _ if displayObject.imageRef !== lastTextureName =>
          textureLocations.find(t => t.name === displayObject.imageRef).foreach { textureLookup =>
            gl.bindTexture(TEXTURE_2D, textureLookup.texture)
            lastTextureName = displayObject.imageRef
          }

          gl.drawArrays(TRIANGLE_STRIP, 0, 4)

        case _ =>
          gl.drawArrays(TRIANGLE_STRIP, 0, 4)
      }

      metrics.record(layer.metricDraw)

      metrics.record(layer.metricEnd)
    }

  }

  def viewToDisplayable(gameTime: GameTime, scene: SceneUpdateFragment, assetMapping: AssetMapping, metrics: Metrics): Displayable = {
    metrics.record(ToDisplayableStartMetric)

    val displayable: Displayable =
      Displayable(
        DisplayObjectConversions.sceneNodesToDisplayObjects(scene.gameLayer, gameTime, assetMapping, metrics),
        DisplayObjectConversions.sceneNodesToDisplayObjects(scene.lightingLayer, gameTime, assetMapping, metrics),
        DisplayObjectConversions.sceneNodesToDisplayObjects(scene.uiLayer, gameTime, assetMapping, metrics),
        scene.ambientLight
      )

    metrics.record(ToDisplayableEndMetric)

    displayable
  }

  def persistAnimationStates(metrics: Metrics): Unit = {
    metrics.record(PersistAnimationStatesStartMetric)
    AnimationsRegister.persistAnimationStates()
    metrics.record(PersistAnimationStatesEndMetric)
  }

}
