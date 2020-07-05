package indigo.shared.animation

import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo
import indigo.shared.AsString

import indigo.shared.datatypes.Material
import indigo.shared.time.Millis

final case class Animation(
    animationKey: AnimationKey,
    material: Material,
    currentCycleLabel: CycleLabel,
    cycles: NonEmptyList[Cycle]
) {

  def addCycle(cycle: Cycle): Animation =
    this.copy(cycles = cycle :: cycles)

  def withAnimationKey(animationKey: AnimationKey): Animation =
    this.copy(animationKey = animationKey)

}

object Animation {

  implicit val animationEqualTo: EqualTo[Animation] = {
    val eAK   = implicitly[EqualTo[AnimationKey]]
    val eM    = implicitly[EqualTo[Material]]
    val eCL   = implicitly[EqualTo[CycleLabel]]
    val eNelC = implicitly[EqualTo[NonEmptyList[Cycle]]]

    EqualTo.create { (a, b) =>
      eAK.equal(a.animationKey, b.animationKey) &&
      eM.equal(a.material, b.material) &&
      eCL.equal(a.currentCycleLabel, b.currentCycleLabel) &&
      eNelC.equal(a.cycles, b.cycles)
    }
  }

  implicit val animationAsString: AsString[Animation] = {
    val sAK   = implicitly[AsString[AnimationKey]]
    val sM    = implicitly[AsString[Material]]
    val sCL   = implicitly[AsString[CycleLabel]]
    val sNelC = implicitly[AsString[NonEmptyList[Cycle]]]

    AsString.create { a =>
      s"Animation(${sAK.show(a.animationKey)}, ${sM.show(a.material)}, ${sCL.show(a.currentCycleLabel)}, ${sNelC.show(a.cycles)})"
    }
  }

  def apply(
      animationKey: AnimationKey,
      material: Material,
      frameOne: Frame,
      frames: Frame*
  ): Animation =
    Animation(
      animationKey,
      material,
      CycleLabel("default"),
      NonEmptyList(Cycle(CycleLabel("default"), NonEmptyList(frameOne, frames.toList), 0, Millis.zero))
    )

  def create(animationKey: AnimationKey, material: Material, cycle: Cycle): Animation =
    apply(animationKey, material, cycle.label, NonEmptyList(cycle))

}
