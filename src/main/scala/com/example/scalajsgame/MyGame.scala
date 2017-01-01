package com.example.scalajsgame

import purple.gameengine._
import purple.renderer.{ClearColor, ImageAsset}

import scala.language.implicitConversions

object MyGame extends GameEngine[Blocks] {

  private val viewportHeight: Int = 256
  private val viewportWidth: Int = 455

  def config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0, 0, 0, 1),
    magnification = 1
  )

  val spriteSheetName1: String = "blob1"
  val spriteSheetName2: String = "blob2"
  val spriteSheetName3: String = "f"
  val trafficLightsName: String = "trafficlights"

  private val spriteAsset1 = ImageAsset(spriteSheetName1, "Sprite-0001.png")
  private val spriteAsset2 = ImageAsset(spriteSheetName2, "Sprite-0002.png")
  private val spriteAsset3 = ImageAsset(spriteSheetName3, "f-texture.png")
  private val trafficLightsAsset = ImageAsset(trafficLightsName, "trafficlights.png")

  def imageAssets: Set[ImageAsset] = Set(spriteAsset1, spriteAsset2, spriteAsset3, trafficLightsAsset)

  def initialModel: Blocks = Blocks(
    List(
      Block(0, 0, 0, 0, 0, 1, BlockTint(1, 0, 0), spriteSheetName1, false, false),
      Block(0, 0, 1, 32, 32, 1, BlockTint(1, 1, 1), spriteSheetName2, false, false),
      Block(0, 0, 2, 64, 64, 1, BlockTint(1, 1, 1), spriteSheetName3, false, false)
    )
  )

  var tmpX: Int = 0
  var tmpY: Int = 0
  var angle: Double = 0

  def updateModel(timeDelta: Double, previousState: Blocks): Blocks = {

    tmpX = (Math.sin(angle) * 32).toInt
    tmpY = (Math.cos(angle) * 32).toInt
    angle = angle + 0.01

    previousState.copy(
      blocks = previousState.blocks.map(blk => blk.copy(x = tmpX + blk.centerX, y = tmpY + blk.centerY))
    )

  }

  def updateView(currentState: Blocks): SceneGraphNode = {
    SceneGraphNodeBranch(
      currentState.blocks.map { b =>
        SceneGraphNodeLeaf(b.x, b.y, b.zIndex, 64, 64, b.textureName, SceneGraphNodeLeafEffects(b.alpha, Tint(b.tint.r, b.tint.g, b.tint.b), Flip(b.flipH, b.flipV)))
      } ++
        List(
          SceneGraphNodeLeaf(
            128,
            128,
            3,
            64,
            64,
            trafficLightsName,
            SceneGraphNodeLeafEffects(
              1,
              Tint(1,1,1),
              Flip(false, false)
            )
          )
        )
    )
  }

}

case class Blocks(blocks: List[Block])
case class Block(x: Int, y: Int, zIndex: Int, centerX: Int, centerY: Int, alpha: Double, tint: BlockTint, textureName: String, flipH: Boolean, flipV: Boolean)
case class BlockTint(r: Double, g: Double, b: Double)