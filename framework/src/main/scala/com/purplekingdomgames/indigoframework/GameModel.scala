package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine._

object GameModelHelper {

  def initialModel(startupData: StartupData): GameModel =
    GameModel(startupData.gameDefinition)

  def updateModel(gameTime: GameTime, state: GameModel): GameEvent => GameModel = {
    _ => state
//    case FrameTick =>
//      state
//
//    case KeyDown(Keys.LEFT_ARROW) =>
//      state.copy(dude = state.dude.walkLeft)
//
//    case KeyDown(Keys.RIGHT_ARROW) =>
//      state.copy(dude = state.dude.walkRight)
//
//    case KeyDown(Keys.UP_ARROW) =>
//      state.copy(dude = state.dude.walkUp)
//
//    case KeyDown(Keys.DOWN_ARROW) =>
//      state.copy(dude = state.dude.walkDown)
//
//    case KeyUp(_) =>
//      state.copy(dude = state.dude.idle)
//
//    case _ =>
//      //Logger.info(e)
//      state
  }

}

case class GameModel(gameDefinition: GameDefinition)

//case class MyGameModel(dude: DudeModel)
//
//case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
//  def idle: DudeModel = this.copy(walkDirection = DudeIdle)
//  def walkLeft: DudeModel = this.copy(walkDirection = DudeLeft)
//  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
//  def walkUp: DudeModel = this.copy(walkDirection = DudeUp)
//  def walkDown: DudeModel = this.copy(walkDirection = DudeDown)
//}
//
//sealed trait DudeDirection {
//  val cycleName: String
//}
//case object DudeIdle extends DudeDirection { val cycleName: String = "blink" }
//case object DudeLeft extends DudeDirection { val cycleName: String = "walk left" }
//case object DudeRight extends DudeDirection { val cycleName: String = "walk right" }
//case object DudeUp extends DudeDirection { val cycleName: String = "walk up" }
//case object DudeDown extends DudeDirection { val cycleName: String = "walk down" }
