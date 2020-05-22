package indigoexamples

import indigo._

final case class HelloSubSystem(message: String, fontKey: FontKey) extends SubSystem {
  type EventType = GlobalEvent

  val eventFilter: GlobalEvent => Option[EventType] = _ => None

  def update(context: FrameContext): EventType => Outcome[SubSystem] = _ => Outcome(this)

  def render(context: FrameContext): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addUiLayerNodes(Text(message, 20, 50, 1, fontKey))
}
