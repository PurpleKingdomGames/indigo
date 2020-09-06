package indigo.shared.animation

import indigo.shared.datatypes.Material
import indigo.shared.time.Millis
import indigo.shared.datatypes.BindingKey
import indigo.shared.temporal.Signal
import indigo.shared.time.GameTime
import indigo.shared.animation.AnimationAction.Play
import indigo.shared.animation.AnimationAction.ChangeCycle
import indigo.shared.animation.AnimationAction.JumpToFirstFrame
import indigo.shared.animation.AnimationAction.JumpToLastFrame
import indigo.shared.animation.AnimationAction.JumpToFrame
import indigo.shared.EqualTo
import indigo.shared.AsString

final case class AnimationRef(
    animationKey: AnimationKey,
    material: Material,
    currentCycleLabel: CycleLabel,
    cycles: Map[CycleLabel, CycleRef]
) {

  lazy val frameHash: String =
    currentFrame.crop.hash + "_" + currentFrame.frameMaterial.map(_.hash).getOrElse(material.hash)

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
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
      animation.material,
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

final class AnimationMemento(val bindingKey: BindingKey, val currentCycleLabel: CycleLabel, val currentCycleMemento: CycleMemento) {

  def asString: String =
    implicitly[AsString[AnimationMemento]].show(this)

  override def toString: String =
    asString

  def ===(other: AnimationMemento): Boolean =
    implicitly[EqualTo[AnimationMemento]].equal(this, other)

  @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf", "org.wartremover.warts.AsInstanceOf"))
  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[AnimationMemento])
      this === obj.asInstanceOf[AnimationMemento]
    else false

}
object AnimationMemento {

  implicit val animationMementoAsString: AsString[AnimationMemento] =
    AsString.create { m =>
      s"""AnimationMemento(bindingKey = ${m.bindingKey.toString()}, cycleLabel = ${m.currentCycleLabel.toString()}, cycleMemento = ${m.currentCycleMemento.toString()})"""
    }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  implicit val animationMementoEqualTo: EqualTo[AnimationMemento] = {
    val bk = implicitly[EqualTo[BindingKey]]
    val cl = implicitly[EqualTo[CycleLabel]]
    val cm = implicitly[EqualTo[CycleMemento]]

    EqualTo.create {
      case (a, b) =>
        bk.equal(a.bindingKey, b.bindingKey) &&
          cl.equal(a.currentCycleLabel, b.currentCycleLabel) &&
          cm.equal(a.currentCycleMemento, b.currentCycleMemento)
    }
  }

  def apply(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento): AnimationMemento =
    new AnimationMemento(bindingKey, currentCycleLabel, currentCycleMemento)
}
