package indigoextras.uicomponents

import indigo.shared.datatypes.{Depth, Rectangle}
import indigo.shared.events.GlobalEvent
import indigo.shared.events.MouseState
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneGraphNodePrimitive

final case class Button(
    buttonAssets: ButtonAssets,
    bounds: Rectangle,
    depth: Depth,
    state: ButtonState,
    onUp: () => List[GlobalEvent],
    onDown: () => List[GlobalEvent],
    onHoverOver: () => List[GlobalEvent],
    onHoverOut: () => List[GlobalEvent]
) {

  def update(mouse: MouseState): Outcome[Button] = {
    val mouseInBounds = bounds.isPointWithin(mouse.position)

    state match {
      case ButtonState.Up if mouseInBounds && !mouse.mousePressed =>
        Outcome(toOverState).addGlobalEvents(onHoverOver())

      case ButtonState.Up if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState).addGlobalEvents(onHoverOver() ++ onDown())

      case ButtonState.Over if !mouseInBounds =>
        Outcome(toUpState).addGlobalEvents(onHoverOut())

      case ButtonState.Over if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState).addGlobalEvents(onDown())

      case ButtonState.Down if mouseInBounds && mouse.mouseReleased =>
        Outcome(toOverState).addGlobalEvents(onUp())

      case ButtonState.Down if !mouseInBounds && mouse.mouseReleased =>
        Outcome(toUpState)

      case _ =>
        Outcome(this)
    }
  }

  def draw: SceneGraphNodePrimitive =
    state match {
      case ButtonState.Up =>
        buttonAssets.up.moveTo(bounds.position).withDepth(depth)

      case ButtonState.Over =>
        buttonAssets.over.moveTo(bounds.position).withDepth(depth)

      case ButtonState.Down =>
        buttonAssets.down.moveTo(bounds.position).withDepth(depth)
    }

  def withUpAction(action: => List[GlobalEvent]): Button =
    this.copy(onUp = () => action)

  def withDownAction(action: => List[GlobalEvent]): Button =
    this.copy(onDown = () => action)

  def withHoverOverAction(action: => List[GlobalEvent]): Button =
    this.copy(onHoverOver = () => action)

  def withHoverOutAction(action: => List[GlobalEvent]): Button =
    this.copy(onHoverOut = () => action)

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
      onHoverOut = () => Nil
    )

}

sealed trait ButtonState {
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

final case class ButtonAssets(up: SceneGraphNodePrimitive, over: SceneGraphNodePrimitive, down: SceneGraphNodePrimitive)
