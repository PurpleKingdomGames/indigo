package indigo.shared.animation

import indigo.shared.collections.NonEmptyList

import indigo.shared.time.Millis

final case class Cycle(label: CycleLabel, frames: NonEmptyList[Frame], playheadPosition: Int, lastFrameAdvance: Millis) {

  def addFrame(newFrame: Frame): Cycle =
    this.copy(frames = frames :+ newFrame)

}

object Cycle {

  def apply(label: CycleLabel, frames: NonEmptyList[Frame], playheadPosition: Int, lastFrameAdvance: Millis): Cycle =
    new Cycle(label, frames, playheadPosition, lastFrameAdvance)

  def create(label: String, frames: NonEmptyList[Frame]): Cycle =
    Cycle(CycleLabel(label), frames, 0, Millis.zero)

}

final case class CycleLabel(value: String) extends AnyVal
final case class CycleMemento(playheadPosition: Int, lastFrameAdvance: Millis)
