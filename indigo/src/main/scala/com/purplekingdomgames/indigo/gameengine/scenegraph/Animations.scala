package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.scenegraph.AnimationAction._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{BindingKey, Point, Rectangle}

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */

case class Animations(spriteSheetSize: Point, currentCycleLabel: CycleLabel, cycle: Cycle, cycles: Map[CycleLabel, Cycle], actions: List[AnimationAction]) {

  private val nonEmtpyCycles: Map[CycleLabel, Cycle] = cycles ++ Map(cycle.label -> cycle)

  def currentCycle: Cycle = nonEmtpyCycles.getOrElse(currentCycleLabel, nonEmtpyCycles.head._2)

  def addCycle(cycle: Cycle) = Animations(spriteSheetSize, currentCycleLabel, cycle, nonEmtpyCycles, Nil)

  def addAction(action: AnimationAction): Animations = this.copy(actions = action :: actions)

  private[gameengine] def currentCycleName: String = currentCycle.label.label

  private[gameengine] def currentFrame: Frame = currentCycle.currentFrame

  private[gameengine] def saveMemento(bindingKey: BindingKey): AnimationMemento = AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  private[gameengine] def applyMemento(memento: AnimationMemento): Animations = {

    val applied: Map[CycleLabel, Cycle] =
      nonEmtpyCycles ++ nonEmtpyCycles.get(memento.currentCycleLabel).map(c => Map(memento.currentCycleLabel -> c)).getOrElse(Map.empty[CycleLabel, Cycle])

    this.copy(
      currentCycleLabel = memento.currentCycleLabel,
      cycle = applied.head._2,
      cycles = applied.tail
    )
  }

  private[gameengine] def runActions(gameTime: GameTime): Animations =
    actions.foldLeft(this) { (anim, action) =>
      action match {
        case ChangeCycle(label) =>
          anim.copy(currentCycleLabel = CycleLabel(label))

        case _ =>
          anim.copy(cycle = currentCycle.runActions(gameTime, actions))
      }
    }

}

object Animations {
  def apply(spriteSheetWidth: Int, spriteSheetHeight: Int, cycle: Cycle): Animations = Animations(Point(spriteSheetWidth, spriteSheetHeight), cycle.label, cycle, Map.empty[CycleLabel, Cycle], Nil)
}

case class Cycle(label: CycleLabel, frame: Frame, frames: List[Frame], private[gameengine] val playheadPosition: Int, private[gameengine] val lastFrameAdvance: Double) {
  private val nonEmtpyFrames: List[Frame] = frame :: frames

  def addFrame(newFrame: Frame): Cycle = Cycle(label, nonEmtpyFrames.head, nonEmtpyFrames.tail ++ List(newFrame), playheadPosition, lastFrameAdvance)

  private val frameCount: Int = nonEmtpyFrames.length

  private[gameengine] def currentFrame: Frame = nonEmtpyFrames(playheadPosition % frameCount)

  private[gameengine] def saveMemento: CycleMemento =
    CycleMemento(playheadPosition, lastFrameAdvance)

  private[gameengine] def applyMemento(memento: CycleMemento): Cycle =
    this.copy(playheadPosition = memento.playheadPosition, lastFrameAdvance = memento.lastFrameAdvance)

  private[gameengine] def runActions(gameTime: GameTime, actions: List[AnimationAction]): Cycle = {
    actions.foldLeft(this) { (cycle, action) =>
      action match {
        case Play =>
          val next = Cycle.calculateNextPlayheadPosition(gameTime, playheadPosition, currentFrame.duration, frameCount, lastFrameAdvance)
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

object Cycle {
  def apply(label: String, frame: Frame): Cycle = Cycle(CycleLabel(label), frame, Nil, 0, 0)
  def apply(label: String, frame: Frame, frames: List[Frame]): Cycle = Cycle(CycleLabel(label), frame, frames, 0, 0)

  private[gameengine] def calculateNextPlayheadPosition(gameTime: GameTime, currentPosition: Int, frameDuration: Int, frameCount: Int, lastFrameAdvance: Double): NextPlayheadPositon =
    if (gameTime.running >= lastFrameAdvance + frameDuration) {
      NextPlayheadPositon((currentPosition + 1) % frameCount, gameTime.running)
    } else {
      NextPlayheadPositon(currentPosition, lastFrameAdvance)
    }

}

case class CycleLabel(label: String)

case class Frame(bounds: Rectangle, duration: Int)

object Frame {
  def apply(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), 1)
}

sealed trait AnimationAction
object AnimationAction {
  case object Play extends AnimationAction
  case class ChangeCycle(label: String) extends AnimationAction
  case object JumpToFirstFrame extends AnimationAction
  case object JumpToLastFrame extends AnimationAction
  case class JumpToFrame(number: Int) extends AnimationAction
}

private[gameengine] case class NextPlayheadPositon(position: Int, lastFrameAdvance: Double)

private[gameengine] case class AnimationMemento(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento)

private[gameengine] case class CycleMemento(playheadPosition: Int, lastFrameAdvance: Double)