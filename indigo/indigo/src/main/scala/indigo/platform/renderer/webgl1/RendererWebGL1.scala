package indigo.platform.renderer.webgl1

import indigo.AssetName
import indigo.AssetPath
import indigo.platform.assets.AtlasId
import indigo.platform.events.GlobalEventStream
import indigo.platform.renderer.Renderer
import indigo.platform.renderer.ScreenCaptureConfig
import indigo.platform.renderer.shared.CameraHelper
import indigo.platform.renderer.shared.ContextAndCanvas
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.TextureLookupResult
import indigo.platform.renderer.shared.WebGLHelper
import indigo.shared.assets.AssetType
import indigo.shared.collections.Batch
import indigo.shared.config.GameViewport
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.display.DisplayEntity
import indigo.shared.display.DisplayGroup
import indigo.shared.display.DisplayObject
import indigo.shared.display.DisplayTextLetters
import indigo.shared.events.ViewportResize
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.platform.RendererConfig
import indigo.shared.scenegraph.Camera
import indigo.shared.shader.RawShaderCode
import indigo.shared.time.Seconds
import org.scalajs.dom
import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.*
import org.scalajs.dom.html

import scala.scalajs.js.typedarray.Float32Array

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
final class RendererWebGL1(
    config: RendererConfig,
    loadedTextureAssets: scalajs.js.Array[LoadedTextureAsset],
    cNc: ContextAndCanvas,
    globalEventStream: GlobalEventStream
) extends Renderer {

  val renderingTechnology: RenderingTechnology = RenderingTechnology.WebGL1

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var resizeRun: Boolean = false
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastWidth: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var lastHeight: Int = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

  // Store previous data in order to take screenshots
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _prevSceneData: ProcessedSceneData = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _prevGameRuntime: Seconds = Seconds.zero

  def screenWidth: Int  = lastWidth
  def screenHeight: Int = lastWidth

  private val gl: WebGLRenderingContext = cNc.context
  private val vertexBuffer: WebGLBuffer = gl.createBuffer()
  private val standardShaderProgram = WebGLHelper.shaderProgramSetup(gl, "Pixel", indigo.shared.shader.library.WebGL1)

  private val textureLocations: scalajs.js.Array[TextureLookupResult] =
    gl.pixelStorei(UNPACK_PREMULTIPLY_ALPHA_WEBGL, 1);
    loadedTextureAssets.map(li => TextureLookupResult(li.name, WebGLHelper.organiseImage(gl, li.data)))

  def init(shaders: Set[RawShaderCode]): Unit = {
    gl.disable(DEPTH_TEST)
    gl.viewport(0, 0, gl.drawingBufferWidth.toDouble, gl.drawingBufferHeight.toDouble)
    gl.enable(BLEND)
    gl.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)

    val verticesAndTextureCoords: scalajs.js.Array[Float] = {
      val vert0 = scalajs.js.Array[Float](-0.5f, -0.5f, 0.0f, 1.0f)
      val vert1 = scalajs.js.Array[Float](-0.5f, 0.5f, 0.0f, 0.0f)
      val vert2 = scalajs.js.Array[Float](0.5f, -0.5f, 1.0f, 1.0f)
      val vert3 = scalajs.js.Array[Float](0.5f, 0.5f, 1.0f, 0.0f)

      vert0 ++ vert1 ++ vert2 ++ vert3
    }

    gl.bindBuffer(ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(ARRAY_BUFFER, new Float32Array(verticesAndTextureCoords), STATIC_DRAW)

    gl.useProgram(standardShaderProgram)

    val verticesLocation1 = gl.getAttribLocation(standardShaderProgram, "a_verticesAndCoords")
    bindAttibuteBuffer(gl, verticesLocation1, 4)

    gl.bindBuffer(ARRAY_BUFFER, null)

    gl.bindFramebuffer(FRAMEBUFFER, null)
  }

  private given CanEqual[Option[Int], Option[Int]] = CanEqual.derived

  def captureScreen(captureOptions: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] =
    _prevSceneData match {
      case null => captureOptions.map(_ => Left("No scene data to capture"))
      case _ =>
        val prevSceneData   = _prevSceneData
        val prevGameRuntime = _prevGameRuntime

        captureOptions.map(option =>
          val canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
          val ctx2d =
            canvas.getContext("2d", cNc.context.getContextAttributes()).asInstanceOf[dom.CanvasRenderingContext2D]
          val magnifiedClip = option.croppingRect match {
            case Some(rect) => rect * cNc.magnification
            case None       => Rectangle(0, 0, screenWidth, screenHeight)
          }
          val imageSize = Size(
            (magnifiedClip.width * option.scale.getOrElse(Vector2.one).x).toInt,
            (magnifiedClip.height * option.scale.getOrElse(Vector2.one).y).toInt
          )

          canvas.width = imageSize.width
          canvas.height = imageSize.height
          ctx2d.imageSmoothingEnabled = false

          drawScene(
            ProcessedSceneData(
              _prevSceneData.layers.filter(l =>
                l.layerKey match {
                  case Some(key) => option.excludeLayers.exists(_ == key) == false
                  case None      => true
                }
              ),
              _prevSceneData.cloneBlankDisplayObjects,
              _prevSceneData.shaderId,
              _prevSceneData.shaderUniformData,
              _prevSceneData.camera
            ),
            _prevGameRuntime
          )

          _prevSceneData = prevSceneData
          _prevGameRuntime = prevGameRuntime

          ctx2d.drawImage(
            cNc.canvas,
            magnifiedClip.x,
            magnifiedClip.y,
            magnifiedClip.width,
            magnifiedClip.height,
            0,
            0,
            imageSize.width,
            imageSize.height
          )
          val dataUrl = canvas.toDataURL(option.imageType.toString())
          canvas.remove()

          Right(
            AssetType.Image(
              AssetName(option.name.getOrElse(s"capture-${System.currentTimeMillis()}")),
              AssetPath(dataUrl)
            )
          )
        )
    }

  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = {
    resize(cNc.canvas, cNc.magnification)

    gl.clearColor(config.clearColor.r, config.clearColor.g, config.clearColor.b, config.clearColor.a)
    gl.clear(COLOR_BUFFER_BIT)

    val gameProjection: scala.scalajs.js.Array[Double] = orthographicProjectionMatrix.toJSArray.map(_.toDouble)

    sceneData.layers.foreach { layer =>
      val maybeCamera: Option[Camera] =
        layer.camera.orElse(sceneData.camera)

      val projection: scala.scalajs.js.Array[Double] =
        (layer.magnification, maybeCamera) match {
          case (None, None) =>
            gameProjection

          case (Some(m), None) =>
            CameraHelper
              .calculateCameraMatrix(
                cNc.canvas.width.toDouble,
                cNc.canvas.height.toDouble,
                m.toDouble,
                m.toDouble,
                0,
                0,
                1,
                false,
                Radians.zero,
                false
              )
              .toJSArray
              .map(_.toDouble)

          case (None, Some(c)) =>
            CameraHelper
              .calculateCameraMatrix(
                cNc.canvas.width.toDouble,
                cNc.canvas.height.toDouble,
                cNc.magnification.toDouble,
                1.0,
                c.position.x.toDouble,
                c.position.y.toDouble,
                c.zoom.toDouble,
                false,
                c.rotation,
                c.isLookAt
              )
              .toJSArray
              .map(_.toDouble)

          case (Some(m), Some(c)) =>
            CameraHelper
              .calculateCameraMatrix(
                cNc.canvas.width.toDouble,
                cNc.canvas.height.toDouble,
                m.toDouble,
                1.0,
                c.position.x.toDouble,
                c.position.y.toDouble,
                c.zoom.toDouble,
                false,
                c.rotation,
                c.isLookAt
              )
              .toJSArray
              .map(_.toDouble)
        }

      drawLayer(layer.entities, standardShaderProgram, projection)
    }

    // Store the data for screenshots
    _prevSceneData = sceneData
    _prevGameRuntime = runningTime
  }

  def drawLayer(
      displayEntities: scalajs.js.Array[DisplayEntity],
      shaderProgram: WebGLProgram,
      projectionMatrix: scalajs.js.Array[Double]
  ): Unit = {

    gl.uniformMatrix4fv(
      location = gl.getUniformLocation(shaderProgram, "u_projection"),
      transpose = false,
      value = projectionMatrix
    )

    gl.uniform1i(gl.getUniformLocation(shaderProgram, "u_texture"), 0)

    renderEntities(displayEntities, shaderProgram, CheapMatrix4.identity)
  }

  def setBaseTransform(shaderProgram: WebGLProgram, baseTransform: CheapMatrix4): Unit =
    gl.uniformMatrix4fv(
      location = gl.getUniformLocation(shaderProgram, "u_baseTransform"),
      transpose = false,
      value = Float32Array(baseTransform.toJSArray)
    )

  def renderEntities(
      displayEntities: scalajs.js.Array[DisplayEntity],
      shaderProgram: WebGLProgram,
      baseTransform: CheapMatrix4
  ): Unit =
    setBaseTransform(shaderProgram, baseTransform)

    // This basic renderer only renders entities with images. So we can save work by only considering
    // 1) display objects (and groups of display objects), that 2) have an atlas reference.
    displayEntities
      .collect {
        case d: DisplayObject if d.atlasName.isDefined =>
          (d, d.atlasName.get)

        case g: DisplayGroup =>
          (g, AtlasId(""))

        case l: DisplayTextLetters =>
          (l, AtlasId(""))
      }
      .foreach {
        case (letters: DisplayTextLetters, _) =>
          renderEntities(letters.letters, shaderProgram, baseTransform)
          setBaseTransform

        case (group: DisplayGroup, _) =>
          renderEntities(group.entities, shaderProgram, group.transform)
          setBaseTransform

        case (displayObject: DisplayObject, objectAtlas) =>
          setupVertexShaderState(
            gl,
            displayObject,
            shaderProgram
          )

          textureLocations.find(t => t.name == objectAtlas) match {
            case None =>
              gl.activeTexture(TEXTURE0);
              gl.bindTexture(TEXTURE_2D, null)

            case Some(textureLookup) =>
              gl.activeTexture(TEXTURE0);
              gl.bindTexture(TEXTURE_2D, textureLookup.texture)
          }

          gl.drawArrays(TRIANGLE_STRIP, 0, 4)

        case _ =>
          ()
      }

  def resize(canvas: html.Canvas, magnification: Int): Unit = {
    val actualWidth  = canvas.width
    val actualHeight = canvas.height

    if (!resizeRun || (lastWidth != actualWidth) || (lastHeight != actualHeight)) {
      resizeRun = true
      lastWidth = actualWidth
      lastHeight = actualHeight
      orthographicProjectionMatrix =
        CheapMatrix4.orthographic(actualWidth.toFloat / magnification, actualHeight.toFloat / magnification)

      gl.viewport(0, 0, actualWidth.toDouble, actualHeight.toDouble)

      globalEventStream.pushGlobalEvent(ViewportResize(GameViewport(actualWidth, actualHeight)))

      ()
    }
  }

  def bindAttibuteBuffer(gl: WebGLRenderingContext, attributeLocation: Int, size: Int): Unit = {
    gl.enableVertexAttribArray(attributeLocation)
    gl.vertexAttribPointer(
      indx = attributeLocation,
      size = size,
      `type` = FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
  }

  def setupVertexShaderState(
      gl: WebGLRenderingContext,
      displayObject: DisplayObject,
      shaderProgram: WebGLProgram
  ): Unit = {

    gl.uniform4f(
      location = gl.getUniformLocation(shaderProgram, "u_translateScale"),
      displayObject.x,
      displayObject.y,
      displayObject.scaleX,
      displayObject.scaleY
    )

    gl.uniform4f(
      location = gl.getUniformLocation(shaderProgram, "u_refRotation"),
      displayObject.refX,
      displayObject.refY,
      0,
      displayObject.rotation.toDouble
    )

    gl.uniform4f(
      gl.getUniformLocation(shaderProgram, "u_frameTransform"),
      displayObject.channelOffset0X.toDouble,
      displayObject.channelOffset0Y.toDouble,
      displayObject.frameScaleX.toDouble,
      displayObject.frameScaleY.toDouble
    )

    gl.uniform4f(
      location = gl.getUniformLocation(shaderProgram, "u_sizeFlip"),
      displayObject.width,
      displayObject.height,
      displayObject.flipX,
      displayObject.flipY
    )
  }

}
