package com.example.sandbox

import indigo._
import indigo.json.Json

import indigoextras.subsystems.FPSCounter
import indigoextras.ui.InputField
import indigoextras.ui.InputFieldAssets
import indigo.scenes._

import scala.scalajs.js.annotation._
import indigo.shared.events.FullScreenEntered
import indigo.shared.events.FullScreenExited

import scala.annotation.tailrec

@JSExportTopLevel("IndigoGame")
object TweeningExample extends IndigoSandbox[Unit, Model] {

  val config: GameConfig =
    GameConfig.default
      .withMagnification(2)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set(Assets.fontInfo)

  val animations: Set[Animation] =
    Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Model] =
    Outcome(
      Model(
        // DelayState(Seconds(3), Seconds(3), Seconds.zero),
        Tween(
          List(
            Transition.Linear(
              DelayState(Seconds(2), Seconds(2) /*, None*/ ),
              TransformData(
                position = Point.zero,
                rotation = Radians.zero,
                scale = Vector2.one
              )
            ),
            Transition.Linear(
              DelayState(Seconds(2), Seconds(2) /*, None*/ ),
              TransformData(
                position = Point(160, 60),
                rotation = Radians.zero,
                scale = Vector2.one
              )
            )
          ),
          None
        )
      )
    )

  def updateModel(context: FrameContext[Unit], model: Model): GlobalEvent => Outcome[Model] = {
    case FrameTick =>
      Outcome(model.update(context.running))

    case _ =>
      Outcome(model)
  }

  def present(context: FrameContext[Unit], model: Model): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        // Sigs.tween(model.delayState).map(pt => Assets.Dots.dots.moveTo(pt)).at(context.running)//,
        model.tween.transform.map(td => Assets.Dots.dots.moveTo(td.position)).at(context.running)
      )
      // .addUiLayerNodes(
      //   Text("Left: " + model.delayState.remainingDelay.value.toString, 10, 150, 1, Assets.fontKey)
      // )
    )

}

final case class Model( /*delayState: DelayState, */ tween: Tween) {

  def update(running: Seconds): Model =
    this.copy(
      // delayState = Sigs.delay.get.toSignal(delayState).at(running),
      tween = tween.update(running)
    )

}

// object Sigs {

//   val delay: SignalState[DelayState, Seconds] =
//     SignalState { (state: DelayState) =>
//       if (state.remainingDelay <= Seconds(0))
//         Signal { currentTime =>
//           val s =
//             state.copy(
//               remainingDelay = state.duration - (currentTime - state.startedAt)
//             )

//           (state, currentTime - (state.startedAt + state.duration))
//         }
//       else
//         Signal { currentTime =>
//           val s =
//             state.copy(
//               remainingDelay = state.duration - (currentTime - state.startedAt)
//             )

//           (s, Seconds.zero)
//         }
//     }

//   val signal: Signal[Point] =
//     Signal.Lerp(Point(0, 0), Point(150, 50), Seconds(2))

//   def tween(delayState: DelayState): Signal[Point] =
//     delay.toSignal(delayState).map(signal.at)

// }

final case class DelayState(duration: Seconds, remainingDelay: Seconds /*, startedAt: Option[Seconds]*/ )

// -- WIP, trying to codify the above into something nicer...

final case class Tween(stages: List[Transition], startedAt: Option[Seconds]) {
  private lazy val totalDuration: Seconds =
    stages.foldLeft(Seconds.zero)(_ + _.delayState.duration)

  private lazy val durationOffsets: List[Seconds] =
    Tween.durationOffsets(stages.map(_.delayState.duration))

  def update(running: Seconds): Tween =
    startedAt match {
      case None =>
        this.copy(
          startedAt = Some(running),
          stages = stages.zip(durationOffsets).map {
            case (s, at) =>
              s.update(running, running + at)
          }
        )

      case Some(startTime) =>
        this.copy(
          stages = stages.zip(durationOffsets).map {
            case (s, at) =>
              s.update(running, startTime + at)
          }
        )
    }

  // Work out which signal you're in,
  // the target of the previous is the start of the current.
  // run the signal over the given thing.
  def transform: Signal[TransformData] =
    ???

}
object Tween {
  object Signals {

    val delay: Seconds => SignalState[DelayState, Seconds] =
      startedAt =>
        SignalState { (state: DelayState) =>
          if (state.remainingDelay <= Seconds(0))
            Signal { currentTime =>
              val s =
                state.copy(
                  remainingDelay = state.duration - (currentTime - startedAt)
                )

              (state, currentTime - (startedAt + state.duration))
            }
          else
            Signal { currentTime =>
              val s =
                state.copy(
                  remainingDelay = state.duration - (currentTime - startedAt)
                )

              (s, Seconds.zero)
            }
        }

    val transformer: Signal[Double] => SignalReader[(TransformData, TransformData), TransformData] =
      progression =>
        SignalReader {
          case (from: TransformData, to: TransformData) =>
            progression.map { amount =>
              TransformData(
                Point(
                  x = ((from.position.x.toDouble * (1 - amount)) + (to.position.x.toDouble * amount)).toInt,
                  y = ((from.position.y.toDouble * (1 - amount)) + (to.position.y.toDouble * amount)).toInt
                ),
                Radians((from.rotation.value * (1 - amount)) + (to.rotation.value * amount)),
                Vector2(
                  x = (from.scale.x * (1 - amount)) + (to.scale.x * amount),
                  y = (from.scale.y * (1 - amount)) + (to.scale.y * amount)
                )
              )
            }
        }

  }

  def durationOffsets(durations: List[Seconds]): List[Seconds] = {
    @tailrec
    def rec(remaining: List[Seconds], accTime: Seconds, acc: List[Seconds]): List[Seconds] =
      remaining match {
        case Nil =>
          acc.reverse

        case x :: xs =>
          val next = accTime + x
          rec(xs, next, next :: acc)
      }

    rec(durations, Seconds.zero, Nil)
  }
}

final case class TransformData(position: Point, rotation: Radians, scale: Vector2)

sealed trait Transition {
  def delayState: DelayState
  def to: TransformData

  def update(running: Seconds, startedAt: Seconds): Transition
  def tween(from: TransformData): Signal[TransformData]
}
object Transition {
  final case class Linear(delayState: DelayState, to: TransformData) extends Transition {

    def update(running: Seconds, startedAt: Seconds): Linear =
      this.copy(
        delayState = Tween.Signals.delay(startedAt).get.toSignal(delayState).at(running)
      )

    def tween(from: TransformData): Signal[TransformData] =
      Tween.Signals.transformer(Signal.Linear(delayState.duration)).toSignal((from, to))

  }

  final case class EaseIn(delayState: DelayState, to: TransformData) extends Transition {

    def update(running: Seconds, startedAt: Seconds): EaseIn =
      this.copy(
        delayState = Tween.Signals.delay(startedAt).get.toSignal(delayState).at(running)
      )

    def tween(from: TransformData): Signal[TransformData] =
      Tween.Signals.transformer(Signal.EaseIn(delayState.duration)).toSignal((from, to))

  }

  final case class EaseOut(delayState: DelayState, to: TransformData) extends Transition {

    def update(running: Seconds, startedAt: Seconds): EaseOut =
      this.copy(
        delayState = Tween.Signals.delay(startedAt).get.toSignal(delayState).at(running)
      )

    def tween(from: TransformData): Signal[TransformData] =
      Tween.Signals.transformer(Signal.EaseOut(delayState.duration)).toSignal((from, to))

  }

  final case class EaseInOut(delayState: DelayState, to: TransformData) extends Transition {

    def update(running: Seconds, startedAt: Seconds): EaseInOut =
      this.copy(
        delayState = Tween.Signals.delay(startedAt).get.toSignal(delayState).at(running)
      )

    def tween(from: TransformData): Signal[TransformData] =
      Tween.Signals.transformer(Signal.EaseInOut(delayState.duration)).toSignal((from, to))

  }
}

// sealed trait Stage {
//   val startAfter: Seconds
//   val duration: Seconds
//   val timeUntilStart: Seconds

//   def update(running: Seconds, startedAt: Seconds): Stage
// }
// object Stage {

//   final case class Delay(until: Seconds, timeUntilStart: Seconds) extends Stage {
//     val startAfter: Seconds = until
//     val duration: Seconds = timeUntilStart

//     def update(running: Seconds, startedAt: Seconds): Delay =
//       if (timeUntilStart <= Seconds.zero) this
//       else this.copy(timeUntilStart = until - (running - startedAt))
//   }

// }
