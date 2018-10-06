package indigo.networking

import indigo.gameengine.events.{GameEvent, ViewEvent}

trait NetworkSendEvent    extends ViewEvent
trait NetworkReceiveEvent extends GameEvent
