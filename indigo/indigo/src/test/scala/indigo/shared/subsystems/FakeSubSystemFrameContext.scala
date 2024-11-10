package indigo.shared.subsystems

import indigo.platform.assets.DynamicText
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.Context
import indigo.shared.FontRegister
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
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
