package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Layer
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

object Helper:

  extension [A, ReferenceData](component: A)(using c: Component[A, ReferenceData])
    def update[StartupData, ContextData](
        context: UIContext[ReferenceData]
    ): GlobalEvent => Outcome[A] =
      c.updateModel(context, component)

    def present[StartupData, ContextData](
        context: UIContext[ReferenceData]
    ): Outcome[Layer] =
      c.present(context, component)

    def refresh(
        reference: ReferenceData,
        parentDimensions: Dimensions
    ): A =
      c.refresh(reference, component, parentDimensions)
