package indigoextras.subsystems

import indigo.shared.Context
import indigo.shared.dice.Dice
import indigo.shared.subsystems.SubSystemContext
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds

object FakeSubSystemFrameContext:

  def context(sides: Int): SubSystemContext[Unit] =
    SubSystemContext.fromContext(
      Context.initial
        .modifyFrame(
          _.withDice(Dice.loaded(sides))
        )
    )

  def context(sides: Int, time: Seconds): SubSystemContext[Unit] =
    SubSystemContext.fromContext(
      Context.initial
        .modifyFrame(
          _.withDice(Dice.loaded(sides))
            .withTime(GameTime.is(time))
        )
    )

  def context(sides: Int, time: Seconds, delta: Seconds): SubSystemContext[Unit] =
    SubSystemContext.fromContext(
      Context.initial
        .modifyFrame(
          _.withDice(Dice.loaded(sides))
            .withTime(GameTime.withDelta(time, delta))
        )
    )
