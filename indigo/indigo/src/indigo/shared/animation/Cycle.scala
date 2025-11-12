package indigo.shared.animation

import indigo.shared.collections.NonEmptyBatch
import indigo.shared.time.Millis

final case class Cycle(label: CycleLabel, frames: NonEmptyBatch[Frame], playheadPosition: Int, lastFrameAdvance: Millis)
    derives CanEqual:
  def addFrame(newFrame: Frame): Cycle =
    this.copy(frames = frames :+ newFrame)

object Cycle:
  def apply(label: CycleLabel, frames: NonEmptyBatch[Frame], playheadPosition: Int, lastFrameAdvance: Millis): Cycle =
    new Cycle(label, frames, playheadPosition, lastFrameAdvance)

  def create(label: String, frames: NonEmptyBatch[Frame]): Cycle =
    Cycle(CycleLabel(label), frames, 0, Millis.zero)

opaque type CycleLabel = String
object CycleLabel:
  inline def apply(value: String): CycleLabel = value
  given CanEqual[CycleLabel, CycleLabel]      = CanEqual.derived

final case class CycleMemento(playheadPosition: Int, lastFrameAdvance: Millis) derives CanEqual
