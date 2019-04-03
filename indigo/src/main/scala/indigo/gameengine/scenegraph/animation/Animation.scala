package indigo.gameengine.scenegraph.animation

import indigo.time.GameTime
import indigo.gameengine.scenegraph.animation.AnimationAction._
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

import indigo.shared.EqualTo._

final case class Animation(
    animationsKey: AnimationKey,
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

  def apply(
      animationsKey: AnimationKey,
      imageAssetRef: String,
      spriteSheetSize: Point,
      currentCycleLabel: CycleLabel,
      cycle: Cycle,
      cycles: Map[CycleLabel, Cycle],
      actions: List[AnimationAction]
  ): Animation =
    new Animation(animationsKey, imageAssetRef, spriteSheetSize, currentCycleLabel, cycle, cycles, actions)

  def create(animationsKey: AnimationKey, imageAssetRef: String, spriteSheetSize: Point, cycle: Cycle): Animation =
    apply(animationsKey, imageAssetRef, spriteSheetSize, cycle.label, cycle, Map.empty[CycleLabel, Cycle], Nil)

  def currentCycle(animations: Animation): Cycle =
    animations.toMap.getOrElse(animations.currentCycleLabel, animations.cycle)

  def addCycle(animations: Animation, cycle: Cycle): Animation =
    animations.copy(cycle = cycle, cycles = animations.toMap)

  def addAction(animations: Animation, action: AnimationAction): Animation =
    animations.copy(actions = animations.actions :+ action)

  def withAnimationKey(animations: Animation, animationsKey: AnimationKey): Animation =
    animations.copy(animationsKey = animationsKey)

  def saveMemento(animations: Animation, bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, animations.currentCycleLabel, animations.currentCycle.saveMemento)

  def applyMemento(animations: Animation, memento: AnimationMemento): Animation =
    animations.copy(
      cycle = animations.toMap
        .getOrElse(memento.currentCycleLabel, animations.cycle)
        .updatePlayheadAndLastAdvance(memento.currentCycleMemento.playheadPosition, memento.currentCycleMemento.lastFrameAdvance),
      cycles = animations.toMap
        .filter(p => p._1.value !== memento.currentCycleLabel.value)
    )

  def runActions(animations: Animation, gameTime: GameTime): Animation =
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
