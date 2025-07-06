package indigo.shared.input

import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.events.MouseButton
import indigo.shared.events.PenEvent
import indigo.shared.events.PointerId

final case class PenState(instances: Batch[Pen]) extends ButtonInputState with PositionalInputState:
  val primary = instances.sortBy(_.pointerId.toDouble).headOption

  val pointerId = primary.map(_.pointerId).getOrElse(PointerId.unknown)
  val maybePosition =
    instances.sortBy(_.pointerId.toDouble).map(_.maybePosition).collect { case Some(p) => p }.headOption

  val buttons     = instances.flatMap(_.buttons)
  val clicks      = instances.flatMap(_.clicks)
  val downButtons = instances.flatMap(_.downButtons)
  val upButtons   = instances.flatMap(_.upButtons)

  val isDown   = instances.exists(_.isDown)
  val isUp     = isDown == false && instances.exists(_.isUp)
  val isTap    = instances.exists(_.isTap)
  val pressure = instances.sortBy(_.pressure).headOption.map(_.pressure).getOrElse(0.0)

  def calculateNext(events: Batch[PenEvent]) =
    val newInstances = events.foldLeft(
      instances.map(
        _.copy(
          upButtons = Batch.empty,
          downButtons = Batch.empty,
          clicks = Batch.empty,
          isUp = false,
          isDown = false,
          isTap = false
        )
      )
    )((instances, event) =>
      if (event.isInstanceOf[PenEvent.Leave] || event.isInstanceOf[PenEvent.Cancel])
        instances.filterNot(_.pointerId == event.pointerId)
      else
        val instance = PenState.getOrCreate(event.pointerId, instances)
        val newPen = event match {
          case e: PenEvent.Move  => instance.copy(maybePosition = Some(e.position), pressure = e.pressure)
          case e: PenEvent.Enter => instance.copy(maybePosition = Some(e.position), pressure = e.pressure)
          case e: PenEvent.Click if e.button.isDefined =>
            instance.copy(
              maybePosition = Some(e.position),
              pressure = e.pressure,
              clicks = instance.clicks :+ (e.button.get, e.position)
            )
          case e: PenEvent.Click =>
            instance.copy(maybePosition = Some(e.position), pressure = e.pressure, isTap = true)
          case e: PenEvent.Down if e.button.isDefined =>
            instance.copy(
              isDown = true,
              maybePosition = Some(e.position),
              pressure = e.pressure,
              downButtons = instance.downButtons :+ (e.button.get, e.position),
              buttons = instance.buttons :+ e.button.get
            )
          case e: PenEvent.Down =>
            instance.copy(isDown = true, maybePosition = Some(e.position), pressure = e.pressure)
          case e: PenEvent.Up if e.button.isDefined =>
            instance.copy(
              maybePosition = Some(e.position),
              pressure = e.pressure,
              upButtons = instance.upButtons :+ (e.button.get, e.position),
              buttons = instance.buttons.filterNot(_ == e.button.get)
            )
          case e: PenEvent.Up =>
            instance.copy(isUp = true, maybePosition = Some(e.position), pressure = e.pressure)
          // We should never reach here
          case _: PenEvent.Leave => instance
          // We should never reach here
          case _: PenEvent.Cancel => instance
        }

        instances.filterNot(_.pointerId == event.pointerId) :+ newPen
    )

    this.copy(instances = newInstances)

object PenState:
  val default: PenState = PenState(Batch.empty)

  private def getOrCreate(id: PointerId, instances: Batch[Pen]) =
    instances
      .find(p => p.pointerId == id)
      .getOrElse(
        Pen(
          id,
          None,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          Batch.empty,
          isUp = false,
          isDown = false,
          isTap = false,
          pressure = 0.0
        )
      )

final case class Pen(
    pointerId: PointerId,
    maybePosition: Option[Point],
    buttons: Batch[MouseButton],
    clicks: Batch[(MouseButton, Point)],
    downButtons: Batch[(MouseButton, Point)],
    upButtons: Batch[(MouseButton, Point)],
    isUp: Boolean,
    isDown: Boolean,
    isTap: Boolean,
    pressure: Double
)
