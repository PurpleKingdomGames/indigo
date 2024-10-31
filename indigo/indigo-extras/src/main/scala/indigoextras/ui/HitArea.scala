package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.geometry.Polygon
import indigo.shared.geometry.Vertex
import indigo.shared.input.PointerState

final case class HitArea(
    area: Polygon.Closed,
    state: ButtonState,
    onUp: () => Batch[GlobalEvent],
    onDown: () => Batch[GlobalEvent],
    onHoverOver: () => Batch[GlobalEvent],
    onHoverOut: () => Batch[GlobalEvent],
    onClick: () => Batch[GlobalEvent],
    onHoldDown: () => Batch[GlobalEvent]
) derives CanEqual:

  def update(pointers: PointerState): Outcome[HitArea] = {
    val pointerInBounds = pointers.positions.exists(p => area.contains(Vertex.fromPoint(p)))

    val upEvents: Batch[GlobalEvent] =
      if pointerInBounds && pointers.released then onUp()
      else Batch.empty

    val clickEvents: Batch[GlobalEvent] =
      if pointerInBounds && pointers.isClicked then onClick()
      else Batch.empty

    val downEvents: Batch[GlobalEvent] =
      if pointerInBounds && pointers.pressed then onDown()
      else Batch.empty

    val pointerButtonEvents: Batch[GlobalEvent] =
      downEvents ++ upEvents ++ clickEvents

    state match
      case ButtonState.Down if pointerInBounds && pointers.isLeftDown =>
        Outcome(this).addGlobalEvents(onHoldDown() ++ pointerButtonEvents)

      case ButtonState.Up if pointerInBounds =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ pointerButtonEvents)

      case ButtonState.Over if pointerInBounds && pointers.pressed =>
        Outcome(toDownState).addGlobalEvents(pointerButtonEvents)

      case ButtonState.Down if pointerInBounds && !pointers.isLeftDown =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ pointerButtonEvents)

      case ButtonState.Over if !pointerInBounds =>
        Outcome(toUpState).addGlobalEvents(onHoverOut() ++ pointerButtonEvents)

      case _ =>
        Outcome(this).addGlobalEvents(pointerButtonEvents)
  }

  def withUpActions(actions: GlobalEvent*): HitArea =
    withUpActions(Batch.fromSeq(actions))
  def withUpActions(actions: => Batch[GlobalEvent]): HitArea =
    this.copy(onUp = () => actions)

  def withDownActions(actions: GlobalEvent*): HitArea =
    withDownActions(Batch.fromSeq(actions))
  def withDownActions(actions: => Batch[GlobalEvent]): HitArea =
    this.copy(onDown = () => actions)

  def withHoverOverActions(actions: GlobalEvent*): HitArea =
    withHoverOverActions(Batch.fromSeq(actions))
  def withHoverOverActions(actions: => Batch[GlobalEvent]): HitArea =
    this.copy(onHoverOver = () => actions)

  def withHoverOutActions(actions: GlobalEvent*): HitArea =
    withHoverOutActions(Batch.fromSeq(actions))
  def withHoverOutActions(actions: => Batch[GlobalEvent]): HitArea =
    this.copy(onHoverOut = () => actions)

  def withClickActions(actions: GlobalEvent*): HitArea =
    withClickActions(Batch.fromSeq(actions))
  def withClickActions(actions: => Batch[GlobalEvent]): HitArea =
    this.copy(onClick = () => actions)

  def withHoldDownActions(actions: GlobalEvent*): HitArea =
    withHoldDownActions(Batch.fromSeq(actions))
  def withHoldDownActions(actions: => Batch[GlobalEvent]): HitArea =
    this.copy(onHoldDown = () => actions)

  def toUpState: HitArea =
    this.copy(state = ButtonState.Up)

  def toOverState: HitArea =
    this.copy(state = ButtonState.Over)

  def toDownState: HitArea =
    this.copy(state = ButtonState.Down)

  def moveTo(x: Int, y: Int): HitArea =
    moveTo(Point(x, y))
  def moveTo(newPosition: Point): HitArea =
    this.copy(area = area.moveTo(Vertex.fromPoint(newPosition)))

  def moveBy(x: Int, y: Int): HitArea =
    moveBy(Point(x, y))
  def moveBy(positionDiff: Point): HitArea =
    this.copy(area = area.moveBy(Vertex.fromPoint(positionDiff)))

object HitArea:

  def apply(bounds: Rectangle): HitArea =
    HitArea(
      Polygon.fromRectangle(bounds),
      ButtonState.Up,
      onUp = () => Batch.empty,
      onDown = () => Batch.empty,
      onHoverOver = () => Batch.empty,
      onHoverOut = () => Batch.empty,
      onClick = () => Batch.empty,
      onHoldDown = () => Batch.empty
    )

  def apply(area: Polygon.Closed): HitArea =
    HitArea(
      area,
      ButtonState.Up,
      onUp = () => Batch.empty,
      onDown = () => Batch.empty,
      onHoverOver = () => Batch.empty,
      onHoverOut = () => Batch.empty,
      onClick = () => Batch.empty,
      onHoldDown = () => Batch.empty
    )
