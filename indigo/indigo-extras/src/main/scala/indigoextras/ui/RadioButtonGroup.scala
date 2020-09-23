package indigoextras.ui

import indigo.shared.EqualTo._
import indigo.shared.Outcome
import indigo.shared.datatypes.{Depth, Point, Rectangle}
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.Group

/**
  * Represents an individual option button in a radio button group. This class just containing the distinct information
  * for this option: it's position and the events fired on interacting with it. Attributes shared between options
  * are contained in the RadioButtonGroup class.
  * @param position The top-left position of this option button
  * @param onSelected The events fired when this button is selected
  * @param onUnselected The events fired when this button ceases to be selected
  * @param onHoverOver The events fired when this button is hovered over
  * @param onHoverOut The events fired when this button is no longer hovered over
  */
final case class RadioButton(
    position: Point,
    onSelected: () => List[GlobalEvent],
    onUnselected: () => List[GlobalEvent],
    onHoverOver: () => List[GlobalEvent],
    onHoverOut: () => List[GlobalEvent]
) {
  def withSelectedAction(action: () => List[GlobalEvent]): RadioButton =
    this.copy(onSelected = action)

  def withUnselectedAction(action: () => List[GlobalEvent]): RadioButton =
    this.copy(onUnselected = action)

  def withHoverOverAction(action: () => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOver = action)

  def withHoverOutAction(action: () => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOut = action)
}

object RadioButton {
  def apply(position: Point): RadioButton =
    RadioButton(position, () => Nil, () => Nil, () => Nil, () => Nil)
}

/**
  * The state of a group of mutually exclusive radio buttons.
  *
  * @param buttonAssets The graphics to use identically for all of the options: up (unselected), down (selected) and over
  * @param width The width of each option button graphic
  * @param height The height of each option button graphic
  * @param options A list individual details of the radio buttons that comprise this group
  * @param depth The depth at which to present the buttons
  * @param selected The index of the option currently selected, if any
  * @param over The index of the option currently hovered over, if any
  */
final case class RadioButtonGroup(
    buttonAssets: ButtonAssets,
    width: Int,
    height: Int,
    options: List[RadioButton],
    depth: Depth,
    selected: Option[RadioButton],
    over: Option[RadioButton]
) {
  val bounds: Map[RadioButton, Rectangle] =
    options.map(option => (option -> Rectangle(option.position.x, option.position.y, width, height))).toMap

  /**
    * Returns the button state of a given option: up (unselected), down (selected) or hovered over
    *
    * @param option The index of the option
    * @return The state of the option's button
    */
  def state(option: RadioButton): ButtonState =
    if (selected.contains(option)) ButtonState.Down
    else if (over.contains(option)) ButtonState.Over
    else ButtonState.Up

  /**
    * Update all the option buttons according to the newest state of mouse input.
    *
    * @param mouse The current mouse state
    * @return An Outcome with this radio button's new state
    */
  def update(mouse: Mouse): Outcome[RadioButtonGroup] =
    bounds.find(_._2.isPointWithin(mouse.position)).map(_._1) match {
      case Some(nowOver) =>
        val nowSelected: Option[RadioButton] = if (mouse.mousePressed) Some(nowOver) else selected
        Outcome(
          this.copy(over = Some(nowOver), selected = nowSelected),
          changeOverEvents(Some(nowOver)) ++ changeSelectedEvents(nowSelected)
        )
      case None =>
        Outcome(this.copy(over = None), changeOverEvents(None))
    }

  /**
    * Convenience method to determine which events should be fired given a change in hover state for
    * option buttons.
    *
    * @param nowOver The option that the mouse now hovers over, if any
    * @return The list of hover over/out events to fire
    */
  private def changeOverEvents(nowOver: Option[RadioButton]): List[GlobalEvent] =
    (over, nowOver) match {
      case (None, Some(newOption)) =>
        newOption.onHoverOver()
      case (Some(oldOption), None) =>
        oldOption.onHoverOut()
      case (Some(oldOption), Some(newOption)) if oldOption.position !== newOption.position =>
        oldOption.onHoverOut() ++ newOption.onHoverOver()
      case _ =>
        Nil
    }

  /**
    * Convenience method to determine which events should be fired given a change in selection state for
    * option buttons.
    *
    * @param nowSelected The option that is now selected, if any
    * @return The list of selected/unselected events to fire
    */
  private def changeSelectedEvents(nowSelected: Option[RadioButton]): List[GlobalEvent] =
    (selected, nowSelected) match {
      case (None, Some(newOption)) =>
        newOption.onSelected()
      case (Some(oldOption), None) =>
        oldOption.onUnselected()
      case (Some(oldOption), Some(newOption)) if oldOption.position !== newOption.position =>
        oldOption.onUnselected() ++ newOption.onSelected()
      case _ =>
        Nil
    }

  /**
    * Returns graphics to present the current state of the radio button.
    *
    * @return A group of scene update primitives collecting the graphics for all options
    */
  def draw: Group =
    Group(options.map { option =>
      state(option) match {
        case ButtonState.Up =>
          buttonAssets.up.moveTo(option.position).withDepth(depth)
        case ButtonState.Over =>
          buttonAssets.over.moveTo(option.position).withDepth(depth)
        case ButtonState.Down =>
          buttonAssets.down.moveTo(option.position).withDepth(depth)
      }
    })
}

object RadioButtonGroup {

  /**
    * Construct a radio button without yet specifying the events fired on selection, hover, and so on,
    * with the bounds of each option being specified by the top-left position of each plus the
    * width and height to use for them all.
    *
    * @param buttonAssets The button assets to use to present each option button
    * @param width The width of an option button, identical for all
    * @param height The height of an option button, identical for all
    * @param options The top-left position of each option button by index
    * @param depth The display depth at which to present the radio button
    * @param selected Which option is initially selected, if any
    * @return The constructed radio button
    */
  def apply(
      buttonAssets: ButtonAssets,
      width: Int,
      height: Int,
      options: List[RadioButton],
      depth: Depth,
      selected: Option[RadioButton]
  ): RadioButtonGroup =
    RadioButtonGroup(buttonAssets, width, height, options, depth, selected, None)
}
