package com.purplekingdomgames.indigo.gameengine.scenegraph

// Frames
case class Animations(spriteSheetSize: Point, cycle: Cycle, cycles: List[Cycle]) {
  private val nonEmtpyCycles: List[Cycle] = cycle +: cycles

  def currentCycle: Cycle =
    nonEmtpyCycles.find(_.current).getOrElse(nonEmtpyCycles.head)

  def currentCycleName: String = currentCycle.label

  def currentFrame: Frame = currentCycle.currentFrame

  def addCycle(cycle: Cycle) = Animations(spriteSheetSize, cycle, nonEmtpyCycles)

//  def nextFrame: Animations = {
//    this.copy(cycle = currentCycle.nextFrame(), cycles = nonEmtpyCycles.filterNot(_.current))
//  }

}

case class Cycle(label: String, playheadPosition: Int, frame: Frame, frames: List[Frame], current: Boolean) {
  private val nonEmtpyFrames: List[Frame] = frame +: frames

  def currentFrame: Frame =
    nonEmtpyFrames.find(_.current).getOrElse(nonEmtpyFrames.head)

  def addFrame(frame: Frame) = Cycle(label, playheadPosition, frame, nonEmtpyFrames, current)

//  def nextFrame(): Cycle = this.copy(playheadPosition = playheadPosition + 1 % nonEmtpyFrames.length)

}

case class Frame(bounds: Rectangle, current: Boolean)
