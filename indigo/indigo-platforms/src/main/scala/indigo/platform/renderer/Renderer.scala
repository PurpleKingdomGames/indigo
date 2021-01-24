package indigo.platform.renderer

import indigo.shared.datatypes.mutable.CheapMatrix4
import indigo.shared.platform.ProcessedSceneData
import indigo.shared.display.Shader

trait Renderer {

  def screenWidth: Int
  def screenHeight: Int
  def orthographicProjectionMatrix: CheapMatrix4

  def init(shaders: Set[Shader]): Unit
  def drawScene(sceneData: ProcessedSceneData): Unit
}
