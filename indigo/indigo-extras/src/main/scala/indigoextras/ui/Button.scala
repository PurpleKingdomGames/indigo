package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
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
    onUp: () => Batch[GlobalEvent],
    onDown: () => Batch[GlobalEvent],
    onHoverOver: () => Batch[GlobalEvent],
    onHoverOut: () => Batch[GlobalEvent],
    onClick: () => Batch[GlobalEvent],
    onHoldDown: () => Batch[GlobalEvent]
) derives CanEqual:

  def moveBy(point: Point): Button =
    this.copy(bounds = bounds.moveBy(point))
  def moveBy(x: Int, y: Int): Button =
    moveBy(Point(x, y))

  def moveTo(point: Point): Button =
    this.copy(bounds = bounds.moveTo(point))
  def moveTo(x: Int, y: Int): Button =
    moveTo(Point(x, y))

  def resize(newSize: Size): Button =
    this.copy(bounds = bounds.resize(newSize))
  def resize(x: Int, y: Int): Button =
    resize(Size(x, y))

  def withBounds(newBounds: Rectangle): Button =
    this.copy(bounds = newBounds)

  def withDepth(newDepth: Depth): Button =
    this.copy(depth = newDepth)

  def update(mouse: Mouse): Outcome[Button] = {
    val mouseInBounds = bounds.isPointWithin(mouse.position)

    val upEvents: Batch[GlobalEvent] =
      if mouseInBounds && mouse.mouseReleased then onUp()
      else Batch.empty

    val clickEvents: Batch[GlobalEvent] =
      if mouseInBounds && mouse.mouseClicked then onClick()
      else Batch.empty

    val downEvents: Batch[GlobalEvent] =
      if mouseInBounds && mouse.mousePressed then onDown()
      else Batch.empty

    val mouseButtonEvents: Batch[GlobalEvent] =
      downEvents ++ upEvents ++ clickEvents

    state match
      // Stay in Down state
      case ButtonState.Down if mouseInBounds && mouse.isLeftDown =>
        Outcome(this).addGlobalEvents(onHoldDown() ++ mouseButtonEvents)

      // Move to Down state
      case ButtonState.Up if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState).addGlobalEvents(onHoverOver() ++ mouseButtonEvents)

      case ButtonState.Over if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState).addGlobalEvents(mouseButtonEvents)

      // Out of Down state
      case ButtonState.Down if mouseInBounds && !mouse.isLeftDown =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ mouseButtonEvents)

      case ButtonState.Down if !mouseInBounds && !mouse.isLeftDown =>
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
      case n: Shape[_]   => n.withPosition(pt).withDepth(d)
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
    withUpActions(Batch.fromSeq(actions))
  def withUpActions(actions: => Batch[GlobalEvent]): Button =
    this.copy(onUp = () => actions)

  def withDownActions(actions: GlobalEvent*): Button =
    withDownActions(Batch.fromSeq(actions))
  def withDownActions(actions: => Batch[GlobalEvent]): Button =
    this.copy(onDown = () => actions)

  def withHoverOverActions(actions: GlobalEvent*): Button =
    withHoverOverActions(Batch.fromSeq(actions))
  def withHoverOverActions(actions: => Batch[GlobalEvent]): Button =
    this.copy(onHoverOver = () => actions)

  def withHoverOutActions(actions: GlobalEvent*): Button =
    withHoverOutActions(Batch.fromSeq(actions))
  def withHoverOutActions(actions: => Batch[GlobalEvent]): Button =
    this.copy(onHoverOut = () => actions)

  def withClickActions(actions: GlobalEvent*): Button =
    withClickActions(Batch.fromSeq(actions))
  def withClickActions(actions: => Batch[GlobalEvent]): Button =
    this.copy(onClick = () => actions)

  def withHoldDownActions(actions: GlobalEvent*): Button =
    withHoldDownActions(Batch.fromSeq(actions))
  def withHoldDownActions(actions: => Batch[GlobalEvent]): Button =
    this.copy(onHoldDown = () => actions)

  def toUpState: Button =
    this.copy(state = ButtonState.Up)

  def toOverState: Button =
    this.copy(state = ButtonState.Over)

  def toDownState: Button =
    this.copy(state = ButtonState.Down)

  def withButtonState(newState: ButtonState): Button =
    this.copy(state = newState)

  def withButtonAssets(newButtonAssets: ButtonAssets): Button =
    this.copy(buttonAssets = newButtonAssets)

object Button:

  def apply(buttonAssets: ButtonAssets, bounds: Rectangle, depth: Depth): Button =
    Button(
      buttonAssets,
      bounds,
      depth,
      ButtonState.Up,
      onUp = () => Batch.empty,
      onDown = () => Batch.empty,
      onHoverOver = () => Batch.empty,
      onHoverOut = () => Batch.empty,
      onClick = () => Batch.empty,
      onHoldDown = () => Batch.empty
    )

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
