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
  private val instanceDataBuffer: WebGLBuffer     = gl.createBuffer()

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
    RendererFunctions.bindAttibuteBuffer(gl, RendererFunctions.VertexAtrributeLocation, 3)

    // Bind texture coords
    gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(RendererFunctions.textureCoordinates), STATIC_DRAW)
    RendererFunctions.bindAttibuteBuffer(gl, RendererFunctions.TextureAtrributeLocation, 2)

    List(standardShaderProgram, lightingShaderProgram, mergeShaderProgram).foreach { shaderProgram =>
      gl.useProgram(shaderProgram)
      val textureLocation = gl.getUniformLocation(shaderProgram, "u_texture")
      gl.uniform1i(textureLocation, 0)
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

  private val initialBufferData: Float32Array =
    new Float32Array(RendererFunctions.uboDataSize)

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

    // Bind UBO buffer
    gl.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(
      gl2.UNIFORM_BUFFER,
      0,
      displayObjectUBOBuffer,
      0,
      RendererFunctions.uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl.bufferData(ARRAY_BUFFER, initialBufferData, STATIC_DRAW)
    gl.bufferSubData(ARRAY_BUFFER, 0, new Float32Array(projectionMatrix))

    var lastTextureName: String = ""

    RendererFunctions.sortByDepth(displayObjects).foreach { displayObject =>
      metrics.record(layer.metricStart)

      // Set all the uniforms
      RendererFunctions.updateUBOData(displayObject)
      gl.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
      gl.bufferSubData(
        ARRAY_BUFFER,
        RendererFunctions.projectionMatrixUBODataSize * Float32Array.BYTES_PER_ELEMENT,
        new Float32Array(RendererFunctions.uboData)
      )

      // test
      gl.bindBuffer(ARRAY_BUFFER, instanceDataBuffer)
      gl.bufferData(ARRAY_BUFFER, new Float32Array(scalajs.js.Array[Double](20.0, 0.0)), STATIC_DRAW)
      RendererFunctions.bindAttibuteBuffer(gl, RendererFunctions.InstanceAtrributeLocation, 2)
      gl2.vertexAttribDivisor(RendererFunctions.InstanceAtrributeLocation, 1)

/*
// position attribute
glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)0);
glEnableVertexAttribArray(0);
// color attribute
glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(float), (void*)(3* sizeof(float)));
glEnableVertexAttribArray(1);

    gl.vertexAttribPointer(
      indx = attributeLocation,
      size = size,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(attributeLocation)

    while nothing needs to change (i.e. images or < batch size), keep piling data into our array.
    On state change or batch size met:
    - bind the buffer
    - bind all the data to the buffer
    - set the attribute pointers (done?)
    - enable the arrays
    - set the divisor
    - draw the instance count.

*/

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

          gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, 1)

        case _ =>
          gl2.drawArraysInstanced(TRIANGLE_STRIP, 0, 4, 1)
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
