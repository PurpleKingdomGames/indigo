package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Point, Rectangle}

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

}

object Animations {
  def apply(spriteSheetSize: Point, cycle: Cycle): Animations = Animations(spriteSheetSize, cycle.label, cycle, Map.empty[CycleLabel, Cycle], Nil)
}

case class Cycle(label: CycleLabel, frame: Frame, frames: List[Frame]) {
  private val nonEmtpyFrames: List[Frame] = frame +: frames

  def addFrame(frame: Frame): Cycle = Cycle(label, frame, nonEmtpyFrames)
}

object Cycle {
  def apply(label: String, frame: Frame): Cycle = Cycle(CycleLabel(label), frame, Nil)
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