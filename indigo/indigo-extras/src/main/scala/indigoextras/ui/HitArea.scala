package indigoextras.ui

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigo.shared.Outcome
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex

final case class HitArea(
    area: Polygon.Closed,
    state: ButtonState,
    onUp: () => List[GlobalEvent],
    onDown: () => List[GlobalEvent],
    onHoverOver: () => List[GlobalEvent],
    onHoverOut: () => List[GlobalEvent]
) derives CanEqual {

  def update(mouse: Mouse): Outcome[HitArea] = {
    val mouseInBounds = area.contains(Vertex.fromPoint(mouse.position))

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

  def withUpActions(actions: GlobalEvent*): HitArea =
    withUpActions(actions.toList)
  def withUpActions(actions: => List[GlobalEvent]): HitArea =
    this.copy(onUp = () => actions)

  def withDownActions(actions: GlobalEvent*): HitArea =
    withDownActions(actions.toList)
  def withDownActions(actions: => List[GlobalEvent]): HitArea =
    this.copy(onDown = () => actions)

  def withHoverOverActions(actions: GlobalEvent*): HitArea =
    withHoverOverActions(actions.toList)
  def withHoverOverActions(actions: => List[GlobalEvent]): HitArea =
    this.copy(onHoverOver = () => actions)

  def withHoverOutActions(actions: GlobalEvent*): HitArea =
    withHoverOutActions(actions.toList)
  def withHoverOutActions(actions: => List[GlobalEvent]): HitArea =
    this.copy(onHoverOut = () => actions)

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

}

object HitArea:

  def apply(bounds: Rectangle): HitArea =
    HitArea(
      Polygon.fromRectangle(bounds),
      ButtonState.Up,
      onUp = () => Nil,
      onDown = () => Nil,
      onHoverOver = () => Nil,
      onHoverOut = () => Nil
    )

  def apply(area: Polygon.Closed): HitArea =
    HitArea(
      area,
      ButtonState.Up,
      onUp = () => Nil,
      onDown = () => Nil,
      onHoverOver = () => Nil,
      onHoverOut = () => Nil
    )
