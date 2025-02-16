package indigoextras.ui.components

import indigo.*
import indigo.syntax.*
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

import scala.annotation.targetName

/** Labels are a simple `Component` that render text.
  */
final case class Label[ReferenceData](
    text: UIContext[ReferenceData] => String,
    render: (UIContext[ReferenceData], Label[ReferenceData]) => Outcome[Layer],
    calculateBounds: (UIContext[ReferenceData], String) => Bounds
):
  def withText(value: String): Label[ReferenceData] =
    this.copy(text = _ => value)
  def withText(f: UIContext[ReferenceData] => String): Label[ReferenceData] =
    this.copy(text = f)

object Label:

  /** Minimal label constructor with custom rendering function
    */
  def apply[ReferenceData](text: String, calculateBounds: (UIContext[ReferenceData], String) => Bounds)(
      present: (UIContext[ReferenceData], Label[ReferenceData]) => Outcome[Layer]
  ): Label[ReferenceData] =
    Label(_ => text, present, calculateBounds)

  /** Minimal label constructor with custom rendering function for dynamic text
    */
  @targetName("LabelApplyDynamicText")
  def apply[ReferenceData](
      dynamicText: UIContext[ReferenceData] => String,
      calculateBounds: (UIContext[ReferenceData], String) => Bounds
  )(
      present: (UIContext[ReferenceData], Label[ReferenceData]) => Outcome[Layer]
  ): Label[ReferenceData] =
    Label(dynamicText, present, calculateBounds)

  /** Minimal label constructor with custom rendering function with fixed bounds
    */
  def apply[ReferenceData](text: String, bounds: Bounds)(
      present: (UIContext[ReferenceData], Label[ReferenceData]) => Outcome[Layer]
  ): Label[ReferenceData] =
    Label(_ => text, present, (_, _) => bounds)

  /** Minimal label constructor with custom rendering function for dynamic text with fixed bounds
    */
  def apply[ReferenceData](dynamicText: UIContext[ReferenceData] => String, bounds: Bounds)(
      present: (UIContext[ReferenceData], Label[ReferenceData]) => Outcome[Layer]
  ): Label[ReferenceData] =
    Label(dynamicText, present, (_, _) => bounds)

  given [ReferenceData]: Component[Label[ReferenceData], ReferenceData] with
    def bounds(context: UIContext[ReferenceData], model: Label[ReferenceData]): Bounds =
      model.calculateBounds(context, model.text(context))

    def updateModel(
        context: UIContext[ReferenceData],
        model: Label[ReferenceData]
    ): GlobalEvent => Outcome[Label[ReferenceData]] =
      _ => Outcome(model)

    def present(
        context: UIContext[ReferenceData],
        model: Label[ReferenceData]
    ): Outcome[Layer] =
      model.render(context, model)

    def refresh(
        context: UIContext[ReferenceData],
        model: Label[ReferenceData]
    ): Label[ReferenceData] =
      model
