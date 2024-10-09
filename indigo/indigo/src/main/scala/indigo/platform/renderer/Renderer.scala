package indigo.platform.renderer

import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.shader.RawShaderCode
import indigo.shared.time.Seconds
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey

trait Renderer:
  def renderingTechnology: RenderingTechnology
  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: CheapMatrix4

  def init(shaders: Set[RawShaderCode]): Unit
  def drawScene(sceneData: ProcessedSceneData, runningTime: Seconds): Unit
  def captureScreen(
      clippingRect: Rectangle = Rectangle(Size(screenWidth, screenHeight)),
      excludeLayers: Batch[BindingKey] = Batch.empty
  ): Batch[Byte]
