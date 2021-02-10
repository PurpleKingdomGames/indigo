package indigo.shared.animation

import indigo.shared.collections.NonEmptyList

import indigo.shared.time.Millis

final case class Animation(
    animationKey: AnimationKey,
    currentCycleLabel: CycleLabel,
    cycles: NonEmptyList[Cycle]
) {

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
      NonEmptyList(Cycle(CycleLabel("default"), NonEmptyList(frameOne, frames.toList), 0, Millis.zero))
    )

  def create(animationKey: AnimationKey, cycle: Cycle): Animation =
    apply(animationKey, cycle.label, NonEmptyList(cycle))

}
