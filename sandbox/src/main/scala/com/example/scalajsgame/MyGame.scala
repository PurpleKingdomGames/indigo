package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.renderer.{ClearColor, ImageAsset}

import scala.language.implicitConversions

object MyGame extends GameEngine[Stuff] {

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
  val fontName: String = "fontName"

  private val spriteAsset1 = ImageAsset(spriteSheetName1, "Sprite-0001.png")
  private val spriteAsset2 = ImageAsset(spriteSheetName2, "Sprite-0002.png")
  private val spriteAsset3 = ImageAsset(spriteSheetName3, "f-texture.png")
  private val trafficLightsAsset = ImageAsset(trafficLightsName, "trafficlights.png")
  private val fontAsset = ImageAsset(fontName, "boxy_bold_font_5.png")

  def imageAssets: Set[ImageAsset] = Set(spriteAsset1, spriteAsset2, spriteAsset3, trafficLightsAsset, fontAsset)

  def textAssets: Set[TextAsset] = Set(
    TextAsset("gloop-json", "gloop.json")
  )

  def initialModel: Stuff =
    Stuff(
      Blocks(
        List(
          Block(0, 0, 0, 0, 0, 1, BlockTint(1, 0, 0), spriteSheetName1, false, false),
          Block(0, 0, 1, 32, 32, 1, BlockTint(1, 1, 1), spriteSheetName2, false, false),
          Block(0, 0, 2, 64, 64, 1, BlockTint(1, 1, 1), spriteSheetName3, false, false)
        )
      ),
      TrafficLights("red", 0, 0)
    )

  var tmpX: Int = 0
  var tmpY: Int = 0
  var angle: Double = 0

  var firstRun: Boolean = true

  def updateModel(gameTime: GameTime, state: Stuff): GameEvent => Stuff = {
    case FrameTick =>

      if(firstRun) {
        firstRun = false
        println("Text asset loading: ")
        println(assetCollection.texts.find(p => p.name == "gloop-json"))
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

  def updateView(currentState: Stuff): SceneGraphNode = {
    SceneGraphNodeBranch(
      currentState.blocks.blocks.map { b =>
        Graphic(Rectangle(Point(b.x, b.y), Point(64, 64)), Depth(b.zIndex), b.textureName)
          .withAlpha(b.alpha)
          .withTint(b.tint.r, b.tint.g, b.tint.b)
          .flipHorizontal(b.flipH)
          .flipVertical(b.flipV)
      } ++
        List(
          Sprite(
            bounds = Rectangle(Point(0, 128), Point(64, 64)),
            depth = Depth(3),
            imageAssetRef = trafficLightsName,
            animations =
              Animations(
                Point(192, 64),
                Cycle(
                  label = "trafficlights",
                  frame = Frame(
                    bounds = Rectangle(
                      Point(0, 0),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isRed
                  )
                ).addFrame(
                  frame = Frame(
                    bounds = Rectangle(
                      Point(64, 0),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isAmber
                  )
                ).addFrame(
                  frame = Frame(
                    bounds = Rectangle(
                      Point(128, 0),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isGreen
                  )
                )
              )
          ),
          Text(
            text = "CBA",
            alignment = AlignLeft,
            position = Point(100, 100),
            depth = Depth(10),
            fontInfo = FontInfo(
              charSize = Point(64, 72),
              fontSpriteSheet = FontSpriteSheet(
                imageAssetRef = fontName,
                size = Point(888, 640)
              ),
              fontChar = FontChar("A", Point(8, 215))
            )
              .addChar(FontChar("B", Point(8 + 64, 215)))
              .addChar(FontChar("C", Point(8 + 64 + 64, 215)))
          )
        )
    )
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