package indigoextras.ui.component

import indigo.*
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

/** A typeclass that confirms that some type `A` can be used as a `Component` provides the necessary operations for that
  * type to act as a component.
  */
trait Component[A, ReferenceData]:

  /** The position and size of the component
    */
  def bounds(context: UIContext[ReferenceData], model: A): Bounds

  /** Update this componenet's model.
    */
  def updateModel(
      context: UIContext[ReferenceData],
      model: A
  ): GlobalEvent => Outcome[A]

  /** Produce a renderable output for this component, based on the component's model.
    */
  def present(
      context: UIContext[ReferenceData],
      model: A
  ): Outcome[Layer]

  /** Used internally to instruct the component that the layout has changed in some way, and that it should
    * reflow/refresh it's contents - whatever that means in the context of this component type.
    */
  def refresh(context: UIContext[ReferenceData], model: A): A

object Component:

  given [ReferenceData]: Component[Unit, ReferenceData] =
    new Component[Unit, ReferenceData]:
      def bounds(context: UIContext[ReferenceData], model: Unit): Bounds =
        Bounds.zero

      def updateModel(
          context: UIContext[ReferenceData],
          model: Unit
      ): GlobalEvent => Outcome[Unit] =
        _ => Outcome(model)

      def present(
          context: UIContext[ReferenceData],
          model: Unit
      ): Outcome[Layer] =
        Outcome(Layer.empty)

      def refresh(context: UIContext[ReferenceData], model: Unit): Unit =
        ()
