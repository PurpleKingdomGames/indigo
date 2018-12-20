package com.example.perf

import indigo._

object PerfModel {

  def initialModel(startupData: MyStartupData): MyGameModel =
    MyGameModel(
      DudeModel(startupData.dude, DudeIdle)
    )

  def updateModel(state: MyGameModel): GlobalEvent => UpdatedModel[MyGameModel] = {
    case FrameTick =>
      UpdatedModel(state)

    case KeyboardEvent.KeyDown(Keys.LEFT_ARROW) =>
      UpdatedModel(state.copy(dude = state.dude.walkLeft))

    case KeyboardEvent.KeyDown(Keys.RIGHT_ARROW) =>
      UpdatedModel(state.copy(dude = state.dude.walkRight))

    case KeyboardEvent.KeyDown(Keys.UP_ARROW) =>
      UpdatedModel(state.copy(dude = state.dude.walkUp))

    case KeyboardEvent.KeyDown(Keys.DOWN_ARROW) =>
      UpdatedModel(state.copy(dude = state.dude.walkDown))

    case KeyboardEvent.KeyUp(_) =>
      UpdatedModel(state.copy(dude = state.dude.idle))

    case _ =>
      //Logger.info(e)
      UpdatedModel(state)
  }

}

final case class MyGameModel(dude: DudeModel)

final case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
  def idle: DudeModel      = this.copy(walkDirection = DudeIdle)
  def walkLeft: DudeModel  = this.copy(walkDirection = DudeLeft)
  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
  def walkUp: DudeModel    = this.copy(walkDirection = DudeUp)
  def walkDown: DudeModel  = this.copy(walkDirection = DudeDown)
}

sealed trait DudeDirection {
  val cycleName: String
}
case object DudeIdle  extends DudeDirection { val cycleName: String = "blink"      }
case object DudeLeft  extends DudeDirection { val cycleName: String = "walk left"  }
case object DudeRight extends DudeDirection { val cycleName: String = "walk right" }
case object DudeUp    extends DudeDirection { val cycleName: String = "walk up"    }
case object DudeDown  extends DudeDirection { val cycleName: String = "walk down"  }
