package indigo.platform.renderer

import indigo.shared.metrics._
import indigo.shared.ClearColor
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._
import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import org.scalajs.dom.raw.WebGLRenderingContext
import scala.scalajs.js.typedarray.Float32Array
import indigo.facades.WebGL2RenderingContext
import indigo.platform.shaders._
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.GameTime
import indigo.shared.platform.AssetMapping
import indigo.platform.DisplayObjectConversions
import indigo.shared.AnimationsRegister
import indigo.shared.display.DisplayObject
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Sprite
import indigo.shared.datatypes.Matrix4
import org.scalajs.dom.html
import indigo.shared.EqualTo._
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister

@SuppressWarnings(Array("org.wartremover.warts.Null"))
final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas, boundaryLocator: BoundaryLocator, animationsRegister: AnimationsRegister, fontRegister: FontRegister) extends Renderer {

  private val gl: WebGLRenderingContext =
    cNc.context

  private val gl2: WebGL2RenderingContext =
    gl.asInstanceOf[WebGL2RenderingContext]

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      new TextureLookupResult(li.name, RendererFunctions.organiseImage(gl, li.data))
    }

  private val mergeRenderer: RendererMerge =
    new RendererMerge(gl2)

  private val lightsRenderer: RendererLights =
    new RendererLights(gl2)

  private val layerRenderer: RendererLayer =
    new RendererLayer(gl2, textureLocations, config.maxBatchSize)

  private val vertexAndTextureCoordsBuffer: WebGLBuffer = gl.createBuffer()

  private val vao = gl2.createVertexArray()

  private val standardShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Pixel", StandardPixelArt)
  private val lightingShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Lighting", StandardLightingPixelArt)
  private val distortionShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Lighting", StandardDistortionPixelArt)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameFrameBuffer: FrameBufferComponents.MultiOutput =
    FrameBufferFunctions.createFrameBufferMulti(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lightsFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lightingFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var distortionFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var uiFrameBuffer: FrameBufferComponents.SingleOutput =
    FrameBufferFunctions.createFrameBufferSingle(gl, cNc.canvas.width, cNc.canvas.height)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lastWidth: Int = 0
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var lastHeight: Int = 0
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrix: scalajs.js.Array[Double] = RendererFunctions.mat4ToJsArray(Matrix4.identity)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var orthographicProjectionMatrixNoMag: scalajs.js.Array[Float] = RendererFunctions.mat4ToJsArray(Matrix4.identity).map(_.toFloat)

  def init(): Unit = {

    val verticesAndTextureCoords: scalajs.js.Array[Float] = {
      val vert0 = scalajs.js.Array[Float](-0.5f, -0.5f, 0.0f, 1.0f)
      val vert1 = scalajs.js.Array[Float](-0.5f, 0.5f, 0.0f, 0.0f)
      val vert2 = scalajs.js.Array[Float](0.5f, -0.5f, 1.0f, 1.0f)
      val vert3 = scalajs.js.Array[Float](0.5f, 0.5f, 1.0f, 0.0f)

      vert0 ++ vert1 ++ vert2 ++ vert3
    }

    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)

    gl2.bindVertexArray(vao)

    // Vertex
    gl.bindBuffer(ARRAY_BUFFER, vertexAndTextureCoordsBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(verticesAndTextureCoords), STATIC_DRAW)
    gl.enableVertexAttribArray(0)
    gl.vertexAttribPointer(
      indx = 0,
      size = 4,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    gl2.bindVertexArray(null)
  }

  def calculateProjectionMatrix(width: Double, height: Double, magnification: Int): scalajs.js.Array[Double] =
    RendererFunctions.mat4ToJsArray(Matrix4.orthographic(width.toDouble / magnification.toDouble, height.toDouble / magnification.toDouble))

  def drawScene(gameTime: GameTime, scene: SceneUpdateFragment, assetMapping: AssetMapping, metrics: Metrics): Unit = {

    gl2.bindVertexArray(vao)

    val cloneBlankDisplayObjects: Map[String, DisplayObject] =
      scene.cloneBlanks.foldLeft(Map.empty[String, DisplayObject]) { (acc, blank) =>
        blank.cloneable match {
          case g: Graphic =>
            acc + (blank.id.value -> DisplayObjectConversions.graphicToDisplayObject(g, assetMapping))

          case s: Sprite =>
            animationsRegister.fetchAnimationForSprite(gameTime, s.bindingKey, s.animationKey, s.animationActions, metrics) match {
              case None =>
                acc

              case Some(anim) =>
                acc + (blank.id.value -> DisplayObjectConversions.spriteToDisplayObject(boundaryLocator, s, assetMapping, anim))
            }
        }
      }

    resize(cNc.canvas, cNc.magnification)

    val gameProjection =
      scene.gameLayer.magnification
        .map { m =>
          calculateProjectionMatrix(cNc.canvas.width.toDouble, cNc.canvas.height.toDouble, m)
        }
        .getOrElse(orthographicProjectionMatrix)

    val lightingProjection =
      scene.lightingLayer.magnification
        .map { m =>
          calculateProjectionMatrix(cNc.canvas.width.toDouble, cNc.canvas.height.toDouble, m)
        }
        .getOrElse(orthographicProjectionMatrix)

    val uiProjection =
      scene.uiLayer.magnification
        .map { m =>
          calculateProjectionMatrix(cNc.canvas.width.toDouble, cNc.canvas.height.toDouble, m)
        }
        .getOrElse(orthographicProjectionMatrix)

    // Game layer
    metrics.record(DrawGameLayerStartMetric)
    RendererFunctions.setNormalBlend(gl)
    layerRenderer.drawLayer(
      gameProjection,
      cloneBlankDisplayObjects,
      DisplayObjectConversions.sceneNodesToDisplayObjects(scene.gameLayer.nodes, gameTime, assetMapping, boundaryLocator, animationsRegister, fontRegister, metrics),
      gameFrameBuffer,
      ClearColor.Black.forceTransparent,
      standardShaderProgram,
      CurrentDrawLayer.Game,
      metrics
    )
    metrics.record(DrawGameLayerEndMetric)

    // Dynamic lighting
    metrics.record(DrawLightsLayerStartMetric)
    RendererFunctions.setLightsBlend(gl)
    lightsRenderer.drawLayer(
      scene.lights,
      orthographicProjectionMatrixNoMag,
      lightsFrameBuffer,
      gameFrameBuffer,
      cNc.canvas.width,
      cNc.canvas.height,
      cNc.magnification,
      metrics
    )
    metrics.record(DrawLightsLayerEndMetric)

    // Image based lighting
    metrics.record(DrawLightingLayerStartMetric)
    RendererFunctions.setLightingBlend(gl)
    layerRenderer.drawLayer(
      lightingProjection,
      cloneBlankDisplayObjects,
      DisplayObjectConversions.sceneNodesToDisplayObjects(scene.lightingLayer.nodes, gameTime, assetMapping, boundaryLocator, animationsRegister, fontRegister, metrics),
      lightingFrameBuffer,
      scene.ambientLight.toClearColor,
      lightingShaderProgram,
      CurrentDrawLayer.Lighting,
      metrics
    )
    metrics.record(DrawLightingLayerEndMetric)

    // Distortion
    metrics.record(DrawDistortionLayerStartMetric)
    RendererFunctions.setDistortionBlend(gl)
    layerRenderer.drawLayer(
      lightingProjection,
      cloneBlankDisplayObjects,
      DisplayObjectConversions.sceneNodesToDisplayObjects(scene.distortionLayer.nodes, gameTime, assetMapping, boundaryLocator, animationsRegister, fontRegister, metrics),
      distortionFrameBuffer,
      ClearColor(0.5, 0.5, 1.0, 1.0),
      distortionShaderProgram,
      CurrentDrawLayer.Distortion,
      metrics
    )
    metrics.record(DrawDistortionLayerEndMetric)

    // UI
    metrics.record(DrawUiLayerStartMetric)
    RendererFunctions.setNormalBlend(gl)
    layerRenderer.drawLayer(
      uiProjection,
      cloneBlankDisplayObjects,
      DisplayObjectConversions.sceneNodesToDisplayObjects(scene.uiLayer.nodes, gameTime, assetMapping, boundaryLocator, animationsRegister, fontRegister, metrics),
      uiFrameBuffer,
      ClearColor.Black.forceTransparent,
      standardShaderProgram,
      CurrentDrawLayer.UI,
      metrics
    )
    metrics.record(DrawUiLayerEndMetric)

    // Merge
    metrics.record(RenderToWindowStartMetric)
    RendererFunctions.setNormalBlend(gl2)
    mergeRenderer.drawLayer(
      orthographicProjectionMatrixNoMag,
      gameFrameBuffer,
      lightsFrameBuffer,
      lightingFrameBuffer,
      distortionFrameBuffer,
      uiFrameBuffer,
      cNc.canvas.width,
      cNc.canvas.height,
      config.clearColor,
      scene.screenEffects.gameColorOverlay,
      scene.screenEffects.uiColorOverlay,
      scene.gameLayer.tint,
      scene.lightingLayer.tint,
      scene.uiLayer.tint,
      scene.gameLayer.saturation,
      scene.lightingLayer.saturation,
      scene.uiLayer.saturation,
      metrics
    )
    metrics.record(RenderToWindowEndMetric)

    // Finally, persist animation states...
    // metrics.record(PersistAnimationStatesStartMetric)
    // AnimationsRegister.persistAnimationStates()
    // metrics.record(PersistAnimationStatesEndMetric)
  }

  def resize(canvas: html.Canvas, magnification: Int): Unit = {
    val actualWidth  = canvas.width
    val actualHeight = canvas.height

    if (!resizeRun || (lastWidth !== actualWidth) || (lastHeight !== actualHeight)) {
      resizeRun = true
      lastWidth = actualWidth
      lastHeight = actualHeight

      orthographicProjectionMatrix = RendererFunctions.mat4ToJsArray(Matrix4.orthographic(actualWidth.toDouble / magnification, actualHeight.toDouble / magnification))
      orthographicProjectionMatrixNoMag = RendererFunctions.mat4ToJsArray(Matrix4.orthographic(actualWidth.toDouble, actualHeight.toDouble)).map(_.toFloat)

      gameFrameBuffer = FrameBufferFunctions.createFrameBufferMulti(gl, actualWidth, actualHeight)
      lightsFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      lightingFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      distortionFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)
      uiFrameBuffer = FrameBufferFunctions.createFrameBufferSingle(gl, actualWidth, actualHeight)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      ()
    }
  }

}
