package indigoextras.ui

import indigo.shared.events.GlobalEvent

final case class FakeEvent(message: String) extends GlobalEvent
