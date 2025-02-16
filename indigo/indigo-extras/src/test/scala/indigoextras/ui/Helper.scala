package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.UIContext

object Helper:

  extension [A, ReferenceData](component: A)(using c: Component[A, ReferenceData])
    def bounds(context: UIContext[ReferenceData]): Bounds =
      c.bounds(context, component)

    def update(
        context: UIContext[ReferenceData]
    ): GlobalEvent => Outcome[A] =
      c.updateModel(context, component)

    def present(
        context: UIContext[ReferenceData]
    ): Outcome[Layer] =
      c.present(context, component)

    def refresh(
        context: UIContext[ReferenceData]
    ): A =
      c.refresh(context, component)
