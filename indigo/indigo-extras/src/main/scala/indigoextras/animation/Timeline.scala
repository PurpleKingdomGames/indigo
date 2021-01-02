package indigoextras.animation

import indigo.shared.time.Seconds
import indigo.shared.time.GameTime
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

import scala.annotation.tailrec

final case class Timeline(markers: List[Marker], playhead: Seconds) {

  lazy val duration: Seconds =
    markers.lastOption.map(_.position).getOrElse(Seconds.zero)

  def play(gameTime: GameTime): Timeline = {
    val next = playhead + gameTime.delta
    this.copy(playhead = if (next >= duration) duration else next)
  }

  def reverse(gameTime: GameTime): Timeline = {
    val next = playhead - gameTime.delta
    this.copy(playhead = if (next < Seconds.zero) Seconds.zero else next)
  }

  def pause: Timeline =
    this

  def stop: Timeline =
    this

  def skipTo(time: Seconds): Timeline =
    this.copy(playhead = if (time < Seconds.zero) Seconds.zero else time)

  def jumpToStart: Timeline =
    this.copy(playhead = Seconds.zero)

  def jumpToEnd: Timeline =
    this.copy(playhead = duration)

  def jumpTo(label: MarkerLabel): Timeline =
    markers.find(_.label == label) match {
      case None =>
        this

      case Some(marker) =>
        this.copy(playhead = marker.position)
    }

  def jumpToNext: Timeline =
    nextMarker.map(jumpTo).getOrElse(this)

  def jumpToPrevious: Timeline =
    previousMarker.map(jumpTo).getOrElse(this)

  def nextMarker: Option[MarkerLabel] = {
    @tailrec
    def rec(remaining: List[Marker]): Option[MarkerLabel] =
      remaining match {
        case Nil =>
          None

        case m :: _ if m.position > playhead =>
          Some(m.label)

        case m :: ms =>
          rec(ms)
      }

    rec(markers)
  }

  def previousMarker: Option[MarkerLabel] = {
    @tailrec
    def rec(remaining: List[Marker], last: Option[MarkerLabel]): Option[MarkerLabel] =
      remaining match {
        case Nil =>
          last

        case m :: _ if m.position > playhead =>
          last

        case m :: ms =>
          rec(ms, Some(m.label))
      }

    rec(markers, None)
  }

  def progress: Double =
    playhead.value / duration.value

  def tweenProgress: Double = {
    @tailrec
    def rec(remaining: List[Marker], last: Seconds): Double =
      remaining match {
        case Nil =>
          if (last.value != 0)
            (playhead.value - last.value) / (duration.value - last.value)
          else
            playhead.value / duration.value

        case m :: _ if m.position >= playhead =>
          if (last.value != 0)
            (playhead.value - last.value) / (m.position.value - last.value)
          else
            playhead.value / m.position.value

        case m :: ms =>
          rec(ms, m.position)
      }

    rec(markers, Seconds.zero)
  }

  def transformDiff: TransformDiff = {
    @tailrec
    def rec(remaining: List[Marker], last: Seconds, acc: TransformDiff): TransformDiff =
      remaining match {
        case Nil =>
          val tweenAmount =
            if (last.value != 0)
              (playhead.value - last.value) / (duration.value - last.value)
            else
              playhead.value / duration.value

          acc

        case m :: _ if m.position >= playhead =>
          val tweenAmount =
            if (last.value != 0)
              (playhead.value - last.value) / (m.position.value - last.value)
            else
              playhead.value / m.position.value

          acc.tweenTo(m.diff, tweenAmount)

        case m :: ms =>
          rec(ms, m.position, acc.chooseLatest(m.diff))
      }

    rec(markers, Seconds.zero, TransformDiff.NoChange)
  }

}
object Timeline {

  def apply(markers: Marker*): Timeline =
    Timeline(markers.toList)
  def apply(markers: List[Marker]): Timeline =
    Timeline(markers, Seconds.zero)

}

final case class Marker(label: MarkerLabel, position: Seconds, diff: TransformDiff) {

  def moveTo(x: Int, y: Int): TransformDiff =
    diff.moveTo(x, y)
  def moveTo(newPosition: Point): TransformDiff =
    diff.moveTo(newPosition)

  def rotateTo(newRotation: Radians): TransformDiff =
    diff.rotateTo(newRotation)

  def scaleTo(x: Double, y: Double): TransformDiff =
    diff.scaleTo(x, y)
  def scaleTo(newScale: Vector2): TransformDiff =
    diff.scaleTo(newScale)

}
object Marker {

  def apply(label: MarkerLabel, position: Seconds): Marker =
    Marker(label, position, TransformDiff.NoChange)

}

final case class MarkerLabel(value: String) extends AnyVal

final case class TransformDiff(maybeMoveTo: Option[Point], maybeRotateTo: Option[Radians], maybeScaleTo: Option[Vector2]) {

  def chooseLatest(next: TransformDiff): TransformDiff =
    TransformDiff(
      (maybeMoveTo, next.maybeMoveTo) match {
        case (None, None) => None
        case (p, None)    => p
        case (None, p)    => p
        case (Some(_), p) => p
      },
      (maybeRotateTo, next.maybeRotateTo) match {
        case (None, None) => None
        case (p, None)    => p
        case (None, p)    => p
        case (Some(_), p) => p
      },
      (maybeScaleTo, next.maybeScaleTo) match {
        case (None, None) => None
        case (p, None)    => p
        case (None, p)    => p
        case (Some(_), p) => p
      }
    )

  def tweenTo(next: TransformDiff, amount: Double): TransformDiff =
    TransformDiff(
      (maybeMoveTo, next.maybeMoveTo) match {
        case (None, None) =>
          None

        case (p, None) =>
          p

        case (None, p) =>
          p

        case (Some(p1), Some(p2)) =>
          Some(
            Point(
              x = ((p2.x.toDouble - p1.x.toDouble) * amount).toInt,
              y = ((p2.y.toDouble - p1.y.toDouble) * amount).toInt
            )
          )
      },
      (maybeRotateTo, next.maybeRotateTo) match {
        case (None, None) =>
          None

        case (p, None) =>
          p

        case (None, p) =>
          p

        case (Some(p1), Some(p2)) =>
          Some(Radians((p2.value - p1.value) * amount))
      },
      (maybeScaleTo, next.maybeScaleTo) match {
        case (None, None) =>
          None

        case (p, None) =>
          p

        case (None, p) =>
          p

        case (Some(p1), Some(p2)) =>
          Some(
            Vector2(
              x = (p2.x - p1.x) * amount,
              y = (p2.y - p1.y) * amount
            )
          )
      }
    )

  def moveTo(x: Int, y: Int): TransformDiff =
    moveTo(Point(x, y))
  def moveTo(newPosition: Point): TransformDiff =
    this.copy(maybeMoveTo = Option(newPosition))

  def rotateTo(newRotation: Radians): TransformDiff =
    this.copy(maybeRotateTo = Option(newRotation))

  def scaleTo(x: Double, y: Double): TransformDiff =
    scaleTo(Vector2(x, y))
  def scaleTo(newScale: Vector2): TransformDiff =
    this.copy(maybeScaleTo = Option(newScale))

}
object TransformDiff {

  val NoChange: TransformDiff =
    TransformDiff(None, None, None)

}
