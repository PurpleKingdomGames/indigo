package indigo.gameengine.scenegraph.animation

import indigo.GameTime
import indigo.AnimationAction._
import indigo.collections.NonEmptyList

final class Cycle(val label: CycleLabel, val frames: NonEmptyList[Frame], val playheadPosition: Int, val lastFrameAdvance: Long) {

  def addFrame(newFrame: Frame): Cycle =
    Cycle(label, frames :+ newFrame, playheadPosition, lastFrameAdvance)

  val frameCount: Int =
    frames.length

  def currentFrame: Frame =
    Cycle.currentFrame(this)

  def saveMemento: CycleMemento =
    Cycle.saveMemento(this)

  def applyMemento(memento: CycleMemento): Cycle =
    Cycle.applyMemento(this, memento)

  def runActions(gameTime: GameTime, actions: List[AnimationAction]): Cycle =
    Cycle.runActions(this, gameTime, actions)

  def updatePlayheadAndLastAdvance(playheadPosition: Int, lastFrameAdvance: Long): Cycle =
    Cycle.updatePlayheadAndLastAdvance(this, playheadPosition, lastFrameAdvance)

}

object Cycle {

  def apply(label: CycleLabel, frames: NonEmptyList[Frame], playheadPosition: Int, lastFrameAdvance: Long): Cycle =
    new Cycle(label, frames, playheadPosition, lastFrameAdvance)

  // def apply(label: String, frame: Frame): Cycle                = Cycle(CycleLabel(label), NonEmptyList(frame), 0, 0)
  def create(label: String, frames: NonEmptyList[Frame]): Cycle =
    Cycle(CycleLabel(label), frames, 0, 0)

  def calculateNextPlayheadPosition(gameTime: GameTime, currentPosition: Int, frameDuration: Int, frameCount: Int, lastFrameAdvance: Long): CycleMemento =
    if (gameTime.running.value >= lastFrameAdvance + frameDuration)
      CycleMemento((currentPosition + 1) % frameCount, gameTime.running.value)
    else
      CycleMemento(currentPosition, lastFrameAdvance)

  def currentFrame(cycle: Cycle): Frame =
    cycle.frames.toList(cycle.playheadPosition % cycle.frameCount)

  def saveMemento(cycle: Cycle): CycleMemento =
    new CycleMemento(cycle.playheadPosition, cycle.lastFrameAdvance)

  def updatePlayheadAndLastAdvance(cycle: Cycle, playheadPosition: Int, lastFrameAdvance: Long): Cycle =
    Cycle(cycle.label, cycle.frames, playheadPosition, lastFrameAdvance)

  def applyMemento(cycle: Cycle, memento: CycleMemento): Cycle =
    updatePlayheadAndLastAdvance(cycle, memento.playheadPosition, memento.lastFrameAdvance)

  def runActions(cycle: Cycle, gameTime: GameTime, actions: List[AnimationAction]): Cycle =
    actions.foldLeft(cycle) { (cycle, action) =>
      action match {
        case Play =>
          cycle.applyMemento(
            calculateNextPlayheadPosition(gameTime, cycle.playheadPosition, cycle.currentFrame.duration, cycle.frameCount, cycle.lastFrameAdvance)
          )

        case ChangeCycle(_) =>
          cycle // No op, done at animation level.

        case JumpToFirstFrame =>
          updatePlayheadAndLastAdvance(cycle, 0, cycle.lastFrameAdvance)

        case JumpToLastFrame =>
          updatePlayheadAndLastAdvance(cycle, cycle.frameCount - 1, cycle.lastFrameAdvance)

        case JumpToFrame(number) =>
          updatePlayheadAndLastAdvance(cycle, if (number > cycle.frameCount - 1) cycle.frameCount - 1 else number, cycle.lastFrameAdvance)

      }
    }
}

final case class CycleLabel(value: String) extends AnyVal

final class CycleMemento(val playheadPosition: Int, val lastFrameAdvance: Long)
object CycleMemento {
  def apply(playheadPosition: Int, lastFrameAdvance: Long): CycleMemento =
    new CycleMemento(playheadPosition, lastFrameAdvance)
}
