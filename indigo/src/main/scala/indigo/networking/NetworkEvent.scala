package indigo.networking

import indigo.gameengine.events.{GameEvent, FrameEvent}

trait NetworkSendEvent extends FrameEvent
trait NetworkReceiveEvent extends GameEvent {
  val isGameEvent: Boolean = true
}
