package indigoextras.ui.components

import indigo.*
import indigo.syntax.*
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

/** Labels are a simple `Component` that render text.
  */
final case class Label[ReferenceData](
    text: ReferenceData => String,
    render: (Coords, String, Dimensions) => Outcome[Layer],
    calculateBounds: (ReferenceData, String) => Bounds
):
  def withText(value: String): Label[ReferenceData] =
    this.copy(text = _ => value)
  def withText(f: ReferenceData => String): Label[ReferenceData] =
    this.copy(text = f)

object Label:

  /** Minimal label constructor with custom rendering function
    */
  def apply[ReferenceData](text: String, calculateBounds: (ReferenceData, String) => Bounds)(
      present: (Coords, String, Dimensions) => Outcome[Layer]
  ): Label[ReferenceData] =
    Label(_ => text, present, calculateBounds)

  given [ReferenceData]: Component[Label[ReferenceData], ReferenceData] with
    def bounds(reference: ReferenceData, model: Label[ReferenceData]): Bounds =
      model.calculateBounds(reference, model.text(reference))

    def updateModel(
        context: UIContext[ReferenceData],
        model: Label[ReferenceData]
    ): GlobalEvent => Outcome[Label[ReferenceData]] =
      _ => Outcome(model)

    def present(
        context: UIContext[ReferenceData],
        model: Label[ReferenceData]
    ): Outcome[Layer] =
      val t = model.text(context.reference)
      model.render(context.bounds.coords, t, model.calculateBounds(context.reference, t).dimensions)

    def refresh(
        reference: ReferenceData,
        model: Label[ReferenceData],
        parentDimensions: Dimensions
    ): Label[ReferenceData] =
      model
