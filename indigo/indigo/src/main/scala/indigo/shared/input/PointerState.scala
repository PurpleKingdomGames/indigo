package indigo.shared.input

import indigo.MouseButton
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerId

import scala.annotation.nowarn

final case class PointerState(instances: Batch[Pointer]) extends ButtonInputState with PositionalInputState {
  val pointerId = PointerId.unknown
  val maybePosition =
    instances.sortBy(_.pointerId.toDouble).map(_.maybePosition).collect { case Some(p) => p }.headOption

  val buttons     = instances.flatMap(_.buttons)
  val clicks      = instances.flatMap(_.clicks)
  val downButtons = instances.flatMap(_.downButtons)
  val upButtons   = instances.flatMap(_.upButtons)

  @nowarn("msg=deprecated")
  def calculateNext(events: Batch[PointerEvent]) =
    val newInstances = events.foldLeft(
      instances.map(_.copy(upButtons = Batch.empty, downButtons = Batch.empty, clicks = Batch.empty))
    )((instances, event) =>
      if (event.isInstanceOf[PointerEvent.Leave] || event.isInstanceOf[PointerEvent.Cancel])
        instances.filterNot(_.pointerId == event.pointerId)
      else
        val instance = PointerState.getOrCreate(event.pointerId, instances)
        val newInstance = event match {
          case e: PointerEvent.Move  => instance.copy(maybePosition = Some(e.position))
          case e: PointerEvent.Enter => instance.copy(maybePosition = Some(e.position))
          case e: PointerEvent.Click =>
            e.button match {
              case Some(value) => instance.copy(clicks = instance.clicks :+ (value, e.position))
              case None        => instance
            }
          case e: PointerEvent.Down =>
            e.button match {
              case Some(value) =>
                instance.copy(
                  downButtons = instance.downButtons :+ (value, e.position),
                  buttons = instance.buttons :+ value
                )
              case None => instance
            }
          case e: PointerEvent.Up =>
            e.button match {
              case Some(value) =>
                instance.copy(
                  upButtons = instance.upButtons :+ (value, e.position),
                  buttons = instance.buttons.filterNot(_ == value)
                )
              case None => instance
            }
          // We should never reach here
          case _: PointerEvent.Leave => instance
          // We should never reach here
          case _: PointerEvent.Cancel => instance
          // Deprecated
          case _: PointerEvent.Out => instance
        }

        instances.filterNot(_.pointerId == event.pointerId) :+ newInstance
    )

    this.copy(instances = newInstances)
}

object PointerState:
  val default: PointerState = PointerState(Batch.empty)

  private def getOrCreate(id: PointerId, instances: Batch[Pointer]) =
    instances
      .find(p => p.pointerId == id)
      .getOrElse(
        Pointer(
          id,
          None,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          Batch.empty
        )
      )

final case class Pointer(
    pointerId: PointerId,
    maybePosition: Option[Point],
    buttons: Batch[MouseButton],
    clicks: Batch[(MouseButton, Point)],
    downButtons: Batch[(MouseButton, Point)],
    upButtons: Batch[(MouseButton, Point)]
)
