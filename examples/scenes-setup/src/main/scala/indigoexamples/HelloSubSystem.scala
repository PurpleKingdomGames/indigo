package indigoexamples

import indigo._

final case class HelloSubSystem(initialMessage: String, fontKey: FontKey) extends SubSystem {
  type EventType      = GlobalEvent
  type SubSystemModel = String

  val id: SubSystemId =
    SubSystemId("hello")

  val eventFilter: GlobalEvent => Option[EventType] =
    _ => None

  def initialModel: Outcome[String] =
    Outcome(initialMessage)

  def update(context: SubSystemFrameContext, message: String): EventType => Outcome[String] =
    _ => Outcome(message)

  def present(context: SubSystemFrameContext, message: String): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(Text(message, 20, 50, 1, fontKey, FontStuff.fontMaterial))
    )
}
