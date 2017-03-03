package com.purplekingdomgames.indigo.gameengine.scenegraph

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */

// Frames
case class Animations(spriteSheetSize: Point, cycle: Cycle, cycles: Map[CycleLabel, Cycle]) {

  private val currentCycleLabel = cycle.label // TODO: Ok is default but needs to come from somewhere if changed.

  private val nonEmtpyCycles: Map[CycleLabel, Cycle] = cycles ++ Map(cycle.label -> cycle)

  private def currentCycle: Cycle =
    nonEmtpyCycles.getOrElse(currentCycleLabel, nonEmtpyCycles.head._2)

  def currentCycleName: String = currentCycle.label.label

  def currentFrame: Frame = currentCycle.currentFrame

  def addCycle(cycle: Cycle) = Animations(spriteSheetSize, cycle, nonEmtpyCycles)

}

object Animations {
  def apply(spriteSheetSize: Point, cycle: Cycle): Animations = Animations(spriteSheetSize, cycle, Map.empty[CycleLabel, Cycle])
}

case class CycleLabel(label: String)

case class Cycle(label: CycleLabel, frame: Frame, frames: List[Frame]) {
  private val nonEmtpyFrames: List[Frame] = frame +: frames
  private val playheadPosition: Int = 0 // TODO: Ok is default but needs to come from somewhere if changed.
  private val frameCount: Int = nonEmtpyFrames.length

  def currentFrame: Frame =
    nonEmtpyFrames(playheadPosition % frameCount)

  def addFrame(frame: Frame) = Cycle(label, frame, nonEmtpyFrames)

}

object Cycle {
  def apply(label: String, frame: Frame): Cycle = Cycle(CycleLabel(label), frame, Nil)
}

case class Frame(bounds: Rectangle)
