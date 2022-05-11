package indigo.shared.animation

import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.time.Millis

final case class Animation(
    animationKey: AnimationKey,
    currentCycleLabel: CycleLabel,
    cycles: NonEmptyBatch[Cycle]
) derives CanEqual {

  def addCycle(cycle: Cycle): Animation =
    this.copy(cycles = cycle :: cycles)

  def withAnimationKey(animationKey: AnimationKey): Animation =
    this.copy(animationKey = animationKey)

}

object Animation {

  def apply(
      animationKey: AnimationKey,
      frameOne: Frame,
      frames: Frame*
  ): Animation =
    Animation(
      animationKey,
      CycleLabel("default"),
      NonEmptyBatch(Cycle(CycleLabel("default"), NonEmptyBatch(frameOne, Batch.fromSeq(frames)), 0, Millis.zero))
    )

  def create(animationKey: AnimationKey, cycle: Cycle): Animation =
    apply(animationKey, cycle.label, NonEmptyBatch(cycle))

}
