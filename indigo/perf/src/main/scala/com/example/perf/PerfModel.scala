package com.example.perf

import indigo.*

object PerfModel {

  def initialModel(dude: Dude): DudeModel =
    DudeModel(dude, DudeIdle)

  def updateModel(state: DudeModel): GlobalEvent => Outcome[DudeModel] = {
    case FrameTick =>
      Outcome(state)

    case KeyboardEvent.KeyDown(Key.ARROW_LEFT) =>
      Outcome(state.walkLeft)

    case KeyboardEvent.KeyDown(Key.ARROW_RIGHT) =>
      Outcome(state.walkRight)

    case KeyboardEvent.KeyDown(Key.ARROW_UP) =>
      Outcome(state.walkUp)

    case KeyboardEvent.KeyDown(Key.ARROW_DOWN) =>
      Outcome(state.walkDown)

    case KeyboardEvent.KeyUp(_) =>
      Outcome(state.idle)

    case _ =>
      // Logger.info(e)
      Outcome(state)
  }

}

final case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
  def idle: DudeModel      = this.copy(walkDirection = DudeIdle)
  def walkLeft: DudeModel  = this.copy(walkDirection = DudeLeft)
  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
  def walkUp: DudeModel    = this.copy(walkDirection = DudeUp)
  def walkDown: DudeModel  = this.copy(walkDirection = DudeDown)
}

sealed trait DudeDirection derives CanEqual {
  val cycleName: CycleLabel
}
case object DudeIdle  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("blink")      }
case object DudeLeft  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk left")  }
case object DudeRight extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk right") }
case object DudeUp    extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk up")    }
case object DudeDown  extends DudeDirection { val cycleName: CycleLabel = CycleLabel("walk down")  }
