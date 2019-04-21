package indigo.shared.platform

import indigo.shared.display.Displayable
import indigo.shared.metrics.Metrics

trait Renderer {
  def init(): Unit
  def drawScene(displayable: Displayable, metrics: Metrics): Unit
}