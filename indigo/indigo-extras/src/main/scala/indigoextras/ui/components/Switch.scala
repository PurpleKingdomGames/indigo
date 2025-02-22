package indigoextras.ui.components

import indigo.*
import indigo.syntax.*
import indigoextras.ui.component.Component
import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

import datatypes.BoundsType
import datatypes.SwitchState

/** The Switch `Component` allows you to create a two state (on / off) button for your UI. These can be used for simple
  * checkboxes, toggles, and switches, but also with more coordination, compound components like radio button groups.
  */
final case class Switch[ReferenceData](
    bounds: Bounds,
    state: SwitchState,
    on: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer],
    off: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer],
    switch: (UIContext[ReferenceData], Switch[ReferenceData]) => Batch[GlobalEvent],
    boundsType: BoundsType[ReferenceData, Unit],
    isDown: Boolean,
    autoToggle: (UIContext[ReferenceData], Switch[ReferenceData]) => Option[SwitchState]
):
  def withSwitchState(value: SwitchState): Switch[ReferenceData] =
    this.copy(state = value)
  def switchOn: Switch[ReferenceData] =
    withSwitchState(SwitchState.On)
  def switchOff: Switch[ReferenceData] =
    withSwitchState(SwitchState.Off)

  def presentOn(
      on: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer]
  ): Switch[ReferenceData] =
    this.copy(on = on)

  def presentOff(
      off: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer]
  ): Switch[ReferenceData] =
    this.copy(off = off)

  def onSwitch(events: (UIContext[ReferenceData], Switch[ReferenceData]) => Batch[GlobalEvent]): Switch[ReferenceData] =
    this.copy(switch = events)

  def withBoundsType(value: BoundsType[ReferenceData, Unit]): Switch[ReferenceData] =
    this.copy(boundsType = value)

  /** Decide the state of the switch based on the current state and the reference data. Returns an optional value, if
    * `None` is returned the switch will not change state.
    */
  def withAutoToggle(
      f: (UIContext[ReferenceData], Switch[ReferenceData]) => Option[SwitchState]
  ): Switch[ReferenceData] =
    this.copy(autoToggle = f)

object Switch:

  /** Minimal button constructor with custom rendering function
    */
  def apply[ReferenceData](boundsType: BoundsType[ReferenceData, Unit])(
      on: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer],
      off: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer]
  ): Switch[ReferenceData] =
    Switch(
      Bounds.zero,
      SwitchState.Off,
      on,
      off,
      (_, _) => Batch.empty,
      boundsType,
      false,
      (_, _) => None
    )

  /** Minimal button constructor with custom rendering function
    */
  def apply[ReferenceData](bounds: Bounds)(
      on: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer],
      off: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer]
  ): Switch[ReferenceData] =
    Switch(
      bounds,
      SwitchState.Off,
      on,
      off,
      (_, _) => Batch.empty,
      datatypes.BoundsType.Fixed(bounds),
      false,
      (_, _) => None
    )

  /** Minimal button constructor with custom rendering function and dynamic sizing
    */
  def apply[ReferenceData](calculateBounds: UIContext[ReferenceData] => Bounds)(
      on: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer],
      off: (UIContext[ReferenceData], Switch[ReferenceData]) => Outcome[Layer]
  ): Switch[ReferenceData] =
    Switch(
      Bounds.zero,
      SwitchState.Off,
      on,
      off,
      (_, _) => Batch.empty,
      datatypes.BoundsType.Calculated(calculateBounds),
      false,
      (_, _) => None
    )

  given [ReferenceData]: Component[Switch[ReferenceData], ReferenceData] with
    def bounds(context: UIContext[ReferenceData], model: Switch[ReferenceData]): Bounds =
      model.bounds

    def updateModel(
        context: UIContext[ReferenceData],
        model: Switch[ReferenceData]
    ): GlobalEvent => Outcome[Switch[ReferenceData]] =
      case FrameTick =>
        val newBounds =
          model.boundsType match
            case datatypes.BoundsType.Fixed(bounds) =>
              bounds

            case datatypes.BoundsType.Calculated(calculate) =>
              calculate(context, ())

            case _ =>
              model.bounds

        val nextState =
          model.autoToggle(context, model).getOrElse(model.state)

        Outcome(
          model.copy(
            bounds = newBounds,
            state = nextState
          )
        )

      case _: PointerEvent.Down
          if context.isActive && model.bounds
            .moveBy(context.parent.coords + context.parent.additionalOffset)
            .contains(context.pointerCoords) =>
        Outcome(model.copy(isDown = true))

      case _: PointerEvent.Up
          if context.isActive && model.isDown && model.bounds
            .moveBy(context.parent.coords + context.parent.additionalOffset)
            .contains(context.pointerCoords) =>
        val next    = model.state.toggle
        val updated = model.copy(state = next, isDown = false)
        Outcome(updated)
          .addGlobalEvents(model.switch(context, updated))

      case _: PointerEvent.Up =>
        // Released Outside.
        Outcome(model.copy(isDown = false))

      case _ =>
        Outcome(model)

    def present(
        context: UIContext[ReferenceData],
        model: Switch[ReferenceData]
    ): Outcome[Layer] =
      model.state match
        case SwitchState.On =>
          model.on(context, model)

        case SwitchState.Off =>
          model.off(context, model)

    def refresh(
        context: UIContext[ReferenceData],
        model: Switch[ReferenceData]
    ): Switch[ReferenceData] =
      model.boundsType match
        case datatypes.BoundsType.Fixed(bounds) =>
          model.copy(
            bounds = bounds
          )

        case datatypes.BoundsType.Calculated(calculate) =>
          model

        case datatypes.BoundsType.FillWidth(height, padding) =>
          model.copy(
            bounds = Bounds(
              context.parent.bounds.width - padding.left - padding.right,
              height
            )
          )

        case datatypes.BoundsType.FillHeight(width, padding) =>
          model.copy(
            bounds = Bounds(
              width,
              context.parent.bounds.height - padding.top - padding.bottom
            )
          )

        case datatypes.BoundsType.Fill(padding) =>
          model.copy(
            bounds = Bounds(
              context.parent.bounds.width - padding.left - padding.right,
              context.parent.bounds.height - padding.top - padding.bottom
            )
          )
