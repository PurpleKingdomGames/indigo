package indigo.gameengine.events
import indigo.gameengine.events

trait EventTypeAliases {

  type FrameInputEvents = events.FrameInputEvents
  type GlobalEvent      = events.GlobalEvent

  type Signals = events.Signals
  val Signals: events.Signals.type = events.Signals

  type MouseEvent = events.MouseEvent
  val MouseEvent: events.MouseEvent.type = events.MouseEvent

  type KeyboardEvent = events.KeyboardEvent
  val KeyboardEvent: events.KeyboardEvent.type = events.KeyboardEvent

  val FrameTick: events.FrameTick.type = events.FrameTick

  type PlaySound = events.PlaySound
  val PlaySound: events.PlaySound.type = events.PlaySound

  type NetworkSendEvent    = events.NetworkSendEvent
  type NetworkReceiveEvent = events.NetworkReceiveEvent

}
