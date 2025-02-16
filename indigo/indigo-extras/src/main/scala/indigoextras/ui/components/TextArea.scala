package indigoextras.ui.components

import indigo.*
import indigo.syntax.*
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.Coords
import indigoextras.ui.datatypes.Dimensions
import indigoextras.ui.datatypes.UIContext

import scala.annotation.targetName

/** `TextArea`s are a simple stateless component that render multi-line text.
  */
final case class TextArea[ReferenceData](
    text: UIContext[ReferenceData] => List[String],
    render: (Coords, List[String], Dimensions) => Outcome[Layer],
    calculateBounds: (UIContext[ReferenceData], List[String]) => Bounds
):
  def withText(value: String): TextArea[ReferenceData] =
    this.copy(text = _ => value.split("\n").toList)
  def withText(f: UIContext[ReferenceData] => String): TextArea[ReferenceData] =
    this.copy(text = (r: UIContext[ReferenceData]) => f(r).split("\n").toList)

object TextArea:

  def apply[ReferenceData](text: String, calculateBounds: (UIContext[ReferenceData], List[String]) => Bounds)(
      present: (Coords, List[String], Dimensions) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      (_: UIContext[ReferenceData]) => text.split("\n").toList,
      present,
      calculateBounds
    )

  @targetName("TextAreaRefToString")
  def apply[ReferenceData](
      text: UIContext[ReferenceData] => String,
      calculateBounds: (UIContext[ReferenceData], List[String]) => Bounds
  )(
      present: (Coords, List[String], Dimensions) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      (r: UIContext[ReferenceData]) => text(r).split("\n").toList,
      present,
      calculateBounds
    )

  def apply[ReferenceData](text: String, bounds: Bounds)(
      present: (Coords, List[String], Dimensions) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      (_: UIContext[ReferenceData]) => text.split("\n").toList,
      present,
      (_, _) => bounds
    )

  @targetName("TextAreaRefToStringFixedBounds")
  def apply[ReferenceData](
      text: UIContext[ReferenceData] => String,
      bounds: Bounds
  )(
      present: (Coords, List[String], Dimensions) => Outcome[Layer]
  ): TextArea[ReferenceData] =
    TextArea(
      (r: UIContext[ReferenceData]) => text(r).split("\n").toList,
      present,
      (_, _) => bounds
    )

  given [ReferenceData]: Component[TextArea[ReferenceData], ReferenceData] with
    def bounds(context: UIContext[ReferenceData], model: TextArea[ReferenceData]): Bounds =
      model.calculateBounds(context, model.text(context))

    def updateModel(
        context: UIContext[ReferenceData],
        model: TextArea[ReferenceData]
    ): GlobalEvent => Outcome[TextArea[ReferenceData]] =
      _ => Outcome(model)

    def present(
        context: UIContext[ReferenceData],
        model: TextArea[ReferenceData]
    ): Outcome[Layer] =
      model.render(
        context.parent.coords,
        model.text(context),
        bounds(context, model).dimensions
      )

    def refresh(
        context: UIContext[ReferenceData],
        model: TextArea[ReferenceData]
    ): TextArea[ReferenceData] =
      model
