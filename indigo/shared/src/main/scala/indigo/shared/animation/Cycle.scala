package indigo.shared.animation

import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.time.Millis

final class Cycle(val label: CycleLabel, val frames: NonEmptyList[Frame], val playheadPosition: Int, val lastFrameAdvance: Millis) {

  def addFrame(newFrame: Frame): Cycle =
    Cycle(label, frames :+ newFrame, playheadPosition, lastFrameAdvance)

  override def toString(): String =
    s"Cycle(${label.toString()}, ${frames.toString()}, ${playheadPosition.toString()}, ${lastFrameAdvance.toString()})"

}

object Cycle {

  implicit val cycleEqualTo: EqualTo[Cycle] =
    EqualTo.create { (a, b) =>
      a.label === b.label &&
      a.frames === b.frames &&
      a.playheadPosition === b.playheadPosition &&
      a.lastFrameAdvance === b.lastFrameAdvance
    }

  def apply(label: CycleLabel, frames: NonEmptyList[Frame], playheadPosition: Int, lastFrameAdvance: Millis): Cycle =
    new Cycle(label, frames, playheadPosition, lastFrameAdvance)

  def create(label: String, frames: NonEmptyList[Frame]): Cycle =
    Cycle(CycleLabel(label), frames, 0, Millis.zero)

}

final class CycleLabel(val value: String) extends AnyVal {
  override def toString(): String =
    s"CycleLabel($value)"
}
object CycleLabel {

  implicit val cycleLabelEqualTo: EqualTo[CycleLabel] =
    EqualTo.create { (a, b) =>
      a.value === b.value
    }

  def apply(value: String): CycleLabel =
    new CycleLabel(value)
}

final class CycleMemento(val playheadPosition: Int, val lastFrameAdvance: Millis) {

  override def toString: String =
    s"CycleMemento(${playheadPosition.toString()}, ${lastFrameAdvance.toString()})"

  def ===(other: CycleMemento): Boolean =
    implicitly[EqualTo[CycleMemento]].equal(this, other)

  @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf", "org.wartremover.warts.AsInstanceOf"))
  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[CycleMemento])
      this === obj.asInstanceOf[CycleMemento]
    else false

}
object CycleMemento {

  implicit val cycleMementoEqualTo: EqualTo[CycleMemento] =
    EqualTo.create { (a, b) =>
      a.playheadPosition === b.playheadPosition && a.lastFrameAdvance === b.lastFrameAdvance
    }

  def apply(playheadPosition: Int, lastFrameAdvance: Millis): CycleMemento =
    new CycleMemento(playheadPosition, lastFrameAdvance)
}
