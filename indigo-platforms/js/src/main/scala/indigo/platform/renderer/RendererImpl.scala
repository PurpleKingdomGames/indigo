package indigo.platform.renderer

import indigo.shared.datatypes.AmbientLight
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

@SuppressWarnings(Array("org.wartremover.warts.Null"))
final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends Renderer {

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

  private val layerRenderer: RendererLayer =
    new RendererLayer(gl2, textureLocations, config.maxBatchSize)

  private val vertexBuffer: WebGLBuffer  = gl.createBuffer()
  private val textureBuffer: WebGLBuffer = gl.createBuffer()

  private val vao = gl2.createVertexArray()

  private val standardShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Pixel", StandardPixelArtVert.shader, StandardPixelArtFrag.shader)
  private val lightingShaderProgram =
    RendererFunctions.shaderProgramSetup(gl, "Lighting", StandardLightingPixelArtVert.shader, StandardLightingPixelArtFrag.shader)

  private val gameFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents =
    FrameBufferFunctions.createFrameBuffer(gl, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {

    val vertices: scalajs.js.Array[Double] = {
      val vert0 = scalajs.js.Array[Double](-0.5, -0.5, 1.0d)
      val vert1 = scalajs.js.Array[Double](-0.5, 0.5, 1.0d)
      val vert2 = scalajs.js.Array[Double](0.5, -0.5, 1.0d)
      val vert3 = scalajs.js.Array[Double](0.5, 0.5, 1.0d)

      vert0 ++ vert1 ++ vert2 ++ vert3
    }

    val textureCoordinates: scalajs.js.Array[Double] = {
      val tx0 = scalajs.js.Array[Double](0.0, 1.0)
      val tx1 = scalajs.js.Array[Double](0.0, 0.0)
      val tx2 = scalajs.js.Array[Double](1.0, 1.0)
      val tx3 = scalajs.js.Array[Double](1.0, 0.0)

      tx0 ++ tx1 ++ tx2 ++ tx3
    }

    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)

    gl2.bindVertexArray(vao)

    // Vertex
    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(vertices), STATIC_DRAW)
    gl.enableVertexAttribArray(0)
    gl.vertexAttribPointer(
      indx = 0,
      size = 3,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    // Bind texture coords
    gl.bindBuffer(ARRAY_BUFFER, textureBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(textureCoordinates), STATIC_DRAW)
    gl.enableVertexAttribArray(1)
    gl.vertexAttribPointer(
      indx = 1,
      size = 2,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )

    gl2.bindVertexArray(null)

    List(standardShaderProgram, lightingShaderProgram).foreach { shaderProgram =>
      gl.useProgram(shaderProgram)
      val textureLocation = gl2.getUniformLocation(shaderProgram, "u_texture")
      gl2.uniform1i(textureLocation, 0)
    }
  }

  def drawScene(gameTime: GameTime, scene: SceneUpdateFragment, assetMapping: AssetMapping, metrics: Metrics): Unit = {

    gl2.bindVertexArray(vao)

    val cloneBlankDisplayObjects: Map[String, DisplayObject] =
      scene.cloneBlanks.foldLeft(Map.empty[String, DisplayObject]) { (acc, blank) =>
        blank.cloneable match {
          case g: Graphic =>
            acc + (blank.id.value -> DisplayObjectConversions.graphicToDisplayObject(g, assetMapping))

          case s: Sprite =>
            AnimationsRegister.fetchFromCache(gameTime, s.bindingKey, s.animationsKey, metrics) match {
              case None =>
                acc

              case Some(anim) =>
                acc + (blank.id.value -> DisplayObjectConversions.spriteToDisplayObject(s, assetMapping, anim))
            }
        }
      }

    RendererFunctions.resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight, cNc.magnification)

    metrics.record(DrawGameLayerStartMetric)
    RendererFunctions.setNormalBlend(gl)
    layerRenderer.drawLayer(
      cloneBlankDisplayObjects,
      DisplayObjectConversions.sceneNodesToDisplayObjects(scene.gameLayer, gameTime, assetMapping, metrics),
      gameFrameBuffer,
      config.clearColor,
      standardShaderProgram,
      CurrentDrawLayer.Game,
      metrics
    )
    metrics.record(DrawGameLayerEndMetric)

    metrics.record(DrawLightingLayerStartMetric)
    RendererFunctions.setLightingBlend(gl)
    layerRenderer.drawLayer(
      cloneBlankDisplayObjects,
      DisplayObjectConversions.sceneNodesToDisplayObjects(scene.lightingLayer, gameTime, assetMapping, metrics),
      lightingFrameBuffer,
      AmbientLight.toClearColor(scene.ambientLight),
      lightingShaderProgram,
      CurrentDrawLayer.Lighting,
      metrics
    )
    metrics.record(DrawLightingLayerEndMetric)

    metrics.record(DrawUiLayerStartMetric)
    RendererFunctions.setNormalBlend(gl)
    layerRenderer.drawLayer(
      cloneBlankDisplayObjects,
      DisplayObjectConversions.sceneNodesToDisplayObjects(scene.uiLayer, gameTime, assetMapping, metrics),
      uiFrameBuffer,
      ClearColor.Black.forceTransparent,
      standardShaderProgram,
      CurrentDrawLayer.UI,
      metrics
    )
    metrics.record(DrawUiLayerEndMetric)

    metrics.record(RenderToWindowStartMetric)
    RendererFunctions.setNormalBlend(gl2)
    mergeRenderer.drawLayer(
      gameFrameBuffer,
      lightingFrameBuffer,
      uiFrameBuffer,
      cNc.width,
      cNc.height,
      config.clearColor,
      metrics
    )
    metrics.record(RenderToWindowEndMetric)

    // Finally, persist animation states...
    metrics.record(PersistAnimationStatesStartMetric)
    AnimationsRegister.persistAnimationStates()
    metrics.record(PersistAnimationStatesEndMetric)
  }

}
