package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.renderer.ClearColor

import scala.language.implicitConversions

object MyGame extends GameEngine[MyStartupData, MyErrorReport, Stuff] {

  private val viewportHeight: Int = 256
  private val viewportWidth: Int = 455

  def config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0, 0, 0, 1),
    magnification = 1
  )

  def assets: Set[AssetType] = MyAssets.assets

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

  def initialise(assetCollection: AssetCollection): Startup[MyErrorReport, MyStartupData] = {
    val pass: Boolean = true

    if(pass) MyStartupData("Hello")
    else MyErrorReport(List("Boom!", "Boom!", "Shake the room!"))
  }

  var tmpX: Int = 0
  var tmpY: Int = 0
  var angle: Double = 0

  var firstRun: Boolean = true

  var aseprite: Option[Aseprite] = None
  var asepriteSprite: Option[Sprite] = None

  def updateModel(gameTime: GameTime, state: Stuff): GameEvent => Stuff = {
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

  def updateView(currentState: Stuff): SceneGraphNode = {
    SceneGraphNodeBranch(
      currentState.blocks.blocks.map { b =>
        Graphic(Rectangle(Point(b.x, b.y), Point(64, 64)), Depth(b.zIndex), b.textureName, ref = Point(0, 0), crop = None, effects = Effects.default)
          .withAlpha(b.alpha)
          .withTint(b.tint.r, b.tint.g, b.tint.b)
          .flipHorizontal(b.flipH)
          .flipVertical(b.flipV)
      } ++
        {
          if(asepriteSprite.isEmpty) Nil else {
            asepriteSprite = asepriteSprite.map(_.nextFrame)
//            println(asepriteSprite.get.animations.currentCycle.playheadPosition)
            List(asepriteSprite.get)
          }
        } ++
        List(
          Sprite(
            bounds = Rectangle(Point(0, 128), Point(64, 64)),
            depth = Depth(3),
            imageAssetRef = MyAssets.trafficLightsName,
            animations =
              Animations(
                Point(128, 128),
                Cycle(
                  label = "trafficlights",
                  playheadPosition = 0,
                  frame = Frame(
                    bounds = Rectangle(
                      Point(0, 0),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isRed
                  ),
                  frames = Nil,
                  current = true
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
                      Point(0, 64),
                      Point(64, 64)
                    ),
                    current = currentState.trafficLights.isGreen
                  )
                ),
                cycles = Nil
              ),
            ref = Point(0, 0),
            effects = Effects.default
          ),
          Text(
            text = "CBA",
            alignment = AlignLeft,
            position = Point(100, 100),
            depth = Depth(10),
            fontInfo = FontInfo(
              charSize = Point(64, 72),
              fontSpriteSheet = FontSpriteSheet(
                imageAssetRef = MyAssets.fontName,
                size = Point(888, 640)
              ),
              fontChar = FontChar("A", Point(8, 215)),
              fontChars = Nil
            )
              .addChar(FontChar("B", Point(8 + 64, 215)))
              .addChar(FontChar("C", Point(8 + 64 + 64, 215))),
            effects = Effects.default
          )
        )
    )
  }

}

case class MyStartupData(name: String)

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

case class MyErrorReport(errors: List[String])

object MyErrorReport {

  implicit val toErrorReport: ToReportable[MyErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

}