package com.purplekingdomgames.indigo.gameengine.scenegraph

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */

// Frames
case class Animations(spriteSheetSize: Point, cycle: Cycle, cycles: Map[CycleLabel, Cycle]) {

  private val nonEmtpyCycles: Map[CycleLabel, Cycle] = cycles ++ Map(cycle.label -> cycle)

  private def currentCycle: Cycle =
    nonEmtpyCycles.getOrElse()
//    nonEmtpyCycles.find(_.current).getOrElse(nonEmtpyCycles.head)

  def currentCycleName: String = currentCycle.label.label

  def currentFrame: Frame = currentCycle.currentFrame

  def addCycle(cycle: Cycle) = Animations(spriteSheetSize, cycle, nonEmtpyCycles)

//  def nextFrame: Animations = {
//    this.copy(cycle = currentCycle.nextFrame(), cycles = nonEmtpyCycles.filterNot(_.current))
//  }

}

case class CycleLabel(label: String)

case class Cycle(label: CycleLabel, frame: Frame, frames: List[Frame], current: Boolean) {
  private val nonEmtpyFrames: List[Frame] = frame +: frames
  private val playheadPosition: Int = 0
  private val frameCount: Int = nonEmtpyFrames.length

  def currentFrame: Frame =
    nonEmtpyFrames(playheadPosition % frameCount)

  def addFrame(frame: Frame) = Cycle(label, frame, nonEmtpyFrames, current)

//  def nextFrame(): Cycle = this.copy(playheadPosition = playheadPosition + 1 % nonEmtpyFrames.length)

}

case class Frame(bounds: Rectangle, current: Boolean)
