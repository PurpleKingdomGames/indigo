package indigo.platform.renderer

import indigo.shared.collections.Batch
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.shader.RawShaderCode
import indigo.shared.time.Seconds
import indigo.shared.ImageData
import indigo.shared.ImageType

trait Renderer:
  def renderingTechnology: RenderingTechnology
  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: CheapMatrix4

  def init(shaders: Set[RawShaderCode]): Unit
  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit
  def captureScreen(
      clippingRect: Rectangle = Rectangle(Size(screenWidth, screenHeight)),
      excludeLayers: Batch[BindingKey] = Batch.empty,
      imageType: ImageType = ImageType.PNG
  ): ImageData

object Renderer:
  def blackHole = new Renderer {
    def renderingTechnology: RenderingTechnology   = RenderingTechnology.WebGL1
    def screenWidth: Int                           = 0
    def screenHeight: Int                          = 0
    def orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

    def init(shaders: Set[RawShaderCode]): Unit                              = ()
    def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = ()
    def captureScreen(
        clippingRect: Rectangle,
        excludeLayers: Batch[BindingKey],
        imageType: ImageType = ImageType.PNG
    ): ImageData = ImageData(0, ImageType.PNG, Array.emptyByteArray)
  }
