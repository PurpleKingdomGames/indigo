package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine._

object MyModel {

  def initialModel(startupData: MyStartupData): Stuff =
    Stuff(
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
        println("Text asset loading: ")

        val json = assetCollection.texts.find(p => p.name == MyAssets.trafficLightsName + "-json").map(_.contents).getOrElse("BOOM!")

        println(json)

        aseprite = AsepriteHelper.fromJson(json)

        println(aseprite)

        asepriteSprite = aseprite.flatMap(asepriteObj => AsepriteHelper.toSprite(asepriteObj, Depth(3), MyAssets.trafficLightsName))

        println(asepriteSprite)

      }

      tmpX = (Math.sin(angle) * 32).toInt
      tmpY = (Math.cos(angle) * 32).toInt
      angle = angle + 0.01

      state.copy(
        blocks = Blocks(state.blocks.blocks.map(blk => blk.copy(x = tmpX + blk.centerX, y = tmpY + blk.centerY))),
        trafficLights = state.trafficLights.nextColor(gameTime.delta)
      )

    case e =>
      //      println(e)
      state
  }

}

case class Stuff(blocks: Blocks, trafficLights: TrafficLights)
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