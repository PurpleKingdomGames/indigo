package indigo.shared.platform

import indigo.shared.events.GlobalEvent

trait GlobalEventStream {
  def pushGlobalEvent(e: GlobalEvent): Unit
  def collect: List[GlobalEvent]
}