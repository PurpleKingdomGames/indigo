package com.example.scalajsgame

import purple.gameengine._
import purple.renderer.ImageAsset

import scala.language.implicitConversions

object MyGame extends GameEngine[Blocks] {

  private val viewportHeight: Int = 256
  private val viewportWidth: Int = (viewportHeight.toDouble * (16d / 9d)).toInt

  def config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight)
  )

  private val spriteAsset = ImageAsset("blob", "Sprite-0001.png")

  def imageAssets: Set[ImageAsset] = Set(spriteAsset)

  def initialModel: Blocks = Blocks(
    List(
      Block(0, 0),
      Block(32, 32),
      Block(viewportWidth - 64, viewportHeight - 64)
    )
  )

  def updateModel(previousState: Blocks): Blocks = initialModel

  def updateView(currentState: Blocks): SceneGraphNode = {
    SceneGraphNodeBranch(
      currentState.blocks.map(b => SceneGraphNodeLeaf(b.x, b.y, 64, 64, spriteAsset))
    )
  }

//  private val viewportHeight: Int = 256
//  private val viewportWidth: Int = (viewportHeight.toDouble * (16d / 9d)).toInt
//
//  def main(): Unit = {
//
//    val renderer = Renderer(RendererConfig())
//
//    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
//    image.src = "Sprite-0001.png"
////    image.src = "f-texture.png"
//    image.onload = (_: dom.Event) => {
//
//      implicit val cnc: ContextAndCanvas = renderer.createCanvas("canvas", viewportWidth, viewportHeight)
//
//      renderer.addRectangle(Rectangle2D(0, 0, 64, 64, image))
//      renderer.addRectangle(Rectangle2D(32, 32, 64, 64, image))
//      renderer.addRectangle(Rectangle2D(viewportWidth - 64, viewportHeight - 64, 64, 64, image))
//
//      renderer.drawScene
//    }
//
//  }

}

case class Blocks(blocks: List[Block])
case class Block(x: Int, y: Int)