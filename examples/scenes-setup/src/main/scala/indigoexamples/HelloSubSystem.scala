package indigoexamples

import indigo._

final case class HelloSubSystem(initialMessage: String, fontKey: FontKey) extends SubSystem {
  type EventType      = GlobalEvent
  type SubSystemModel = String

  val eventFilter: GlobalEvent => Option[EventType] = _ => None

  def initialModel: String = initialMessage

  def update(context: SubSystemFrameContext, message: String): EventType => Outcome[String] = _ => Outcome(message)

  def render(context: SubSystemFrameContext, message: String): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(Text(message, 20, 50, 1, fontKey))
}
