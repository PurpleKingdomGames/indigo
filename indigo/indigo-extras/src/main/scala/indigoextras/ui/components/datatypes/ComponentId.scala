package indigoextras.ui.components.datatypes

opaque type ComponentId = String

object ComponentId:

  def apply(value: String): ComponentId = value
  def None: ComponentId                 = ""

  extension (c: ComponentId) def value: String = c
