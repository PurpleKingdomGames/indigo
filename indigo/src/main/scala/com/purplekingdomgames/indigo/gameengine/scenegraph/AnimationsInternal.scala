package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.scenegraph.AnimationAction._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */

// Frames
case class AnimationsInternal(spriteSheetSize: Point, currentCycleLabel: CycleLabel, nonEmtpyCycles: Map[CycleLabel, CycleInternal], actions: List[AnimationAction]) {

  private def currentCycle: CycleInternal = nonEmtpyCycles.getOrElse(currentCycleLabel, nonEmtpyCycles.head._2)

  def currentCycleName: String = currentCycle.label.label

  def currentFrame: Frame = currentCycle.currentFrame

  def saveMemento(bindingKey: BindingKey): AnimationMemento = AnimationMemento(bindingKey, currentCycleLabel, nonEmtpyCycles.map(c => c._1 -> c._2.saveMemento))

  def applyMemento(memento: AnimationMemento): AnimationsInternal =
    this.copy(
      currentCycleLabel = memento.currentCycleLabel,
      nonEmtpyCycles = nonEmtpyCycles.map { case (l, c) =>
        l -> memento.cycleMementos.get(l).map(cm => c.applyMemento(cm)).getOrElse(c)
      }
    )

  def runActions(gameTime: GameTime): AnimationsInternal =
    actions.foldLeft(this) { (anim, action) =>
      action match {
        case ChangeCycle(label) =>
          anim.copy(currentCycleLabel = CycleLabel(label))

        case _ =>
          val newCurrent = currentCycle.runActions(gameTime, actions)
          anim.copy(nonEmtpyCycles = nonEmtpyCycles ++ Map(newCurrent.label -> newCurrent))
      }
    }
}

case class CycleInternal(label: CycleLabel, nonEmtpyFrames: List[Frame], playheadPosition: Int, lastFrameAdvance: Double) {
  private val frameCount: Int = nonEmtpyFrames.length

  def currentFrame: Frame = nonEmtpyFrames(playheadPosition % frameCount)

  def saveMemento: CycleMemento = CycleMemento(playheadPosition, lastFrameAdvance)

  def applyMemento(memento: CycleMemento): CycleInternal =
    this.copy(playheadPosition = memento.playheadPosition)

  def runActions(gameTime: GameTime, actions: List[AnimationAction]): CycleInternal = {
    actions.foldLeft(this) { (cycle, action) =>
      action match {
        case Play =>
          val next = CycleInternal.calculateNextPlayheadPosition(gameTime, playheadPosition, currentFrame.duration, frameCount, lastFrameAdvance)
          cycle.copy(
            playheadPosition = next.position,
            lastFrameAdvance = next.lastFrameAdvance
          )

        case ChangeCycle(_) => cycle // No op, done at animation level.
        case JumpToFirstFrame =>
          cycle.copy(playheadPosition = 0)

        case JumpToLastFrame =>
          cycle.copy(playheadPosition = frameCount - 1)

        case JumpToFrame(number) =>
          if(number > frameCount - 1) cycle.copy(playheadPosition = frameCount - 1)
          else cycle.copy(playheadPosition = number)
      }
    }
  }

}

object CycleInternal {

  def calculateNextPlayheadPosition(gameTime: GameTime, currentPosition: Int, frameDuration: Int, frameCount: Int, lastFrameAdvance: Double): NextPlayheadPositon =
    if(gameTime.running >= lastFrameAdvance + frameDuration) {
      NextPlayheadPositon(
        position = (currentPosition + 1) % frameCount,
        lastFrameAdvance = gameTime.running
      )
    } else {
      NextPlayheadPositon(
        position = currentPosition,
        lastFrameAdvance = lastFrameAdvance
      )
    }

}

case class NextPlayheadPositon(position: Int, lastFrameAdvance: Double)

case class AnimationMemento(bindingKey: BindingKey, currentCycleLabel: CycleLabel, cycleMementos: Map[CycleLabel, CycleMemento])

case class CycleMemento(playheadPosition: Int, lastFrameAdvance: Double)