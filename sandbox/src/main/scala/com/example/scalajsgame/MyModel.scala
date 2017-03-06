package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Depth
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Aseprite, AsepriteHelper, Sprite}

object MyModel {

  def initialModel(startupData: MyStartupData): Stuff =
    Stuff(
      DudeModel(startupData.dude, DudeIdle),
      Blocks(
        List(
          Block(0, 0, 0, 0, 0, 1, BlockTint(1, 0, 0), MyAssets.spriteSheetName1, false, false),
          Block(0, 0, 1, 32, 32, 1, BlockTint(1, 1, 1), MyAssets.spriteSheetName2, false, false),
          Block(0, 0, 2, 64, 64, 1, BlockTint(1, 1, 1), MyAssets.spriteSheetName3, false, false)
        )
      ),
      TrafficLights("red", 0, 0)
    )

  var tmpX: Int = 0
  var tmpY: Int = 0
  var angle: Double = 0

  var firstRun: Boolean = true

  var aseprite: Option[Aseprite] = None
  var asepriteSprite: Option[Sprite] = None

  def updateModel(assetCollection: AssetCollection, gameTime: GameTime, state: Stuff): GameEvent => Stuff = {
    case FrameTick =>

      if(firstRun) {
        firstRun = false
        val json = assetCollection.texts.find(p => p.name == MyAssets.trafficLightsName + "-json").map(_.contents).getOrElse("BOOM!")
        aseprite = AsepriteHelper.fromJson(json)
        asepriteSprite = aseprite.flatMap(asepriteObj => AsepriteHelper.toSprite(asepriteObj, Depth(3), MyAssets.trafficLightsName))
      }

      tmpX = (Math.sin(angle) * 32).toInt
      tmpY = (Math.cos(angle) * 32).toInt
      angle = angle + 0.01

      state.copy(
        blocks = Blocks(state.blocks.blocks.map(blk => blk.copy(x = tmpX + blk.centerX, y = tmpY + blk.centerY))),
        trafficLights = state.trafficLights.nextColor(gameTime.delta)
      )

    case KeyDown(Keys.LeftArrow) =>
      state.copy(dude = state.dude.walkLeft)
    case KeyDown(Keys.RightArrow) =>
      state.copy(dude = state.dude.walkRight)
    case KeyDown(Keys.UpArrow) =>
      state.copy(dude = state.dude.walkUp)
    case KeyDown(Keys.DownArrow) =>
      state.copy(dude = state.dude.walkDown)
    case KeyUp(_) =>
      state.copy(dude = state.dude.idle)

    case e =>
      //      println(e)
      state
  }

}

case class Stuff(dude: DudeModel, blocks: Blocks, trafficLights: TrafficLights)

case class DudeModel(dude: Dude, walkDirection: DudeDirection) {
  def idle: DudeModel = this.copy(walkDirection = DudeIdle)
  def walkLeft: DudeModel = this.copy(walkDirection = DudeLeft)
  def walkRight: DudeModel = this.copy(walkDirection = DudeRight)
  def walkUp: DudeModel = this.copy(walkDirection = DudeUp)
  def walkDown: DudeModel = this.copy(walkDirection = DudeDown)
}

sealed trait DudeDirection {
  val cycleName: String
}
case object DudeIdle extends DudeDirection { val cycleName: String = "blink" }
case object DudeLeft extends DudeDirection { val cycleName: String = "walk left" }
case object DudeRight extends DudeDirection { val cycleName: String = "walk right" }
case object DudeUp extends DudeDirection { val cycleName: String = "walk up" }
case object DudeDown extends DudeDirection { val cycleName: String = "walk down" }

case class TrafficLights(color: String, lastChange: Double, timeSinceChange: Double) {
  def nextColor(timeDelta: Double): TrafficLights = {
    if(timeSinceChange + timeDelta >= 1000) {
      color match {
        case "red" => TrafficLights("amber", lastChange + timeDelta, 0)
        case "amber" => TrafficLights("green", lastChange + timeDelta, 0)
        case "green" => TrafficLights("red", lastChange + timeDelta, 0)
      }
    } else {
      this.copy(timeSinceChange = timeSinceChange + timeDelta)
    }
  }

  def isRed: Boolean = color == "red"
  def isAmber: Boolean = color == "amber"
  def isGreen: Boolean = color == "green"
}
case class Blocks(blocks: List[Block])
case class Block(x: Int, y: Int, zIndex: Int, centerX: Int, centerY: Int, alpha: Double, tint: BlockTint, textureName: String, flipH: Boolean, flipV: Boolean)
case class BlockTint(r: Double, g: Double, b: Double)