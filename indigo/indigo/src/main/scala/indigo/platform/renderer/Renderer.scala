package indigo.platform.renderer

import indigo.shared.ImageType
import indigo.shared.assets.AssetType
import indigo.shared.collections.Batch
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.shader.RawShaderCode
import indigo.shared.time.Seconds

trait Renderer:
  def renderingTechnology: RenderingTechnology
  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: CheapMatrix4

  def init(shaders: Set[RawShaderCode]): Unit
  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit

  /** Capture the screen as a number of images, each with the specified configuration
    *
    * @param captureConfig
    *   The configurations to use when capturing the screen
    * @return
    *   A batch containing either the captured images, or error messages
    */
  def captureScreen(captureConfig: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]]

  /** Capture the screen as an image, with the specified configuration
    *
    * @param captureOption
    *   The configuration to use when capturing the screen
    * @return
    *   The captured image, or an error message
    */
  def captureScreen(captureConfig: ScreenCaptureConfig): Either[String, AssetType.Image] =
    captureScreen(Batch(captureConfig)).headOption match {
      case Some(v) => v
      case None    => Left("Could not capture image")
    }

object Renderer:
  def blackHole = new Renderer {
    def renderingTechnology: RenderingTechnology   = RenderingTechnology.WebGL1
    def screenWidth: Int                           = 0
    def screenHeight: Int                          = 0
    def orthographicProjectionMatrix: CheapMatrix4 = CheapMatrix4.identity

    def init(shaders: Set[RawShaderCode]): Unit                              = ()
    def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit = ()
    def captureScreen(captureOptions: Batch[ScreenCaptureConfig]): Batch[Either[String, AssetType.Image]] = Batch(
      Left("No renderer available")
    )
  }
