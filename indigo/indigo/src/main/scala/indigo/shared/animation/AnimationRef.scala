package indigo.shared.animation

import indigo.shared.animation.AnimationAction.ChangeCycle
import indigo.shared.animation.AnimationAction.JumpToFirstFrame
import indigo.shared.animation.AnimationAction.JumpToFrame
import indigo.shared.animation.AnimationAction.JumpToLastFrame
import indigo.shared.animation.AnimationAction.Play
import indigo.shared.animation.AnimationAction.ScrubTo
import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
import indigo.shared.temporal.Signal
import indigo.shared.time.GameTime
import indigo.shared.time.Millis

final case class AnimationRef(
    animationKey: AnimationKey,
    currentCycleLabel: CycleLabel,
    cycles: Map[CycleLabel, CycleRef]
) derives CanEqual {

  val frameHash: String =
    currentFrame.crop.hashCode().toString

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

  def runActions(actions: Batch[AnimationAction], gameTime: GameTime): AnimationRef =
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
      animation.cycles.toBatch.map(c => (c.label, CycleRef.fromCycle(c))).toMap
    )

  given CanEqual[Option[AnimationRef], Option[AnimationRef]] = CanEqual.derived
}

final case class CycleRef(
    label: CycleLabel,
    frames: Batch[Frame],
    playheadPosition: Int,
    lastFrameAdvance: Millis
) derives CanEqual {

  lazy val frameCount: Int =
    frames.length

  def currentFrame: Frame =
    frames(playheadPosition % frameCount)

  def saveMemento: CycleMemento =
    CycleMemento(playheadPosition, lastFrameAdvance)

  def updatePlayheadAndLastAdvance(playheadPosition: Int, lastFrameAdvance: Millis): CycleRef =
    CycleRef(label, frames, playheadPosition, lastFrameAdvance)

  def applyMemento(memento: CycleMemento): CycleRef =
    updatePlayheadAndLastAdvance(memento.playheadPosition, memento.lastFrameAdvance)

  def runActions(gameTime: GameTime, actions: Batch[AnimationAction]): CycleRef =
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
          updatePlayheadAndLastAdvance(0, gameTime.running.toMillis)

        case JumpToLastFrame =>
          updatePlayheadAndLastAdvance(frameCount - 1, gameTime.running.toMillis)

        case JumpToFrame(number) =>
          updatePlayheadAndLastAdvance(
            if (number > frameCount - 1) frameCount - 1 else number,
            gameTime.running.toMillis
          )

        case ScrubTo(position) =>
          val number = (frameCount.toDouble * position).toInt
          updatePlayheadAndLastAdvance(
            if (number > frameCount - 1) frameCount - 1 else number,
            gameTime.running.toMillis
          )

      }
    }
}
object CycleRef:
  given CanEqual[Option[CycleRef], Option[CycleRef]] = CanEqual.derived

  def fromCycle(cycle: Cycle): CycleRef =
    new CycleRef(
      cycle.label,
      cycle.frames.toBatch,
      cycle.playheadPosition,
      cycle.lastFrameAdvance
    )

  def create(label: CycleLabel, frames: Batch[Frame]): CycleRef =
    new CycleRef(label, frames, 0, Millis.zero)

  def calculateNextPlayheadPosition(
      currentPosition: Int,
      frameDuration: Millis,
      frameCount: Int,
      lastFrameAdvance: Millis
  ): Signal[CycleMemento] =
    Signal { t =>
      if t.toMillis >= lastFrameAdvance + frameDuration then
        val framestoAdvance = ((t.toMillis - lastFrameAdvance) / frameDuration).toInt
        CycleMemento((currentPosition + framestoAdvance) % frameCount, t.toMillis)
      else CycleMemento(currentPosition, lastFrameAdvance)
    }

final case class AnimationMemento(
    bindingKey: BindingKey,
    currentCycleLabel: CycleLabel,
    currentCycleMemento: CycleMemento
) derives CanEqual
