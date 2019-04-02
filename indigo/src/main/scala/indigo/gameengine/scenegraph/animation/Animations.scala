package indigo.gameengine.scenegraph.animation

import indigo.time.GameTime
import indigo.gameengine.scenegraph.animation.AnimationAction._
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */
final case class Animations(
    animationsKey: AnimationsKey,
    imageAssetRef: String,
    spriteSheetSize: Point,
    currentCycleLabel: CycleLabel,
    cycle: Cycle,
    cycles: Map[CycleLabel, Cycle],
    actions: List[AnimationAction]
) {

  private val nonEmptyCycles: Map[CycleLabel, Cycle] = cycles ++ Map(cycle.label -> cycle)

  def currentCycle: Cycle = nonEmptyCycles.getOrElse(currentCycleLabel, cycle)

  def addCycle(cycle: Cycle): Animations =
    Animations(animationsKey, imageAssetRef, spriteSheetSize, currentCycleLabel, cycle, nonEmptyCycles, Nil)

  def addAction(action: AnimationAction): Animations = this.copy(actions = actions :+ action)

  def withAnimationsKey(animationsKey: AnimationsKey): Animations =
    this.copy(animationsKey = animationsKey)

  val frameHash: String = currentFrame.bounds.hash + "_" + imageAssetRef

  def currentCycleName: String = currentCycle.label.label

  def currentFrame: Frame = currentCycle.currentFrame

  def saveMemento(bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  def applyMemento(memento: AnimationMemento): Animations =
    Animations(
      animationsKey = animationsKey,
      imageAssetRef = imageAssetRef,
      spriteSheetSize = spriteSheetSize,
      currentCycleLabel = memento.currentCycleLabel,
      cycle = nonEmptyCycles
        .getOrElse(memento.currentCycleLabel, cycle)
        .copy(playheadPosition = memento.currentCycleMemento.playheadPosition, lastFrameAdvance = memento.currentCycleMemento.lastFrameAdvance),
      cycles = nonEmptyCycles.filter(p => p._1.label != memento.currentCycleLabel.label),
      actions = actions
    )

  def runActions(gameTime: GameTime): Animations =
    actions.foldLeft(this) { (anim, action) =>
      action match {
        case ChangeCycle(label) =>
          anim.copy(currentCycleLabel = CycleLabel(label))

        case _ =>
          anim.copy(cycle = anim.currentCycle.runActions(gameTime, actions))
      }
    }

}

object Animations {
  def apply(animationsKey: AnimationsKey, imageAssetRef: String, spriteSheetWidth: Int, spriteSheetHeight: Int, cycle: Cycle): Animations =
    Animations(animationsKey, imageAssetRef, Point(spriteSheetWidth, spriteSheetHeight), cycle.label, cycle, Map.empty[CycleLabel, Cycle], Nil)
}

final case class NextPlayheadPositon(position: Int, lastFrameAdvance: Long)

final case class AnimationMemento(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento)

final case class CycleMemento(playheadPosition: Int, lastFrameAdvance: Long)
