package indigo.scenes

import indigo.shared.Context
import indigo.shared.dice.Dice
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

object FakeFrameContext {

  def context(sides: Int): Context[Unit] =
    Context.initial
      .modifyFrame(
        _.withDice(Dice.loaded(sides))
      )

  def context(sides: Int, time: Seconds): Context[Unit] =
    Context.initial
      .modifyFrame(
        _.withDice(Dice.loaded(sides))
          .withTime(GameTime.is(time))
      )

  def context(sides: Int, time: Seconds, delta: Seconds): Context[Unit] =
    Context.initial
      .modifyFrame(
        _.withDice(Dice.loaded(sides))
          .withTime(GameTime.withDelta(time, delta))
      )

}
