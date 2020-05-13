package indigo.shared.animation

import indigo.shared.time.GameTime
import indigo.shared.animation.AnimationAction._
import indigo.shared.datatypes.BindingKey
import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo
import indigo.shared.AsString

import indigo.shared.EqualTo._
import indigo.shared.datatypes.Material
import indigo.shared.time.Millis

final case class Animation(
    animationKey: AnimationKey,
    material: Material,
    currentCycleLabel: CycleLabel,
    cycles: NonEmptyList[Cycle]
) {

  val frameHash: String =
    currentFrame.bounds.hash + "_" + material.hash

  def currentCycle: Cycle =
    Animation.currentCycle(this)

  def addCycle(cycle: Cycle): Animation =
    Animation.addCycle(this, cycle)

  def withAnimationKey(animationKey: AnimationKey): Animation =
    Animation.withAnimationKey(this, animationKey)

  def currentCycleName: String =
    currentCycle.label.value

  def currentFrame: Frame =
    currentCycle.currentFrame

  def saveMemento(bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  def applyMemento(memento: AnimationMemento): Animation =
    Animation.applyMemento(this, memento)

  def runActions(actions: List[AnimationAction], gameTime: GameTime): Animation =
    Animation.runActions(this, actions, gameTime)

  def changeCycle(newLabel: CycleLabel): Animation =
    this.copy(
      currentCycleLabel =
        if (cycles.exists(_.label === newLabel)) newLabel
        else currentCycleLabel
    )

}

object Animation {

  implicit def animationEqualTo(
      implicit eAK: EqualTo[AnimationKey],
      eM: EqualTo[Material],
      eCL: EqualTo[CycleLabel],
      eNelC: EqualTo[NonEmptyList[Cycle]]
  ): EqualTo[Animation] =
    EqualTo.create { (a, b) =>
      eAK.equal(a.animationKey, b.animationKey) &&
      eM.equal(a.material, b.material) &&
      eCL.equal(a.currentCycleLabel, b.currentCycleLabel) &&
      eNelC.equal(a.cycles, b.cycles)
    }

  implicit def animationAsString(
      implicit sAK: AsString[AnimationKey],
      sM: AsString[Material],
      sCL: AsString[CycleLabel],
      sNelC: AsString[NonEmptyList[Cycle]]
  ): AsString[Animation] =
    AsString.create { a =>
      s"Animation(${sAK.show(a.animationKey)}, ${sM.show(a.material)}, ${sCL.show(a.currentCycleLabel)}, ${sNelC.show(a.cycles)})"
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

  def currentCycle(animations: Animation): Cycle =
    animations.cycles.find(_.label === animations.currentCycleLabel).getOrElse(animations.cycles.head)

  def addCycle(animations: Animation, cycle: Cycle): Animation =
    animations.copy(cycles = cycle :: animations.cycles)

  def withAnimationKey(animations: Animation, animationKey: AnimationKey): Animation =
    animations.copy(animationKey = animationKey)

  def saveMemento(animations: Animation, bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, animations.currentCycleLabel, animations.currentCycle.saveMemento)

  def applyMemento(animations: Animation, memento: AnimationMemento): Animation =
    animations.copy(
      currentCycleLabel =
        if (animations.cycles.exists(_.label === memento.currentCycleLabel)) memento.currentCycleLabel
        else animations.currentCycleLabel,
      cycles = animations.cycles.map { c =>
        if (c.label === memento.currentCycleLabel) {
          c.applyMemento(memento.currentCycleMemento)
        } else c
      }
    )

  def runActions(animation: Animation, actions: List[AnimationAction], gameTime: GameTime): Animation =
    actions.foldLeft(animation) { (anim, action) =>
      action match {
        case ChangeCycle(newLabel) if animation.cycles.exists(_.label === newLabel) =>
          anim.copy(currentCycleLabel = newLabel)

        case ChangeCycle(_) =>
          anim

        case _ =>
          anim.copy(
            cycles = anim.cycles.map { c =>
              if (c.label === anim.currentCycleLabel) {
                c.runActions(gameTime, actions)
              } else c
            }
          )
      }
    }
}

final class AnimationMemento(val bindingKey: BindingKey, val currentCycleLabel: CycleLabel, val currentCycleMemento: CycleMemento)
object AnimationMemento {

  implicit val animationMementoAsString: AsString[AnimationMemento] = {
    val bk = implicitly[AsString[BindingKey]]
    val cl = implicitly[AsString[CycleLabel]]
    val cm = implicitly[AsString[CycleMemento]]

    AsString.create { m =>
      s"""AnimationMemento(bindingKey = ${bk.show(m.bindingKey)}, cycleLabel = ${cl.show(m.currentCycleLabel)}, cycleMemento = ${cm.show(m.currentCycleMemento)})"""
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val animationMementoEqualTo: EqualTo[AnimationMemento] = {
    val bk = implicitly[EqualTo[BindingKey]]
    val cl = implicitly[EqualTo[CycleLabel]]
    val cm = implicitly[EqualTo[CycleMemento]]

    EqualTo.create {
      case (a, b) =>
        bk.equal(a.bindingKey, b.bindingKey) &&
          cl.equal(a.currentCycleLabel, b.currentCycleLabel) &&
          cm.equal(a.currentCycleMemento, b.currentCycleMemento)
    }
  }

  def apply(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento): AnimationMemento =
    new AnimationMemento(bindingKey, currentCycleLabel, currentCycleMemento)
}
