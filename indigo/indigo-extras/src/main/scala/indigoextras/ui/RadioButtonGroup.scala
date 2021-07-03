package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.datatypes.{Depth, Point, Rectangle}
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.TextBox
import indigo.shared.scenegraph.Group
import scala.annotation.tailrec
import indigo.shared.scenegraph.SceneNode

/** Represents an individual option button in a radio button group. This class just containing the distinct information
  * for this option: it's position and the events fired on interacting with it. Attributes shared between options are
  * contained in the RadioButtonGroup class.
  * @param position
  *   The top-left position of this option button
  * @param onSelected
  *   The events fired when this button is selected
  * @param onUnselected
  *   The events fired when this button ceases to be selected
  * @param onHoverOver
  *   The events fired when this button is hovered over
  * @param onHoverOut
  *   The events fired when this button is no longer hovered over
  * @param state
  *   The current state of the radio button i.e., selected, hover, or normal
  */
final case class RadioButton(
    position: Point,
    onSelected: () => List[GlobalEvent],
    onUnselected: () => List[GlobalEvent],
    onHoverOver: () => List[GlobalEvent],
    onHoverOut: () => List[GlobalEvent],
    hitArea: Option[Rectangle],
    buttonAssets: Option[ButtonAssets],
    state: RadioButtonState
) derives CanEqual {

  /** Events to fire when selected.
    *
    * @param actions
    *   A variables number of events to fire
    * @return
    */
  def withSelectedActions(actions: GlobalEvent*): RadioButton =
    withSelectedActions(actions.toList)

  /** Events to fire when selected.
    *
    * @param actions
    *   A list of events to fire
    * @return
    */
  def withSelectedActions(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onSelected = () => actions)

  /** Events to fire when deselected.
    *
    * @param actions
    *   A variables number of events to fire
    * @return
    */
  def withDeselectedActions(actions: GlobalEvent*): RadioButton =
    withDeselectedActions(actions.toList)

  /** Events to fire when deselected.
    *
    * @param actions
    *   A list of events to fire
    * @return
    */
  def withDeselectedActions(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onUnselected = () => actions)

  /** Events to fire on hover over.
    *
    * @param actions
    *   A variables number of events to fire
    * @return
    */
  def withHoverOverActions(actions: GlobalEvent*): RadioButton =
    withHoverOverActions(actions.toList)

  /** Events to fire on hover over.
    *
    * @param actions
    *   A list of events to fire
    * @return
    */
  def withHoverOverActions(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOver = () => actions)

  /** Events to fire on hover out.
    *
    * @param actions
    *   A variables number of events to fire
    * @return
    */
  def withHoverOutActions(actions: GlobalEvent*): RadioButton =
    withHoverOutActions(actions.toList)

  /** Events to fire on hover out.
    *
    * @param actions
    *   A list of events to fire
    * @return
    */
  def withHoverOutActions(actions: => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOut = () => actions)

  /** Give this button it's own hit area
    *
    * @param newHitArea
    *   the hit area to use for this radio button
    * @return
    *   RadioButton
    */
  def withHitArea(newHitArea: Rectangle): RadioButton =
    withHitArea(Some(newHitArea))

  /** Optionally give this button it's own hit area. A value of `None` means that the button should use the default
    * group level hit area.
    *
    * @param newHitArea
    *   the hit area to use for this radio button
    * @return
    *   RadioButton
    */
  def withHitArea(newHitArea: Option[Rectangle]): RadioButton =
    this.copy(hitArea = newHitArea)

  /** Give this radio button its own button assets
    *
    * @param newButtonAssets
    *   the assets to use when rendering this radio button
    * @return
    *   RadioButton
    */
  def withButtonAssets(newButtonAssets: ButtonAssets): RadioButton =
    withButtonAssets(Some(newButtonAssets))

  /** Optionally give this radio button its own button assets. A value of `None` means that the button should be
    * rendered with the default group level assets.
    *
    * @param newButtonAssets
    *   the assets to use when rendering this radio button
    * @return
    *   RadioButton
    */
  def withButtonAssets(newButtonAssets: Option[ButtonAssets]): RadioButton =
    this.copy(buttonAssets = newButtonAssets)

  /** Set this radio button to the selected state (may be overriden during validation)
    *
    * @return
    *   RadioButton
    */
  def selected: RadioButton =
    this.copy(state = RadioButtonState.Selected)

  /** Deselect this radio button
    *
    * @return
    *   RadioButton
    */
  def deselected: RadioButton =
    this.copy(state = RadioButtonState.Normal)

  /** Ask if this radio button is selected
    *
    * @return
    *   RadioButton
    */
  def inSelectedState: Boolean =
    state.inSelectedState

  /** Ask if this radio button has been hovered over
    *
    * @return
    *   RadioButton
    */
  def inHoverState: Boolean =
    state.inHoverState
}

object RadioButton {

  /** Build a individual radio button by specifying it's position.
    *
    * @param position
    *   on screen location of the radio button
    */
  def apply(position: Point): RadioButton =
    RadioButton(position, () => Nil, () => Nil, () => Nil, () => Nil, None, None, RadioButtonState.Normal)
}

/** A group of mutually exclusive radio buttons.
  *
  * @param buttonAssets
  *   The graphics to use identically for all of the options: up (unselected), down (selected) and over
  * @param hitArea
  *   The hit area of the radio button relative to the button's position
  * @param options
  *   A list individual radio buttons that comprise this group
  * @param depth
  *   The depth at which to present the buttons
  */
final case class RadioButtonGroup(
    buttonAssets: ButtonAssets,
    hitArea: Rectangle,
    depth: Depth,
    options: List[RadioButton]
) derives CanEqual {

  /** Specify a new hit area for the radio buttons
    *
    * @param newHitArea
    *   the new hit area
    * @return
    *   RadioButtonGroup
    */
  def withHitArea(newHitArea: Rectangle): RadioButtonGroup =
    this.copy(hitArea = newHitArea)

  /** Specify a new depth to draw the radio buttons at.
    *
    * @param newDepth
    *   the new depth to render the radio buttons at
    * @return
    *   RadioButtonGroup
    */
  def withDepth(newDepth: Depth): RadioButtonGroup =
    this.copy(depth = newDepth)

  /** Replace the radio buttons in this group
    *
    * @param radioButtons
    *   a variable number of radio buttons to use
    * @return
    *   RadioButtonGroup
    */
  def withRadioButtons(radioButtons: RadioButton*): RadioButtonGroup =
    withRadioButtons(radioButtons.toList)

  /** Replace the radio buttons in this group
    *
    * @param radioButtons
    *   a list of radio buttons to use
    * @return
    *   RadioButtonGroup
    */
  def withRadioButtons(radioButtons: List[RadioButton]): RadioButtonGroup =
    this.copy(options = selectFirstOnly(radioButtons))

  /** Append radio buttons to this group
    *
    * @param radioButtons
    *   a variable number of radio buttons to add
    * @return
    *   RadioButtonGroup
    */
  def addRadioButtons(radioButtons: RadioButton*): RadioButtonGroup =
    addRadioButtons(radioButtons.toList)

  /** Append radio buttons to this group
    *
    * @param radioButtons
    *   a list of radio buttons to add
    * @return
    *   RadioButtonGroup
    */
  def addRadioButtons(radioButtons: List[RadioButton]): RadioButtonGroup =
    this.copy(options = selectFirstOnly(options ++ radioButtons))

  private def selectFirstOnly(radioButtons: List[RadioButton]): List[RadioButton] = {
    @tailrec
    def rec(remaining: List[RadioButton], foundSelected: Boolean, acc: List[RadioButton]): List[RadioButton] =
      remaining match {
        case Nil =>
          acc.reverse

        case head :: next if head.inSelectedState && !foundSelected =>
          rec(next, true, head :: acc)

        case head :: next if head.inSelectedState && foundSelected =>
          rec(next, foundSelected, head.deselected :: acc)

        case head :: next =>
          rec(next, foundSelected, head :: acc)
      }

    rec(radioButtons, false, Nil)
  }

  /** Update all the option buttons according to the newest state of mouse input.
    *
    * @param mouse
    *   The current mouse state
    * @return
    *   An Outcome[RadioButtonGroup] with this radio button's new state
    */
  def update(mouse: Mouse): Outcome[RadioButtonGroup] = {
    val indexedOptions = options.zipWithIndex

    val selected: Option[Int] =
      indexedOptions.flatMap {
        case (o, i)
            if mouse.leftMouseIsDown && o.hitArea.getOrElse(hitArea).moveBy(o.position).isPointWithin(mouse.position) =>
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
        case (o, _)
            if !o.inSelectedState && !mouse.leftMouseIsDown && o.hitArea
              .getOrElse(hitArea)
              .moveBy(o.position)
              .isPointWithin(mouse.position) =>
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

  private def applyPositionAndDepth(sceneNode: SceneNode, pt: Point, d: Depth): SceneNode =
    sceneNode match {
      case n: Shape      => n.withPosition(pt).withDepth(d)
      case n: Graphic[_] => n.withPosition(pt).withDepth(d)
      case n: Sprite[_]  => n.withPosition(pt).withDepth(d)
      case n: Text[_]    => n.withPosition(pt).withDepth(d)
      case n: TextBox    => n.withPosition(pt).withDepth(d)
      case n: Group      => n.withPosition(pt).withDepth(d)
      case n             => n
    }

  /** Returns graphics to present the current state of the radio button.
    *
    * @return
    *   A Group of scene update primitives collecting the graphics for all options
    */
  def draw: Group =
    Group(
      options.map { option =>
        option.state.toButtonState match {
          case ButtonState.Up =>
            applyPositionAndDepth(
              option.buttonAssets.getOrElse(buttonAssets).up,
              option.position,
              depth
            )

          case ButtonState.Over =>
            applyPositionAndDepth(
              option.buttonAssets.getOrElse(buttonAssets).over,
              option.position,
              depth
            )

          case ButtonState.Down =>
            applyPositionAndDepth(
              option.buttonAssets.getOrElse(buttonAssets).down,
              option.position,
              depth
            )
        }
      }
    )
}

object RadioButtonGroup {

  /** Construct a bare bones radio button group, with no buttons in it.
    *
    * @param buttonAssets
    *   The button assets to use to present each option button
    * @param width
    *   The width of the radio button hit area, identical for all
    * @param height
    *   The height of the radio button hit area, identical for all
    * @return
    *   RadioButtonGroup
    */
  def apply(
      buttonAssets: ButtonAssets,
      width: Int,
      height: Int
  ): RadioButtonGroup =
    RadioButtonGroup(buttonAssets, Rectangle(0, 0, width, height), Depth(1), Nil)

  /** Construct a bare bones radio button group, with no buttons in it.
    *
    * @param buttonAssets
    *   The button assets to use to present each option button
    * @param hitArea
    *   The hit area of the radio button relative to the button's position
    * @return
    *   RadioButtonGroup
    */
  def apply(
      buttonAssets: ButtonAssets,
      hitArea: Rectangle
  ): RadioButtonGroup =
    RadioButtonGroup(buttonAssets, hitArea, Depth(1), Nil)

}

sealed trait RadioButtonState derives CanEqual {
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
