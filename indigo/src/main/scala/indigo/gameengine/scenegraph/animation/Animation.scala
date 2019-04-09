package indigo.gameengine.scenegraph.animation

import indigo.time.GameTime
import indigo.gameengine.scenegraph.animation.AnimationAction._
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}
import indigo.collections.NonEmptyList
import indigo.shared.EqualTo
import indigo.shared.AsString

import indigo.shared.EqualTo._

final case class Animation(
    animationsKey: AnimationKey,
    imageAssetRef: String,
    spriteSheetSize: Point,
    currentCycleLabel: CycleLabel,
    cycles: NonEmptyList[Cycle],
    actions: List[AnimationAction]
) {

  val frameHash: String =
    currentFrame.bounds.hash + "_" + imageAssetRef

  def currentCycle: Cycle =
    Animation.currentCycle(this)

  def addCycle(cycle: Cycle): Animation =
    Animation.addCycle(this, cycle)

  def addAction(action: AnimationAction): Animation =
    Animation.addAction(this, action)

  def withAnimationKey(animationsKey: AnimationKey): Animation =
    Animation.withAnimationKey(this, animationsKey)

  def currentCycleName: String =
    currentCycle.label.value

  def currentFrame: Frame =
    currentCycle.currentFrame

  def saveMemento(bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  def applyMemento(memento: AnimationMemento): Animation =
    Animation.applyMemento(this, memento)

  def runActions(gameTime: GameTime): Animation =
    Animation.runActions(this, gameTime)

}

object Animation {

  implicit def animationEqualTo(
    implicit eAK: EqualTo[AnimationKey],
    eS: EqualTo[String],
    eP: EqualTo[Point],
    eCL: EqualTo[CycleLabel],
    eNelC: EqualTo[NonEmptyList[Cycle]],
    eLA: EqualTo[List[AnimationAction]]
): EqualTo[Animation] =
    EqualTo.create { (a, b) =>
      eAK.equal(a.animationsKey, b.animationsKey) &&
      eS.equal(a.imageAssetRef, b.imageAssetRef) &&
      eP.equal(a.spriteSheetSize, b.spriteSheetSize) &&
      eCL.equal(a.currentCycleLabel, b.currentCycleLabel) &&
      eNelC.equal(a.cycles, b.cycles) &&
      eLA.equal(a.actions, b.actions)
    }

  implicit def animationAsString(
      implicit sAK: AsString[AnimationKey],
      sP: AsString[Point],
      sCL: AsString[CycleLabel],
      sNelC: AsString[NonEmptyList[Cycle]],
      sLA: AsString[List[AnimationAction]]
  ): AsString[Animation] =
    AsString.create { a =>
      s"Animation(${sAK.show(a.animationsKey)}, imageAssetRef, ${sP.show(a.spriteSheetSize)}, ${sCL.show(a.currentCycleLabel)}, ${sNelC.show(a.cycles)}, ${sLA.show(a.actions)})"
    }

  def create(animationsKey: AnimationKey, imageAssetRef: String, spriteSheetSize: Point, cycle: Cycle): Animation =
    apply(animationsKey, imageAssetRef, spriteSheetSize, cycle.label, NonEmptyList(cycle), Nil)

  def currentCycle(animations: Animation): Cycle =
    animations.cycles.find(_.label === animations.currentCycleLabel).getOrElse(animations.cycles.head)

  def addCycle(animations: Animation, cycle: Cycle): Animation =
    animations.copy(cycles = cycle :: animations.cycles)

  def addAction(animations: Animation, action: AnimationAction): Animation =
    animations.copy(actions = animations.actions :+ action)

  def withAnimationKey(animations: Animation, animationsKey: AnimationKey): Animation =
    animations.copy(animationsKey = animationsKey)

  def saveMemento(animations: Animation, bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, animations.currentCycleLabel, animations.currentCycle.saveMemento)

  def applyMemento(animations: Animation, memento: AnimationMemento): Animation =
    animations.copy(
      cycles = animations.cycles.map { c =>
        if (c.label === memento.currentCycleLabel) {
          c.applyMemento(memento.currentCycleMemento)
        } else c
      }
    )

  def runActions(animations: Animation, gameTime: GameTime): Animation =
    animations.actions.foldLeft(animations) { (anim, action) =>
      action match {
        case ChangeCycle(newLabel) if animations.cycles.exists(_.label === newLabel) =>
          anim.copy(currentCycleLabel = newLabel)

        case ChangeCycle(_) =>
          anim

        case _ =>
          anim.copy(
            cycles = anim.cycles.map { c =>
              if (c.label === anim.currentCycleLabel) {
                c.runActions(gameTime, animations.actions)
              } else c
            }
          )
      }
    }
}

final class AnimationMemento(val bindingKey: BindingKey, val currentCycleLabel: CycleLabel, val currentCycleMemento: CycleMemento)
object AnimationMemento {
  def apply(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento): AnimationMemento =
    new AnimationMemento(bindingKey, currentCycleLabel, currentCycleMemento)
}
