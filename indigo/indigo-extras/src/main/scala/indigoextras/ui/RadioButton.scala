package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.datatypes.{Depth, Point, Rectangle}
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.Group

/**
  * Structure for presenting a radio button.
  *
  * @param buttonAssets The graphics to use identically for all of the options: up (unselected), down (selected) and over
  * @param bounds A list of the rectangle locations of each option button by index
  * @param depth The depth at which to present this button
  * @param selected The index of the option currently selected, if any
  * @param over The index of the option currently hovered over, if any
  * @param onUnselected Events to fire when an option becomes unselected, mapping from the index of the option deselected
  * @param onSelected Events to fire when an option becomes selected, mapping from the index of the option selected
  * @param onHoverOver Events to fire when an option is hovered over, mapping from the index of the option hovered over
  * @param onHoverOut Events to fire when an option is no longer hovered over, mapping from the index of the option no longer hovered over
  */
final case class RadioButton(
    buttonAssets: ButtonAssets,
    bounds: List[Rectangle],
    depth: Depth,
    selected: Option[Int],
    over: Option[Int],
    onUnselected: Int => List[GlobalEvent],
    onSelected: Int => List[GlobalEvent],
    onHoverOver: Int => List[GlobalEvent],
    onHoverOut: Int => List[GlobalEvent]
) {

  /**
    * Returns the button state of a given option: up (unselected), down (selected) or hovered over
    *
    * @param option The index of the option
    * @return The state of the option's button
    */
  def state(option: Int): ButtonState =
    if (selected.contains(option)) ButtonState.Down
    else if (over.contains(option)) ButtonState.Over
    else ButtonState.Up

  /**
    * Update all the option buttons according to the newest state of mouse input.
    *
    * @param mouse The current mouse state
    * @return An Outcome with this radio button's new state
    */
  def update(mouse: Mouse): Outcome[RadioButton] = {
    val nowOver: Int = bounds.indexWhere(_.isPointWithin(mouse.position))

    if (nowOver >= 0) {
      val nowSelected: Option[Int] = if (mouse.mousePressed) Some(nowOver) else selected
      Outcome(this.copy(over = Some(nowOver), selected = nowSelected))
        .addGlobalEvents(changeOverEvents(Some(nowOver)))
        .addGlobalEvents(changeSelectedEvents(nowSelected))
    } else
      Outcome(this.copy(over = None)).addGlobalEvents(changeOverEvents(None))
  }

  /**
    * Convenience method to determine which events should be fired given a change in hover state for
    * option buttons.
    *
    * @param nowOver The option that the mouse now hovers over, if any
    * @return The list of hover over/out events to fire
    */
  private def changeOverEvents(nowOver: Option[Int]): List[GlobalEvent] =
    (over, nowOver) match {
      case (None, Some(newOption)) =>
        onHoverOver(newOption)
      case (Some(oldOption), None) =>
        onHoverOut(oldOption)
      case (Some(oldOption), Some(newOption)) if oldOption != newOption =>
        onHoverOut(oldOption) ++ onHoverOver(newOption)
      case _ =>
        List.empty
    }

  /**
    * Convenience method to determine which events should be fired given a change in selection state for
    * option buttons.
    *
    * @param nowSelected The option that is now selected, if any
    * @return The list of selected/unselected events to fire
    */
  private def changeSelectedEvents(nowSelected: Option[Int]): List[GlobalEvent] =
    (selected, nowSelected) match {
      case (None, Some(newOption)) =>
        onSelected(newOption)
      case (Some(oldOption), None) =>
        onUnselected(oldOption)
      case (Some(oldOption), Some(newOption)) if oldOption != newOption =>
        onUnselected(oldOption) ++ onSelected(newOption)
      case _ =>
        List.empty
    }

  /**
    * Returns graphics to present the current state of the radio button.
    *
    * @return A group of scene update primitives collecting the graphics for all options
    */
  def draw: Group =
    Group(bounds.zipWithIndex.map {
      case (optionBounds, optionIndex) =>
        state(optionIndex) match {
          case ButtonState.Up =>
            buttonAssets.up.moveTo(optionBounds.position).withDepth(depth)

          case ButtonState.Over =>
            buttonAssets.over.moveTo(optionBounds.position).withDepth(depth)

          case ButtonState.Down =>
            buttonAssets.down.moveTo(optionBounds.position).withDepth(depth)
        }
    })

  /**
    * In constructing this radio button, set the events to fire on an option losing selection.
    *
    * @param action A mapping from an option index to the list of events to fire
    * @return The updated radio button construct
    */
  def withUnselectedAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onUnselected = action)

  /**
    * In constructing this radio button, set the events to fire on an option gaining selection.
    *
    *  @param action A mapping from an option index to the list of events to fire
    * @return The updated radio button construct
    */
  def withSelectedAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onSelected = action)

  /**
    * In constructing this radio button, set the events to fire on an option being hovered over.
    *
    * @param action A mapping from an option index to the list of events to fire.
    * @return The updated radio button construct
    */
  def withHoverOverAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOver = action)

  /**
    * In constructing this radio button, set the events to fire on an option losing hover.
    *
    * @param action A mapping from an option index to the list of events to fire
    * @return The updated radio button construct
    */
  def withHoverOutAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOut = action)
}

object RadioButton {

  /**
    * Construct a radio button without yet specifying the events fired on selection, hover, and so on,
    * with the bounds of each option being specified by the rectangle they occupy.
    *
    * @param buttonAssets The button assets to use to present each option button
    * @param bounds The rectangular bounds of each option button by index
    * @param depth The display depth at which to present the radio button
    * @param selected Which option is initially selected, if any
    * @return The constructed radio button
    */
  def apply(buttonAssets: ButtonAssets, bounds: List[Rectangle], depth: Depth, selected: Option[Int]): RadioButton =
    RadioButton(
      buttonAssets = buttonAssets,
      bounds = bounds,
      depth = depth,
      selected = selected,
      over = None,
      onUnselected = _ => Nil,
      onSelected = _ => Nil,
      onHoverOut = _ => Nil,
      onHoverOver = _ => Nil
    )

  /**
    * Construct a radio button without yet specifying the events fired on selection, hover, and so on,
    * with the bounds of each option being specified by the top-left position of each plus the
    * width and height to use for them all.
    *
    * @param buttonAssets The button assets to use to present each option button
    * @param positions The top-left position of each option button by index
    * @param width The width of an option button, identical for all
    * @param height The height of an option button, identical for all
    * @param depth The display depth at which to present the radio button
    * @param selected Which option is initially selected, if any
    * @return The constructed radio button
    */
  def apply(
      buttonAssets: ButtonAssets,
      positions: List[Point],
      width: Int,
      height: Int,
      depth: Depth,
      selected: Option[Int]
  ): RadioButton =
    apply(buttonAssets, positions.map(pos => Rectangle(pos.x, pos.y, width, height)), depth, selected)
}
