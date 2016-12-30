package com.example.scalajsgame

import purple.gameengine._
import purple.renderer.ImageAsset

import scala.language.implicitConversions

object MyGame extends GameEngine[Blocks] {

  private val viewportHeight: Int = 256
  private val viewportWidth: Int = 455

  def config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30
  )

  val spriteSheetName: String = "blob"

  private val spriteAsset = ImageAsset(spriteSheetName, "Sprite-0001.png")

  def imageAssets: Set[ImageAsset] = Set(spriteAsset)

  def initialModel: Blocks = Blocks(
    List(
      Block(0, 0, 0, 0, 1, BlockTint(1, 0, 0)),
      Block(0, 0, 32, 32, 0.5, BlockTint(0, 1, 0)),
      Block(0, 0, viewportWidth - 64, viewportHeight - 64, 0.25, BlockTint(0, 0, 1))
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
      currentState.blocks.map(b => SceneGraphNodeLeaf(b.x, b.y, 64, 64, spriteSheetName, SceneGraphNodeLeafEffects(b.alpha, Tint(b.tint.r, b.tint.g, b.tint.b))))
    )
  }

}

case class Blocks(blocks: List[Block])
case class Block(x: Int, y: Int, centerX: Int, centerY: Int, alpha: Double, tint: BlockTint)
case class BlockTint(r: Double, g: Double, b: Double)