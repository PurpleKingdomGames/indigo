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
        DelayState(Seconds(3), Seconds(3), Seconds.zero)
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
        Sigs.tween(model.delayState).map(pt => Assets.Dots.dots.moveTo(pt)).at(context.running)
      ).addUiLayerNodes(
        Text("Left: " + model.delayState.remainingDelay.value.toString, 10, 150, 1, Assets.fontKey)
      )
    )

}

final case class Model(delayState: DelayState) {

  def update(running: Seconds): Model =
    this.copy(
      delayState = Sigs.delay.get.toSignal(delayState).at(running)
    )

}

object Sigs {

  val delay: SignalState[DelayState, Seconds] =
    SignalState { (state: DelayState) =>
      if (state.remainingDelay <= Seconds(0))
        Signal { currentTime =>
          val s =
            state.copy(
              remainingDelay = state.duration - (currentTime - state.startedAt)
            )

          (state, currentTime - (state.startedAt + state.duration))
        }
      else
        Signal { currentTime =>
          val s =
            state.copy(
              remainingDelay = state.duration - (currentTime - state.startedAt)
            )

          (s, Seconds.zero)
        }
    }

  val signal: Signal[Point] =
    Signal.Lerp(Point(0, 0), Point(150, 50), Seconds(2))

  def tween(delayState: DelayState): Signal[Point] =
    delay.toSignal(delayState).map(signal.at)

}

final case class DelayState(duration: Seconds, remainingDelay: Seconds, startedAt: Seconds)

// -- WIP, trying to codify the above into something nicer...

final case class Tween(stages: List[Stage], startedAt: Seconds) {
  def totalDuration: Seconds = stages.foldLeft(Seconds.zero)(_ + _.duration)

  def update(running: Seconds): Tween =
    this.copy(
      stages = stages.map(_.update(running, startedAt))
    )
}

sealed trait Stage {
  val startAfter: Seconds
  val duration: Seconds
  val timeUntilStart: Seconds

  def update(running: Seconds, startedAt: Seconds): Stage
}
object Stage {

  final case class Delay(until: Seconds, timeUntilStart: Seconds) extends Stage {
    val startAfter: Seconds = until
    val duration: Seconds = timeUntilStart

    def update(running: Seconds, startedAt: Seconds): Delay =
      if (timeUntilStart <= Seconds.zero) this
      else this.copy(timeUntilStart = until - (running - startedAt))
  }

}
