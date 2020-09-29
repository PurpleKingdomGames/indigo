package indigoextras.ui

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
  * @param state The current state of the radio button i.e., selected, hover, or normal
  */
final case class RadioButton(
    position: Point,
    onSelected: () => List[GlobalEvent],
    onUnselected: () => List[GlobalEvent],
    onHoverOver: () => List[GlobalEvent],
    onHoverOut: () => List[GlobalEvent],
    state: RadioButtonState
) {
  def withSelectedAction(actions: GlobalEvent*): RadioButton =
    withSelectedAction(actions.toList)
  def withSelectedAction(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onSelected = () => actions)

  def withUnselectedAction(actions: GlobalEvent*): RadioButton =
    withUnselectedAction(actions.toList)
  def withUnselectedAction(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onUnselected = () => actions)

  def withHoverOverAction(actions: GlobalEvent*): RadioButton =
    withHoverOverAction(actions.toList)
  def withHoverOverAction(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOver = () => actions)

  def withHoverOutAction(actions: GlobalEvent*): RadioButton =
    withHoverOutAction(actions.toList)
  def withHoverOutAction(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOut = () => actions)

  def selected: RadioButton =
    this.copy(state = RadioButtonState.Selected)

  def inSelectedState: Boolean =
    state.inSelectedState

  def inHoverState: Boolean =
    state.inHoverState
}

object RadioButton {

  /**
    * Build a individual radio button by specifying it's position.
    *
    * @param position on screen location of the radio button
    */
  def apply(position: Point): RadioButton =
    RadioButton(position, () => Nil, () => Nil, () => Nil, () => Nil, RadioButtonState.Normal)
}

/**
  * The state of a group of mutually exclusive radio buttons.
  *
  * @param buttonAssets The graphics to use identically for all of the options: up (unselected), down (selected) and over
  * @param width The width of each option button graphic
  * @param height The height of each option button graphic
  * @param options A list individual radio buttons that comprise this group
  * @param depth The depth at which to present the buttons
  */
final case class RadioButtonGroup(
    buttonAssets: ButtonAssets,
    size: Point,
    options: List[RadioButton],
    depth: Depth
) {

  def withSize(newSize: Point): RadioButtonGroup =
    this.copy(size = newSize)

  def withDepth(newDepth: Depth): RadioButtonGroup =
    this.copy(depth = newDepth)

  def addRadioButton(radioButton: RadioButton): RadioButtonGroup =
    this.copy(options = options :+ radioButton)

  /**
    * Update all the option buttons according to the newest state of mouse input.
    *
    * @param mouse The current mouse state
    * @return An Outcome with this radio button's new state
    */
  def update(mouse: Mouse): Outcome[RadioButtonGroup] = {
    val indexedOptions = options.zipWithIndex

    val selected: Option[Int] =
      indexedOptions.flatMap {
        case (o, i) if mouse.leftMouseIsDown && Rectangle(o.position, size).isPointWithin(mouse.position) =>
          List(i)

        case _ =>
          Nil
      }.headOption

    val updatedOptions: List[Outcome[RadioButton]] =
      indexedOptions.map {
        // Selected already
        case (o, _) if o.inSelectedState && selected.isEmpty =>
          Outcome(o)

        // Selected already after some mouse selection
        case (o, i) if o.inSelectedState && selected.isDefined && selected.contains(i) =>
          Outcome(o)

        // Not selected, but should be due to user interaction
        case (o, i) if !o.inSelectedState && selected.isDefined && selected.contains(i) =>
          Outcome(o.copy(state = RadioButtonState.Selected), o.onSelected())

        // Selected, but shouldn't be, user selected something else
        case (o, i) if o.inSelectedState && selected.isDefined && !selected.contains(i) =>
          Outcome(o.copy(state = RadioButtonState.Normal), o.onUnselected())

        // Not selected, no mouse click, mouse within, should be in hover state.
        case (o, _) if !o.inSelectedState && !mouse.leftMouseIsDown && Rectangle(o.position, size).isPointWithin(mouse.position) =>
          Outcome(o.copy(state = RadioButtonState.Hover), o.onHoverOver())

        // Hovered, but mouse outside so revert to normal
        case (o, _) if o.inHoverState =>
          Outcome(o.copy(state = RadioButtonState.Normal), o.onHoverOut())

        // Default
        case (o, _) =>
          Outcome(o)
      }

    updatedOptions.sequence.map(opts => this.copy(options = opts))
  }

  /**
    * Returns graphics to present the current state of the radio button.
    *
    * @return A group of scene update primitives collecting the graphics for all options
    */
  def draw: Group =
    Group(options.map { option =>
      option.state.toButtonState match {
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
    * Construct a radio button group without yet specifying the events fired on selection, hover, and so on,
    * with the bounds of each option being specified by the top-left position of each plus the
    * width and height to use for them all.
    *
    * @param buttonAssets The button assets to use to present each option button
    * @param width The width of the radio button hit area, identical for all
    * @param height The height of the radio button hit area, identical for all
    * @param options The top-left position of each option button by index
    * @param depth The display depth at which to present the radio button
    * @return The constructed radio button
    */
  def apply(
      buttonAssets: ButtonAssets,
      width: Int,
      height: Int,
      options: List[RadioButton],
      depth: Depth
  ): RadioButtonGroup =
    RadioButtonGroup(buttonAssets, Point(width, height), options, depth /*, selected, None*/ )

  /**
    * Construct a bare bones radio button group, with no buttons in it.
    *
    * @param buttonAssets The button assets to use to present each option button
    * @param width The width of the radio button hit area, identical for all
    * @param height The height of the radio button hit area, identical for all
    * @return
    */
  def apply(
      buttonAssets: ButtonAssets,
      width: Int,
      height: Int
  ): RadioButtonGroup =
    RadioButtonGroup(buttonAssets, Point(width, height), Nil, Depth(1) /*, selected, None*/ )
}

sealed trait RadioButtonState {
  def toButtonState: ButtonState =
    this match {
      case RadioButtonState.Selected => ButtonState.Down
      case RadioButtonState.Hover    => ButtonState.Over
      case RadioButtonState.Normal   => ButtonState.Up
    }

  def inSelectedState: Boolean
  def inHoverState: Boolean
}
object RadioButtonState {
  case object Selected extends RadioButtonState {
    val inSelectedState: Boolean = true
    val inHoverState: Boolean    = false
  }
  case object Hover extends RadioButtonState {
    val inSelectedState: Boolean = false
    val inHoverState: Boolean    = true
  }
  case object Normal extends RadioButtonState {
    val inSelectedState: Boolean = false
    val inHoverState: Boolean    = false
  }
}
