package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.scenegraph.AnimationAction._

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */

// Frames
case class Animations(spriteSheetSize: Point, cycle: Cycle, cycles: Map[CycleLabel, Cycle], actions: List[AnimationAction]) {

  private val nonEmtpyCycles: Map[CycleLabel, Cycle] = cycles ++ Map(cycle.label -> cycle)

  private var currentCycleLabel = cycle.label // TODO: Ok as default but needs to come from somewhere if changed.

  private def currentCycle: Cycle =
    nonEmtpyCycles.getOrElse(currentCycleLabel, nonEmtpyCycles.head._2)

  def currentCycleName: String = currentCycle.label.label

  def currentFrame: Frame = currentCycle.currentFrame

  def addCycle(cycle: Cycle) = Animations(spriteSheetSize, cycle, nonEmtpyCycles, Nil)

  def addAction(action: AnimationAction): Animations = this.copy(actions = action :: actions)

  def saveMemento(bindingKey: BindingKey): AnimationMemento = AnimationMemento(bindingKey, currentCycleLabel, nonEmtpyCycles.map(c => c._1 -> c._2.saveMemento))

  def applyMemento(memento: AnimationMemento): Animations = {
    currentCycleLabel = memento.currentCycleLabel
    val updatedCycles: Map[CycleLabel, Cycle] = nonEmtpyCycles.map { case (l, c) =>
      l -> memento.cycleMementos.get(l).map(cm => c.applyMemento(cm)).getOrElse(c)
    }
    this.copy(cycle = updatedCycles.head._2, cycles = updatedCycles.tail)
  }

  // TODO: This is wrong!! Need to combine the anims.
  def runActions(gameTime: GameTime): Animations = {
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

sealed trait AnimationAction
object AnimationAction {
  case object Play extends AnimationAction
  case class ChangeCycle(label: String) extends AnimationAction
  case object JumpToFirstFrame extends AnimationAction
  case object JumpToLastFrame extends AnimationAction
  case class JumpToFrame(number: Int) extends AnimationAction
}

object Animations {
  def apply(spriteSheetSize: Point, cycle: Cycle): Animations = Animations(spriteSheetSize, cycle, Map.empty[CycleLabel, Cycle], Nil)
}

case class AnimationMemento(bindingKey: BindingKey, currentCycleLabel: CycleLabel, cycleMementos: Map[CycleLabel, CycleMemento])

case class CycleLabel(label: String)

case class Cycle(label: CycleLabel, frame: Frame, frames: List[Frame]) {
  private val nonEmtpyFrames: List[Frame] = frame +: frames
  private val frameCount: Int = nonEmtpyFrames.length

  private var playheadPosition: Int = 0 // TODO: Ok as default but needs to come from somewhere if changed.
  private var frameDuration: Int = 100 // TODO: Ok as default but needs to come from somewhere if changed.

  def currentFrame: Frame =
    nonEmtpyFrames(playheadPosition % frameCount)

  def addFrame(frame: Frame) = Cycle(label, frame, nonEmtpyFrames)

  def saveMemento: CycleMemento = CycleMemento(playheadPosition, frameDuration)

  def applyMemento(memento: CycleMemento): Cycle = {
    playheadPosition = memento.playheadPosition
    frameDuration = memento.frameDuration
    this.copy()
  }

  //TODO: Obviousy wrong!
  def runActions(gameTime: GameTime, actions: List[AnimationAction]): Cycle = {
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

object Cycle {
  def apply(label: String, frame: Frame): Cycle = Cycle(CycleLabel(label), frame, Nil)
}

case class CycleMemento(playheadPosition: Int, frameDuration: Int)

case class Frame(bounds: Rectangle)
