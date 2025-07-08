package indigo.shared.input

import indigo.MouseButton
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.events.PointerEvent
import indigo.shared.events.PointerId
import indigo.shared.events.PointerType
import indigo.shared.time.Millis

import scala.annotation.nowarn

final case class PointerState(instances: Batch[Pointer]) extends ButtonInputState with PositionalInputState {
  val pointerId = PointerId.unknown
  val maybePosition =
    instances.sortBy(_.lastUpdateTime.toDouble).reverse.map(_.maybePosition).collect { case Some(p) => p }.headOption

  val buttons     = instances.flatMap(_.buttons)
  val clicks      = instances.flatMap(_.clicks)
  val downButtons = instances.flatMap(_.downButtons)
  val upButtons   = instances.flatMap(_.upButtons)

  lazy val isUpAt   = instances.filter(_.isDown == false).map(_.maybePosition).collect { case Some(p) => p }
  lazy val isDownAt = instances.filter(_.isDown).map(_.maybePosition).collect { case Some(p) => p }

  val isDown = isDownAt.isEmpty == false
  val isUp   = isUpAt.isEmpty == false

  @nowarn("msg=deprecated")
  def calculateNext(events: Batch[PointerEvent], time: Millis) =
    val newInstances = events.foldLeft(
      instances.map(_.copy(upButtons = Batch.empty, downButtons = Batch.empty, clicks = Batch.empty))
    )((instances, event) =>
      if (event.isInstanceOf[PointerEvent.Leave] || event.isInstanceOf[PointerEvent.Cancel])
        instances.filterNot(_.pointerId == event.pointerId)
      else
        val instance = PointerState.getOrCreate(event.pointerId, event.pointerType, instances)
        val newInstance = event match {
          case e: PointerEvent.Move  => instance.copy(maybePosition = Some(e.position), lastUpdateTime = time)
          case e: PointerEvent.Enter => instance.copy(maybePosition = Some(e.position), lastUpdateTime = time)
          case e: PointerEvent.Click =>
            e.button match {
              case Some(value) => instance.copy(clicks = instance.clicks :+ (value, e.position), lastUpdateTime = time)
              case None        => instance
            }
          case e: PointerEvent.Down =>
            e.button match {
              case Some(value) =>
                instance.copy(
                  downButtons = instance.downButtons :+ (value, e.position),
                  buttons = instance.buttons :+ value,
                  isDown =
                    (e.pointerType == PointerType.Mouse && value == MouseButton.LeftMouseButton) || instance.isDown,
                  lastUpdateTime = time
                )
              case None => instance.copy(isDown = true, lastUpdateTime = time)
            }
          case e: PointerEvent.Up =>
            e.button match {
              case Some(value) =>
                instance.copy(
                  upButtons = instance.upButtons :+ (value, e.position),
                  buttons = instance.buttons.filterNot(_ == value),
                  isDown =
                    if e.pointerType == PointerType.Mouse && value == MouseButton.LeftMouseButton then false
                    else instance.isDown,
                  lastUpdateTime = time
                )
              case None => instance.copy(isDown = false, lastUpdateTime = time)
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

  private def getOrCreate(id: PointerId, pointerType: PointerType, instances: Batch[Pointer]) =
    instances
      .find(p => p.pointerId == id)
      .getOrElse(
        Pointer(
          id,
          pointerType,
          None,
          false,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          Millis.zero
        )
      )

final case class Pointer(
    pointerId: PointerId,
    pointerType: PointerType,
    maybePosition: Option[Point],
    isDown: Boolean,
    buttons: Batch[MouseButton],
    clicks: Batch[(MouseButton, Point)],
    downButtons: Batch[(MouseButton, Point)],
    upButtons: Batch[(MouseButton, Point)],
    lastUpdateTime: Millis
)
