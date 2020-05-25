package indigo.shared.platform

import indigo.shared.datatypes.Matrix4

trait Renderer {

  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: Matrix4

  def init(): Unit
  def drawScene(sceneData: ProcessedSceneData): Unit
}
