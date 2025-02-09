package indigoextras.ui.simple

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.events.GlobalEvent
import indigo.shared.input.PointerState
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

  def update(pointer: PointerState): Outcome[Button] = {
    val pointerInBounds = pointer.positions.exists(p => bounds.isPointWithin(p))

    val upEvents: Batch[GlobalEvent] =
      if pointerInBounds && pointer.released then onUp()
      else Batch.empty

    val clickEvents: Batch[GlobalEvent] =
      if pointerInBounds && pointer.isClicked then onClick()
      else Batch.empty

    val downEvents: Batch[GlobalEvent] =
      if pointerInBounds && pointer.pressed then onDown()
      else Batch.empty

    val pointerButtonEvents: Batch[GlobalEvent] =
      downEvents ++ upEvents ++ clickEvents

    state match
      // Stay in Down state
      case ButtonState.Down if pointerInBounds && pointer.isLeftDown =>
        Outcome(this).addGlobalEvents(onHoldDown() ++ pointerButtonEvents)

      // Move to Down state
      case ButtonState.Up if pointerInBounds && pointer.pressed =>
        Outcome(toDownState).addGlobalEvents(onHoverOver() ++ pointerButtonEvents)

      case ButtonState.Over if pointerInBounds && pointer.pressed =>
        Outcome(toDownState).addGlobalEvents(pointerButtonEvents)

      // Out of Down state
      case ButtonState.Down if pointerInBounds && !pointer.isLeftDown =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ pointerButtonEvents)

      case ButtonState.Down if !pointerInBounds && !pointer.isLeftDown =>
        Outcome(toUpState).addGlobalEvents(onHoverOut() ++ pointerButtonEvents)

      //
      case ButtonState.Up if pointerInBounds =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ pointerButtonEvents)

      case ButtonState.Over if !pointerInBounds =>
        Outcome(toUpState).addGlobalEvents(onHoverOut() ++ pointerButtonEvents)

      case _ =>
        Outcome(this).addGlobalEvents(pointerButtonEvents)
  }

  private def applyPosition(sceneNode: SceneNode, pt: Point): SceneNode =
    sceneNode match {
      case n: Shape[_]   => n.withPosition(pt)
      case n: Graphic[_] => n.withPosition(pt)
      case n: Sprite[_]  => n.withPosition(pt)
      case n: Text[_]    => n.withPosition(pt)
      case n: TextBox    => n.withPosition(pt)
      case n: Group      => n.withPosition(pt)
      case n             => n
    }

  def draw: SceneNode =
    state match {
      case ButtonState.Up =>
        applyPosition(buttonAssets.up, bounds.position)

      case ButtonState.Over =>
        applyPosition(buttonAssets.over, bounds.position)

      case ButtonState.Down =>
        applyPosition(buttonAssets.down, bounds.position)
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

  def apply(buttonAssets: ButtonAssets, bounds: Rectangle): Button =
    Button(
      buttonAssets,
      bounds,
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
