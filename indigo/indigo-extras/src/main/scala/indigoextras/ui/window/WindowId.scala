package indigoextras.ui.window

opaque type WindowId = String

object WindowId:
  def apply(id: String): WindowId               = id
  extension (id: WindowId) def toString: String = id

  given CanEqual[WindowId, WindowId] = CanEqual.derived
