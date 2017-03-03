package com.purplekingdomgames.indigo.gameengine.scenegraph

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */

// Frames
case class Animations(spriteSheetSize: Point, cycle: Cycle, cycles: Map[CycleLabel, Cycle]) {

  private val nonEmtpyCycles: Map[CycleLabel, Cycle] = cycles ++ Map(cycle.label -> cycle)

  private var currentCycleLabel = cycle.label // TODO: Ok as default but needs to come from somewhere if changed.

  private def currentCycle: Cycle =
    nonEmtpyCycles.getOrElse(currentCycleLabel, nonEmtpyCycles.head._2)

  def currentCycleName: String = currentCycle.label.label

  def currentFrame: Frame = currentCycle.currentFrame

  def addCycle(cycle: Cycle) = Animations(spriteSheetSize, cycle, nonEmtpyCycles)

  def saveMemento(bindingKey: BindingKey): AnimationMemento = AnimationMemento(bindingKey, currentCycleLabel, nonEmtpyCycles.map(c => c._1 -> c._2.saveMemento))

  def applyMemento(memento: AnimationMemento): Animations = {
    currentCycleLabel = memento.currentCycleLabel
    val updatedCycles: Map[CycleLabel, Cycle] = nonEmtpyCycles.map { case (l, c) =>
      l -> memento.cycleMementos.get(l).map(cm => c.applyMemento(cm)).getOrElse(c)
    }
    this.copy(cycle = updatedCycles.head._2, cycles = updatedCycles.tail)
  }

}

object Animations {
  def apply(spriteSheetSize: Point, cycle: Cycle): Animations = Animations(spriteSheetSize, cycle, Map.empty[CycleLabel, Cycle])
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
    this
  }

}

object Cycle {
  def apply(label: String, frame: Frame): Cycle = Cycle(CycleLabel(label), frame, Nil)
}

case class CycleMemento(playheadPosition: Int, frameDuration: Int)

case class Frame(bounds: Rectangle)
