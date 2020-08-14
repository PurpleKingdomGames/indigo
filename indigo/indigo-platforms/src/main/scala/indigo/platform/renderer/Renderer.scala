package indigo.platform.renderer

import indigo.shared.datatypes.Matrix4
import indigo.shared.platform.ProcessedSceneData

trait Renderer {

  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: Matrix4

  def init(): Unit
  def drawScene(sceneData: ProcessedSceneData): Unit
}
