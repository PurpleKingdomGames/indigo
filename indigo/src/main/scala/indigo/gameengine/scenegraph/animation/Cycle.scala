package indigo.gameengine.scenegraph.animation

import indigo.GameTime
import indigo.AnimationAction._

final case class Cycle(label: CycleLabel, frame: Frame, frames: List[Frame], val playheadPosition: Int, val lastFrameAdvance: Long) {
  val nonEmptyFrames: List[Frame] = frame :: frames

  def addFrame(newFrame: Frame): Cycle =
    Cycle(label, frame, nonEmptyFrames.drop(1) ++ List(newFrame), playheadPosition, lastFrameAdvance)

  val frameCount: Int = nonEmptyFrames.length

  def currentFrame: Frame = nonEmptyFrames(playheadPosition % frameCount)

  def saveMemento: CycleMemento =
    CycleMemento(playheadPosition, lastFrameAdvance)

  def applyMemento(memento: CycleMemento): Cycle =
    this.copy(playheadPosition = memento.playheadPosition, lastFrameAdvance = memento.lastFrameAdvance)

  def runActions(gameTime: GameTime, actions: List[AnimationAction]): Cycle =
    actions.foldLeft(this) { (cycle, action) =>
      action match {
        case Play =>
          val next =
            Cycle.calculateNextPlayheadPosition(gameTime, playheadPosition, currentFrame.duration, frameCount, lastFrameAdvance)
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
          if (number > frameCount - 1) cycle.copy(playheadPosition = frameCount - 1)
          else cycle.copy(playheadPosition = number)
      }
    }

}

object Cycle {
  def apply(label: String, frame: Frame): Cycle                      = Cycle(CycleLabel(label), frame, Nil, 0, 0)
  def apply(label: String, frame: Frame, frames: List[Frame]): Cycle = Cycle(CycleLabel(label), frame, frames, 0, 0)

  def calculateNextPlayheadPosition(gameTime: GameTime, currentPosition: Int, frameDuration: Int, frameCount: Int, lastFrameAdvance: Long): NextPlayheadPositon =
    if (gameTime.running.value >= lastFrameAdvance + frameDuration)
      NextPlayheadPositon((currentPosition + 1) % frameCount, gameTime.running.value)
    else
      NextPlayheadPositon(currentPosition, lastFrameAdvance)

}