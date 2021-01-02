package indigoextras.animation

import indigo.shared.time.Seconds
import indigo.shared.time.GameTime

import scala.annotation.tailrec

final case class Timeline(markers: List[Marker], playhead: Seconds) {

  lazy val duration: Seconds =
    markers.lastOption.map(_.position).getOrElse(Seconds.zero)

  def play(gameTime: GameTime): Timeline = {
    val next = playhead + gameTime.delta
    this.copy(playhead = if(next >= duration) duration else next)
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

  def progress: Double =
    playhead.value / duration.value

  def tweenProgress: Double =
    @tailrec
    def rec(remaining: List[Marker], last: Seconds): Double =
      remaining match {
        case Nil =>
          if(last.value != 0)
            (playhead.value - last.value) / (duration.value - last.value)
          else
            playhead.value / duration.value

        case m :: _ if m.position >= playhead =>
          if(last.value != 0)
            (playhead.value - last.value) / (m.position.value - last.value)
          else
            playhead.value / m.position.value

        case m :: ms =>
          rec(ms, m.position)
      }

    rec(markers, Seconds.zero)

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

}
object Timeline {

  def apply(markers: Marker*): Timeline =
    Timeline(markers.toList)
  def apply(markers: List[Marker]): Timeline =
    Timeline(markers, Seconds.zero)

}

final case class Marker(label: MarkerLabel, position: Seconds)

final case class MarkerLabel(value: String) extends AnyVal
