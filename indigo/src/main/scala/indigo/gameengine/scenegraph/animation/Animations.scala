package indigo.gameengine.scenegraph.animation

import indigo.time.GameTime
import indigo.gameengine.scenegraph.animation.AnimationAction._
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

import indigo.EqualTo._

final case class Animations(
    animationsKey: AnimationsKey,
    imageAssetRef: String,
    spriteSheetSize: Point,
    currentCycleLabel: CycleLabel,
    cycle: Cycle,
    cycles: Map[CycleLabel, Cycle],
    actions: List[AnimationAction]
) {

  val toMap: Map[CycleLabel, Cycle] =
    cycles ++ Map(cycle.label -> cycle)

  val frameHash: String =
    currentFrame.bounds.hash + "_" + imageAssetRef

  def currentCycle: Cycle =
    Animations.currentCycle(this)

  def addCycle(cycle: Cycle): Animations =
    Animations.addCycle(this, cycle)

  def addAction(action: AnimationAction): Animations =
    Animations.addAction(this, action)

  def withAnimationsKey(animationsKey: AnimationsKey): Animations =
    Animations.withAnimationsKey(this, animationsKey)

  def currentCycleName: String =
    currentCycle.label.value

  def currentFrame: Frame =
    currentCycle.currentFrame

  def saveMemento(bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  def applyMemento(memento: AnimationMemento): Animations =
    Animations.applyMemento(this, memento)

  def runActions(gameTime: GameTime): Animations =
    Animations.runActions(this, gameTime)

}

object Animations {

  def apply(
      animationsKey: AnimationsKey,
      imageAssetRef: String,
      spriteSheetSize: Point,
      currentCycleLabel: CycleLabel,
      cycle: Cycle,
      cycles: Map[CycleLabel, Cycle],
      actions: List[AnimationAction]
  ): Animations =
    new Animations(animationsKey, imageAssetRef, spriteSheetSize, currentCycleLabel, cycle, cycles, actions)

  def create(animationsKey: AnimationsKey, imageAssetRef: String, spriteSheetSize: Point, cycle: Cycle): Animations =
    apply(animationsKey, imageAssetRef, spriteSheetSize, cycle.label, cycle, Map.empty[CycleLabel, Cycle], Nil)

  def currentCycle(animations: Animations): Cycle =
    animations.toMap.getOrElse(animations.currentCycleLabel, animations.cycle)

  def addCycle(animations: Animations, cycle: Cycle): Animations =
    animations.copy(cycle = cycle, cycles = animations.toMap)

  def addAction(animations: Animations, action: AnimationAction): Animations =
    animations.copy(actions = animations.actions :+ action)

  def withAnimationsKey(animations: Animations, animationsKey: AnimationsKey): Animations =
    animations.copy(animationsKey = animationsKey)

  def saveMemento(animations: Animations, bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, animations.currentCycleLabel, animations.currentCycle.saveMemento)

  def applyMemento(animations: Animations, memento: AnimationMemento): Animations =
    animations.copy(
      cycle = animations.toMap
        .getOrElse(memento.currentCycleLabel, animations.cycle)
        .updatePlayheadAndLastAdvance(memento.currentCycleMemento.playheadPosition, memento.currentCycleMemento.lastFrameAdvance),
      cycles = animations.toMap
        .filter(p => p._1.value !== memento.currentCycleLabel.value)
    )

  def runActions(animations: Animations, gameTime: GameTime): Animations =
    animations.actions.foldLeft(animations) { (anim, action) =>
      action match {
        case ChangeCycle(newLabel) =>
          anim.copy(currentCycleLabel = CycleLabel(newLabel))

        case _ =>
          anim.copy(cycle = anim.currentCycle.runActions(gameTime, animations.actions))
      }
    }
}

final class AnimationMemento(val bindingKey: BindingKey, val currentCycleLabel: CycleLabel, val currentCycleMemento: CycleMemento)
object AnimationMemento {
  def apply(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento): AnimationMemento =
    new AnimationMemento(bindingKey, currentCycleLabel, currentCycleMemento)
}
