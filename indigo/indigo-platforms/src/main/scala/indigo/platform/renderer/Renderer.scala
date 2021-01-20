package indigo.platform.renderer

import indigo.facades.worker.ProcessedSceneData
import scala.scalajs.js

trait Renderer {

  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: js.Array[Double]

  def init(): Unit
  def drawScene(sceneData: ProcessedSceneData): Unit
}
