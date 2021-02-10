package indigo.shared.animation

import indigo.shared.time.Millis
import indigo.shared.datatypes.BindingKey
import indigo.shared.temporal.Signal
import indigo.shared.time.GameTime
import indigo.shared.animation.AnimationAction.Play
import indigo.shared.animation.AnimationAction.ChangeCycle
import indigo.shared.animation.AnimationAction.JumpToFirstFrame
import indigo.shared.animation.AnimationAction.JumpToLastFrame
import indigo.shared.animation.AnimationAction.JumpToFrame

final case class AnimationRef(
    animationKey: AnimationKey,
    currentCycleLabel: CycleLabel,
    cycles: Map[CycleLabel, CycleRef]
) {

  lazy val frameHash: String =
    currentFrame.crop.hash

  def currentCycle: CycleRef =
    cycles.get(currentCycleLabel).getOrElse(cycles.head._2)

  def currentFrame: Frame =
    currentCycle.currentFrame

  def saveMemento(bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  def applyMemento(memento: AnimationMemento): AnimationRef =
    this.copy(
      currentCycleLabel =
        if (cycles.contains(memento.currentCycleLabel)) memento.currentCycleLabel
        else currentCycleLabel,
      cycles = cycles.updatedWith(memento.currentCycleLabel) {
        case None =>
          None

        case Some(c) =>
          Some(c.applyMemento(memento.currentCycleMemento))
      }
    )

  def runActions(actions: List[AnimationAction], gameTime: GameTime): AnimationRef =
    actions.foldLeft(this) { (anim, action) =>
      action match {
        case ChangeCycle(newLabel) if cycles.contains(newLabel) =>
          anim.copy(currentCycleLabel = newLabel)

        case ChangeCycle(_) =>
          anim

        case _ =>
          anim.copy(
            cycles = cycles.updatedWith(anim.currentCycleLabel) {
              case None =>
                None

              case Some(c) =>
                Some(c.runActions(gameTime, actions))
            }
          )
      }
    }

}
object AnimationRef {
  def fromAnimation(animation: Animation): AnimationRef =
    new AnimationRef(
      animation.animationKey,
      animation.currentCycleLabel,
      animation.cycles.toList.map(c => (c.label, CycleRef.fromCycle(c))).toMap
    )
}

final case class CycleRef(
    label: CycleLabel,
    frames: List[Frame],
    playheadPosition: Int,
    lastFrameAdvance: Millis
) {

  lazy val frameCount: Int =
    frames.length

  def currentFrame: Frame =
    frames(playheadPosition % frameCount)

  def saveMemento: CycleMemento =
    new CycleMemento(playheadPosition, lastFrameAdvance)

  def updatePlayheadAndLastAdvance(playheadPosition: Int, lastFrameAdvance: Millis): CycleRef =
    CycleRef(label, frames, playheadPosition, lastFrameAdvance)

  def applyMemento(memento: CycleMemento): CycleRef =
    updatePlayheadAndLastAdvance(memento.playheadPosition, memento.lastFrameAdvance)

  def runActions(gameTime: GameTime, actions: List[AnimationAction]): CycleRef =
    actions.foldLeft(this) { (cycle, action) =>
      action match {
        case Play =>
          applyMemento(
            CycleRef
              .calculateNextPlayheadPosition(
                playheadPosition,
                currentFrame.duration,
                frameCount,
                lastFrameAdvance
              )
              .at(gameTime.running)
          )

        case ChangeCycle(_) =>
          cycle // No op, done at animation level.

        case JumpToFirstFrame =>
          updatePlayheadAndLastAdvance(0, lastFrameAdvance)

        case JumpToLastFrame =>
          updatePlayheadAndLastAdvance(frameCount - 1, lastFrameAdvance)

        case JumpToFrame(number) =>
          updatePlayheadAndLastAdvance(if (number > frameCount - 1) frameCount - 1 else number, lastFrameAdvance)

      }
    }
}
object CycleRef {
  def fromCycle(cycle: Cycle): CycleRef =
    new CycleRef(
      cycle.label,
      cycle.frames.toList,
      cycle.playheadPosition,
      cycle.lastFrameAdvance
    )

  def create(label: CycleLabel, frames: List[Frame]): CycleRef =
    new CycleRef(label, frames, 0, Millis.zero)

  def calculateNextPlayheadPosition(currentPosition: Int, frameDuration: Millis, frameCount: Int, lastFrameAdvance: Millis): Signal[CycleMemento] =
    Signal { t =>
      if (t.toMillis >= lastFrameAdvance + frameDuration) {
        val framestoAdvance = ((t.toMillis.value - lastFrameAdvance.value) / frameDuration.value).toInt
        CycleMemento((currentPosition + framestoAdvance) % frameCount, t.toMillis)
      } else
        CycleMemento(currentPosition, lastFrameAdvance)
    }
}

final case class AnimationMemento(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento)
object AnimationMemento {
  def apply(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento): AnimationMemento =
    new AnimationMemento(bindingKey, currentCycleLabel, currentCycleMemento)
}
