package indigo.shared.events

import indigo.shared.events

trait EventTypeAliases {

  type GlobalEvent = events.GlobalEvent
  type InputEvent  = events.InputEvent

  type InputState = events.InputState

  type MouseEvent = events.MouseEvent
  val MouseEvent: events.MouseEvent.type = events.MouseEvent

  type KeyboardEvent = events.KeyboardEvent
  val KeyboardEvent: events.KeyboardEvent.type = events.KeyboardEvent

  type FrameTick = events.FrameTick.type
  val FrameTick: events.FrameTick.type = events.FrameTick

  type PlaySound = events.PlaySound
  val PlaySound: events.PlaySound.type = events.PlaySound

  type NetworkSendEvent    = events.NetworkSendEvent
  type NetworkReceiveEvent = events.NetworkReceiveEvent

  type StorageEvent = events.StorageEvent

  type Save = events.StorageEvent.Save
  val Save: events.StorageEvent.Save.type = events.StorageEvent.Save

  type Load = events.StorageEvent.Load
  val Load: events.StorageEvent.Load.type = events.StorageEvent.Load

  type Delete = events.StorageEvent.Delete
  val Delete: events.StorageEvent.Delete.type = events.StorageEvent.Delete

  val DeleteAll: events.StorageEvent.DeleteAll.type = events.StorageEvent.DeleteAll

  type Loaded = events.StorageEvent.Loaded
  val Loaded: events.StorageEvent.Loaded.type = events.StorageEvent.Loaded

}
