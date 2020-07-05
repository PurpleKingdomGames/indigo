package indigo.shared.animation

import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.AsString
import indigo.shared.AsString._
import indigo.shared.time.Millis

final class Cycle(val label: CycleLabel, val frames: NonEmptyList[Frame], val playheadPosition: Int, val lastFrameAdvance: Millis) {

  def addFrame(newFrame: Frame): Cycle =
    Cycle(label, frames :+ newFrame, playheadPosition, lastFrameAdvance)

}

object Cycle {

  implicit val cycleEqualTo: EqualTo[Cycle] =
    EqualTo.create { (a, b) =>
      a.label === b.label &&
      a.frames === b.frames &&
      a.playheadPosition === b.playheadPosition &&
      a.lastFrameAdvance === b.lastFrameAdvance
    }

  implicit val cycleAsString: AsString[Cycle] =
    AsString.create { c =>
      s"Cycle(${c.label.show}, ${c.frames.show}, ${c.playheadPosition.show}, ${c.lastFrameAdvance.show})"
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

  implicit val cycleLabelAsString: AsString[CycleLabel] =
    AsString.create(l => s"CycleLabel(${l.value})")

  def apply(value: String): CycleLabel =
    new CycleLabel(value)
}

final class CycleMemento(val playheadPosition: Int, val lastFrameAdvance: Millis) {

  def asString: String =
    implicitly[AsString[CycleMemento]].show(this)

  override def toString: String =
    asString

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

  implicit val cycleMementoAsString: AsString[CycleMemento] =
    AsString.create(m => s"CycleMemento(${m.playheadPosition.show}, ${m.lastFrameAdvance.show})")

  def apply(playheadPosition: Int, lastFrameAdvance: Millis): CycleMemento =
    new CycleMemento(playheadPosition, lastFrameAdvance)
}
