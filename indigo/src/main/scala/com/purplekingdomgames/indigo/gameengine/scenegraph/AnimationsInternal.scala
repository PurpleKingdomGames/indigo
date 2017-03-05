package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.scenegraph.AnimationAction._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Point, Rectangle}

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */

// Frames
case class AnimationsInternal(spriteSheetSize: Point, cycle: CycleInternal, cycles: Map[CycleLabel, CycleInternal], actions: List[AnimationAction]) {

  private val nonEmtpyCycles: Map[CycleLabel, CycleInternal] = cycles ++ Map(cycle.label -> cycle)

  private var currentCycleLabel = cycle.label // TODO: Ok as default but needs to come from somewhere if changed.

  private def currentCycle: CycleInternal =
    nonEmtpyCycles.getOrElse(currentCycleLabel, nonEmtpyCycles.head._2)

  def currentCycleName: String = currentCycle.label.label

  def currentFrame: Frame = currentCycle.currentFrame

  def addCycle(cycle: CycleInternal) = AnimationsInternal(spriteSheetSize, cycle, nonEmtpyCycles, Nil)

  def addAction(action: AnimationAction): AnimationsInternal = this.copy(actions = action :: actions)

  def saveMemento(bindingKey: BindingKey): AnimationMemento = AnimationMemento(bindingKey, currentCycleLabel, nonEmtpyCycles.map(c => c._1 -> c._2.saveMemento))

  def applyMemento(memento: AnimationMemento): AnimationsInternal = {
    currentCycleLabel = memento.currentCycleLabel
    val updatedCycles: Map[CycleLabel, CycleInternal] = nonEmtpyCycles.map { case (l, c) =>
      l -> memento.cycleMementos.get(l).map(cm => c.applyMemento(cm)).getOrElse(c)
    }
    this.copy(cycle = updatedCycles.head._2, cycles = updatedCycles.tail)
  }

  // TODO: This is wrong!! Need to combine the anims.
  def runActions(gameTime: GameTime): AnimationsInternal = {
    actions.foldLeft(this) { (anim, action) =>
      action match {
        case ChangeCycle(label) =>
          currentCycleLabel = CycleLabel(label)
          this.copy()

        case _ =>
          val newCurrent = currentCycle.runActions(gameTime, actions)
          val cycles = nonEmtpyCycles ++ Map(newCurrent.label -> newCurrent)
          this.copy(cycle = cycles.head._2, cycles = cycles.tail)
      }
    }
  }
}

case class CycleInternal(label: CycleLabel, frame: Frame, frames: List[Frame]) {
  private val nonEmtpyFrames: List[Frame] = frame +: frames
  private val frameCount: Int = nonEmtpyFrames.length

  private var playheadPosition: Int = 0 // TODO: Ok as default but needs to come from somewhere if changed.
  private var frameDuration: Int = 100 // TODO: Ok as default but needs to come from somewhere if changed.

  def currentFrame: Frame =
    nonEmtpyFrames(playheadPosition % frameCount)

  def addFrame(frame: Frame): CycleInternal = CycleInternal(label, frame, nonEmtpyFrames)

  def saveMemento: CycleMemento = CycleMemento(playheadPosition, frameDuration)

  def applyMemento(memento: CycleMemento): CycleInternal = {
    playheadPosition = memento.playheadPosition
    frameDuration = memento.frameDuration
    this.copy()
  }

  //TODO: Obviousy wrong!
  def runActions(gameTime: GameTime, actions: List[AnimationAction]): CycleInternal = {
    actions.foldLeft(this) { (cycle, action) =>
      action match {
        case Play => cycle
        case ChangeCycle(l) => cycle
        case JumpToFirstFrame => cycle
        case JumpToLastFrame => cycle
        case JumpToFrame(number) => cycle
      }
    }
  }

}
