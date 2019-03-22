package indigo.gameengine.scenegraph

import indigo.gameengine.GameTime
import indigo.gameengine.scenegraph.AnimationAction._
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point, Rectangle}

import indigo.EqualTo._

/*
Animations are really timeline animations:
Construction is about adding animation cycles with frames
The API provided is about issuing commands to control playback.
 */
final case class Animations(animationsKey: AnimationsKey,
                            imageAssetRef: String,
                            spriteSheetSize: Point,
                            currentCycleLabel: CycleLabel,
                            cycle: Cycle,
                            cycles: Map[CycleLabel, Cycle],
                            actions: List[AnimationAction]) {

  private val nonEmptyCycles: Map[CycleLabel, Cycle] = cycles ++ Map(cycle.label -> cycle)

  def currentCycle: Cycle = nonEmptyCycles.getOrElse(currentCycleLabel, cycle)

  def addCycle(cycle: Cycle): Animations =
    Animations(animationsKey, imageAssetRef, spriteSheetSize, currentCycleLabel, cycle, nonEmptyCycles, Nil)

  def addAction(action: AnimationAction): Animations = this.copy(actions = actions :+ action)

  def withAnimationsKey(animationsKey: AnimationsKey): Animations =
    this.copy(animationsKey = animationsKey)

  private[gameengine] val frameHash: String = currentFrame.bounds.hash + "_" + imageAssetRef

  private[gameengine] def currentCycleName: String = currentCycle.label.label

  private[gameengine] def currentFrame: Frame = currentCycle.currentFrame

  private[gameengine] def saveMemento(bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  private[gameengine] def applyMemento(memento: AnimationMemento): Animations =
    Animations(
      animationsKey = animationsKey,
      imageAssetRef = imageAssetRef,
      spriteSheetSize = spriteSheetSize,
      currentCycleLabel = memento.currentCycleLabel,
      cycle = nonEmptyCycles
        .getOrElse(memento.currentCycleLabel, cycle)
        .copy(playheadPosition = memento.currentCycleMemento.playheadPosition, lastFrameAdvance = memento.currentCycleMemento.lastFrameAdvance),
      cycles = nonEmptyCycles.filter(p => p._1.label != memento.currentCycleLabel.label),
      actions = actions
    )

  private[gameengine] def runActions(gameTime: GameTime): Animations =
    actions.foldLeft(this) { (anim, action) =>
      action match {
        case ChangeCycle(label) =>
          anim.copy(currentCycleLabel = CycleLabel(label))

        case _ =>
          anim.copy(cycle = anim.currentCycle.runActions(gameTime, actions))
      }
    }

}

object Animations {
  def apply(animationsKey: AnimationsKey, imageAssetRef: String, spriteSheetWidth: Int, spriteSheetHeight: Int, cycle: Cycle): Animations =
    Animations(animationsKey, imageAssetRef, Point(spriteSheetWidth, spriteSheetHeight), cycle.label, cycle, Map.empty[CycleLabel, Cycle], Nil)
}

final case class AnimationsKey(key: String) extends AnyVal {
  def ===(other: AnimationsKey): Boolean =
    AnimationsKey.equality(this, other)
}
object AnimationsKey {
  def equality(a: AnimationsKey, b: AnimationsKey): Boolean =
    a.key === b.key
}

final case class Cycle(label: CycleLabel, frame: Frame, frames: List[Frame], private[gameengine] val playheadPosition: Int, private[gameengine] val lastFrameAdvance: Double) {
  private val nonEmptyFrames: List[Frame] = frame :: frames

  def addFrame(newFrame: Frame): Cycle =
    Cycle(label, frame, nonEmptyFrames.drop(1) ++ List(newFrame), playheadPosition, lastFrameAdvance)

  private val frameCount: Int = nonEmptyFrames.length

  private[gameengine] def currentFrame: Frame = nonEmptyFrames(playheadPosition % frameCount)

  private[gameengine] def saveMemento: CycleMemento =
    CycleMemento(playheadPosition, lastFrameAdvance)

  private[gameengine] def applyMemento(memento: CycleMemento): Cycle =
    this.copy(playheadPosition = memento.playheadPosition, lastFrameAdvance = memento.lastFrameAdvance)

  private[gameengine] def runActions(gameTime: GameTime, actions: List[AnimationAction]): Cycle =
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

  private[gameengine] def calculateNextPlayheadPosition(gameTime: GameTime, currentPosition: Int, frameDuration: Int, frameCount: Int, lastFrameAdvance: Double): NextPlayheadPositon =
    if (gameTime.running.value >= lastFrameAdvance + frameDuration)
      NextPlayheadPositon((currentPosition + 1) % frameCount, gameTime.running.value)
    else
      NextPlayheadPositon(currentPosition, lastFrameAdvance)

}

final case class CycleLabel(label: String) extends AnyVal

final case class Frame(bounds: Rectangle, duration: Int)

object Frame {
  def apply(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), 1)
  def apply(x: Int, y: Int, width: Int, height: Int, duration: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), duration)
}

sealed trait AnimationAction {
  val hash: String
}
object AnimationAction {
  case object Play extends AnimationAction {
    val hash: String = "Play"
  }

  final case class ChangeCycle(label: String) extends AnimationAction {
    val hash: String = s"ChangeCycle($label)"
  }

  case object JumpToFirstFrame extends AnimationAction {
    val hash: String = "JumpToFirstFrame"
  }

  case object JumpToLastFrame extends AnimationAction {
    val hash: String = "JumpToLastFrame"
  }

  final case class JumpToFrame(number: Int) extends AnimationAction {
    val hash: String = s"JumpToFrame($number)"
  }
}

private[gameengine] final case class NextPlayheadPositon(position: Int, lastFrameAdvance: Double)

private[gameengine] final case class AnimationMemento(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento)

private[gameengine] final case class CycleMemento(playheadPosition: Int, lastFrameAdvance: Double)
