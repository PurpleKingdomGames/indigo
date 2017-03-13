package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AlignCenter, AlignLeft, AlignRight, Point}
import org.scalajs.dom
import com.purplekingdomgames.indigo.renderer._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSApp
import scala.language.implicitConversions

case class GameTime(running: Double, delta: Double)

/*
There are some questions over time.
Events are collected during a frame and then all processed sequentially at
the beginning of the next frame so:
1. Do you want the real time that the event happened?
2. The time since the beginning of the frame (since they are sequential a
long update could have a knock on effect for later events?
3. The frame time - which is what you currently get.
 */
//case class UpdateEvent(event: GameEvent, time: GameTime)

trait GameEngine[StartupData, StartupError, GameModel] extends JSApp {

  def config: GameConfig

  def assets: Set[AssetType]

  def initialise(assetCollection: AssetCollection): Startup[StartupError, StartupData]

  def initialModel(startupData: StartupData): GameModel

  def updateModel(gameTime: GameTime, state: GameModel): GameEvent => GameModel

  def updateView(currentState: GameModel): SceneGraphRootNode

  private var state: Option[GameModel] = None

  private var animationStates: AnimationStates = AnimationStates(Nil)

  protected var assetCollection: AssetCollection = AssetCollection(Nil, Nil)

  def main(): Unit = {

    if(config.viewport.width % 2 != 0 || config.viewport.height % 2 != 0) {
      println("WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!")
    }

    AssetManager.loadAssets(assets).foreach { ac =>

      assetCollection = ac

      initialise(assetCollection) match {
        case e: StartupFailure[_] =>
          println("Start up failed")
          println(e.report)

        case x: StartupSuccess[StartupData] =>
          val loopFunc = loop(x.success) _

          val canvas = Renderer.createCanvas(config.viewport.width, config.viewport.height)

          WorldEvents(canvas)

          val renderer: IRenderer = Renderer(
            RendererConfig(
              viewport = Viewport(config.viewport.width, config.viewport.height),
              clearColor = config.clearColor,
              magnification = config.magnification
            ),
            assetCollection.images.map(_.toTexture),
            canvas
          )

          dom.window.requestAnimationFrame(loopFunc(renderer, 0))
      }
    }

  }

  private def processUpdateEvents(previousState: GameModel, gameTime: GameTime, remaining: List[GameEvent]): GameModel = {
    remaining match {
      case Nil =>
        updateModel(gameTime, previousState)(FrameTick)

      case x :: xs =>
        processUpdateEvents(updateModel(gameTime, previousState)(x), gameTime, xs)
    }
  }

  private def loop(startupData: StartupData)(renderer: IRenderer, lastUpdateTime: Double): Double => Unit = { time =>
    val timeDelta = time - lastUpdateTime

    val gameTime: GameTime = GameTime(time, timeDelta)

    if(timeDelta > config.frameRateDeltaMillis) {
      val model = state match {
        case None =>
          initialModel(startupData)

        case Some(previousState) =>
          processUpdateEvents(previousState, gameTime, GlobalEventStream.collect)
      }

      state = Some(model)

      val viewUpdateFunc: GameModel => SceneGraphRootNodeInternal =
        updateView _ andThen convertToInternalFormat andThen applyAnimationStates andThen processAnimationCommands(gameTime) andThen persistAnimationStates

      drawScene(renderer, model, viewUpdateFunc)

      dom.window.requestAnimationFrame(loop(startupData)(renderer, time))
    } else {
      dom.window.requestAnimationFrame(loop(startupData)(renderer, lastUpdateTime))
    }
  }

  private val convertToInternalFormat: SceneGraphRootNode => SceneGraphRootNodeInternal = scenegraph =>
    SceneGraphInternal.fromPublicFacing(scenegraph)

  private val applyAnimationStates: SceneGraphRootNodeInternal => SceneGraphRootNodeInternal = sceneGraph =>
    sceneGraph.applyAnimationMemento(animationStates)

  private val processAnimationCommands: GameTime => SceneGraphRootNodeInternal => SceneGraphRootNodeInternal = gameTime => sceneGraph =>
    sceneGraph.runAnimationActions(gameTime)

  private val persistAnimationStates: SceneGraphRootNodeInternal => SceneGraphRootNodeInternal = sceneGraph => {
    animationStates = AnimationState.extractAnimationStates(sceneGraph)
    sceneGraph
  }

  private implicit def displayObjectToList(displayObject: DisplayObject): List[DisplayObject] = List(displayObject)

  private val leafToDisplayObject: SceneGraphNodeLeafInternal => List[DisplayObject] = {
      case leaf: GraphicInternal =>
        DisplayObject(
          x = leaf.x,
          y = leaf.y,
          z = -leaf.depth.zIndex,
          width = leaf.crop.size.x,
          height = leaf.crop.size.y,
          imageRef = leaf.imageAssetRef,
          alpha = leaf.effects.alpha,
          tintR = leaf.effects.tint.r,
          tintG = leaf.effects.tint.g,
          tintB = leaf.effects.tint.b,
          flipHorizontal = leaf.effects.flip.horizontal,
          flipVertical = leaf.effects.flip.vertical,
          frame =
            if(leaf.bounds == leaf.crop) SpriteSheetFrame.defaultOffset
            else
              SpriteSheetFrame.calculateFrameOffset(
                imageSize = Vector2(leaf.bounds.size.x, leaf.bounds.size.y),
                frameSize = Vector2(leaf.crop.size.x, leaf.crop.size.y),
                framePosition = Vector2(leaf.crop.position.x, leaf.crop.position.y)
              )
        )

      case leaf: SpriteInternal =>
        DisplayObject(
          x = leaf.x,
          y = leaf.y,
          z = -leaf.depth.zIndex,
          width = leaf.bounds.size.x,
          height = leaf.bounds.size.y,
          imageRef = leaf.imageAssetRef,
          alpha = leaf.effects.alpha,
          tintR = leaf.effects.tint.r,
          tintG = leaf.effects.tint.g,
          tintB = leaf.effects.tint.b,
          flipHorizontal = leaf.effects.flip.horizontal,
          flipVertical = leaf.effects.flip.vertical,
          frame = SpriteSheetFrame.calculateFrameOffset(
            imageSize = Vector2(leaf.animations.spriteSheetSize.x, leaf.animations.spriteSheetSize.y),
            frameSize = Vector2(leaf.animations.currentFrame.bounds.size.x, leaf.animations.currentFrame.bounds.size.y),
            framePosition = Vector2(leaf.animations.currentFrame.bounds.position.x, leaf.animations.currentFrame.bounds.position.y)
          )
        )

      case leaf: TextInternal =>
        leaf.text.toList.zipWithIndex.map { case (char, index) =>
          val fontChar = leaf.fontInfo.findByCharacter(char.toString)
          val alignmentOffset: Point = leaf.alignment match {
            case AlignLeft =>
              Point(0, 0)

            case AlignCenter =>
              Point(-(leaf.bounds.size.x / 2), 0)

            case AlignRight =>
              Point(-leaf.bounds.size.x, 0)
          }

          DisplayObject(
            x = leaf.position.x + (leaf.fontInfo.charSize.x * index) + alignmentOffset.x,
            y = leaf.position.y,
            z = leaf.depth.zIndex,
            width = leaf.fontInfo.charSize.x,
            height = leaf.fontInfo.charSize.y,
            imageRef = leaf.imageAssetRef,
            alpha = leaf.effects.alpha,
            tintR = leaf.effects.tint.r,
            tintG = leaf.effects.tint.g,
            tintB = leaf.effects.tint.b,
            flipHorizontal = leaf.effects.flip.horizontal,
            flipVertical = leaf.effects.flip.vertical,
            frame = SpriteSheetFrame.calculateFrameOffset(
              imageSize = Vector2(leaf.fontInfo.fontSpriteSheet.size.x, leaf.fontInfo.fontSpriteSheet.size.y),
              frameSize = Vector2(leaf.fontInfo.charSize.x, leaf.fontInfo.charSize.y),
              framePosition = Vector2(fontChar.offset.x, fontChar.offset.y)
            )
          )
        }
    }

  private def convertSceneGraphToDisplayable(rootNode: SceneGraphRootNodeInternal): Displayable =
    Displayable(
      GameDisplayLayer(rootNode.game.node.flatten.flatMap(leafToDisplayObject)),
      LightingDisplayLayer(rootNode.lighting.node.flatten.flatMap(leafToDisplayObject), rootNode.lighting.ambientLight),
      UiDisplayLayer(rootNode.ui.node.flatten.flatMap(leafToDisplayObject))
    )

  private def drawScene(renderer: IRenderer, gameModel: GameModel, update: GameModel => SceneGraphRootNodeInternal): Unit =
    renderer.drawScene(
      convertSceneGraphToDisplayable(
        update(gameModel)
      )
    )

}

case class GameConfig(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int) {
  val frameRateDeltaMillis: Int = 1000 / frameRate
}

case class GameViewport(width: Int, height: Int)

sealed trait Startup[+Error, +Success]
case class StartupFailure[Error](error: Error)(implicit toReportable: ToReportable[Error]) extends Startup[Error, Nothing] {
  def report: String = toReportable.report(error)
}
case class StartupSuccess[Success](success: Success) extends Startup[Nothing, Success]

object Startup {
  implicit def toSuccess[T](v: T): StartupSuccess[T] = StartupSuccess(v)
  implicit def toFailure[T](v: T)(implicit toReportable: ToReportable[T]): StartupFailure[T] = StartupFailure(v)
}

trait ToReportable[T] {
  def report(t: T): String
}

object ToReportable {
  def createToReportable[T](f: T => String): ToReportable[T] =
    new ToReportable[T] {
      def report(t: T): String = f(t)
    }
}