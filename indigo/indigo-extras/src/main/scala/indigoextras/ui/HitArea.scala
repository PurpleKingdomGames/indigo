package indigoextras.ui

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex

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

  def update(mouse: Mouse): Outcome[HitArea] = {
    val mouseInBounds = area.contains(Vertex.fromPoint(mouse.position))

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
      case ButtonState.Down if mouseInBounds && mouse.isLeftDown =>
        Outcome(this).addGlobalEvents(onHoldDown() ++ mouseButtonEvents)

      case ButtonState.Up if mouseInBounds =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ mouseButtonEvents)

      case ButtonState.Over if mouseInBounds && mouse.mousePressed =>
        Outcome(toDownState).addGlobalEvents(mouseButtonEvents)

      case ButtonState.Down if mouseInBounds && !mouse.isLeftDown =>
        Outcome(toOverState).addGlobalEvents(onHoverOver() ++ mouseButtonEvents)

      case ButtonState.Over if !mouseInBounds =>
        Outcome(toUpState).addGlobalEvents(onHoverOut() ++ mouseButtonEvents)

      case _ =>
        Outcome(this).addGlobalEvents(mouseButtonEvents)
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
