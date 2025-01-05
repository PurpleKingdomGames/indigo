package indigoextras.ui

import indigo.GlobalEvent
import indigo.Layer
import indigo.Outcome

object syntax:

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
