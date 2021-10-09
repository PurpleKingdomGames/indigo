package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.EntityNode
import indigo.shared.scenegraph.Graphic
import indigo.shared.scenegraph.Group
import indigo.shared.scenegraph.SceneNode
import indigo.shared.scenegraph.Shape
import indigo.shared.scenegraph.Sprite
import indigo.shared.scenegraph.Text
import indigo.shared.scenegraph.TextBox

final case class Button(
    buttonAssets: ButtonAssets,
    bounds: Rectangle,
    depth: Depth,
    state: ButtonState,
    onUp: () => List[GlobalEvent],
    onDown: () => List[GlobalEvent],
    onHoverOver: () => List[GlobalEvent],
    onHoverOut: () => List[GlobalEvent],
    onClick: () => List[GlobalEvent]
) derives CanEqual {

  def update(mouse: Mouse): Outcome[Button] = {
    val mouseInBounds = bounds.isPointWithin(mouse.position)

    val upEvents: List[GlobalEvent] =
      if mouseInBounds && mouse.mouseReleased then onUp()
      else Nil

    val clickEvents: List[GlobalEvent] =
      if mouseInBounds && mouse.mouseClicked then onClick()
      else Nil

    val downEvents: List[GlobalEvent] =
      if mouseInBounds && mouse.mousePressed then onDown()
      else Nil

    val mouseButtonEvents: List[GlobalEvent] =
      downEvents ++ upEvents ++ clickEvents

    state match
      // Stay in Down state
      case ButtonState.Down if mouseInBounds && mouse.leftMouseIsDown =>
        Outcome(this).addGlobalEvents(mouseButtonEvents)

      // Move to Down state
      case ButtonState.Up if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState).addGlobalEvents(onHoverOver() ++ mouseButtonEvents)

      case ButtonState.Over if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState).addGlobalEvents(mouseButtonEvents)

      // Out of Down state
      case ButtonState.Down if mouseInBounds && !mouse.leftMouseIsDown =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ mouseButtonEvents)

      case ButtonState.Down if !mouseInBounds && !mouse.leftMouseIsDown =>
        Outcome(toUpState).addGlobalEvents(onHoverOut() ++ mouseButtonEvents)

      //
      case ButtonState.Up if mouseInBounds =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ mouseButtonEvents)

      case ButtonState.Over if !mouseInBounds =>
        Outcome(toUpState).addGlobalEvents(onHoverOut() ++ mouseButtonEvents)

      case _ =>
        Outcome(this).addGlobalEvents(mouseButtonEvents)
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

  def draw: SceneNode =
    state match {
      case ButtonState.Up =>
        applyPositionAndDepth(buttonAssets.up, bounds.position, depth)

      case ButtonState.Over =>
        applyPositionAndDepth(buttonAssets.over, bounds.position, depth)

      case ButtonState.Down =>
        applyPositionAndDepth(buttonAssets.down, bounds.position, depth)
    }

  def withUpActions(actions: GlobalEvent*): Button =
    withUpActions(actions.toList)
  def withUpActions(actions: => List[GlobalEvent]): Button =
    this.copy(onUp = () => actions)

  def withDownActions(actions: GlobalEvent*): Button =
    withDownActions(actions.toList)
  def withDownActions(actions: => List[GlobalEvent]): Button =
    this.copy(onDown = () => actions)

  def withHoverOverActions(actions: GlobalEvent*): Button =
    withHoverOverActions(actions.toList)
  def withHoverOverActions(actions: => List[GlobalEvent]): Button =
    this.copy(onHoverOver = () => actions)

  def withHoverOutActions(actions: GlobalEvent*): Button =
    withHoverOutActions(actions.toList)
  def withHoverOutActions(actions: => List[GlobalEvent]): Button =
    this.copy(onHoverOut = () => actions)

  def withClickActions(actions: GlobalEvent*): Button =
    withClickActions(actions.toList)
  def withClickActions(actions: => List[GlobalEvent]): Button =
    this.copy(onClick = () => actions)

  def toUpState: Button =
    this.copy(state = ButtonState.Up)

  def toOverState: Button =
    this.copy(state = ButtonState.Over)

  def toDownState: Button =
    this.copy(state = ButtonState.Down)
}

object Button {

  def apply(buttonAssets: ButtonAssets, bounds: Rectangle, depth: Depth): Button =
    Button(
      buttonAssets,
      bounds,
      depth,
      ButtonState.Up,
      onUp = () => Nil,
      onDown = () => Nil,
      onHoverOver = () => Nil,
      onHoverOut = () => Nil,
      onClick = () => Nil
    )

}

sealed trait ButtonState derives CanEqual {
  def isUp: Boolean
  def isDown: Boolean
  def isOver: Boolean
}
object ButtonState {

  case object Up extends ButtonState {
    def isUp: Boolean   = true
    def isDown: Boolean = false
    def isOver: Boolean = false
  }
  case object Over extends ButtonState {
    def isUp: Boolean   = false
    def isDown: Boolean = false
    def isOver: Boolean = true
  }
  case object Down extends ButtonState {
    def isUp: Boolean   = false
    def isDown: Boolean = true
    def isOver: Boolean = false
  }

}

final case class ButtonAssets(
    up: SceneNode,
    over: SceneNode,
    down: SceneNode
) derives CanEqual
