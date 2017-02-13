package com.purplekingdomgames.indigo.gameengine

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

trait GameEngine[GameModel] extends JSApp {

  def config: GameConfig

  def imageAssets: Set[ImageAsset]

  def initialModel: GameModel

  def updateModel(gameTime: GameTime, state: GameModel): GameEvent => GameModel

  def updateView(currentState: GameModel): SceneGraphNode

  private var state: Option[GameModel] = None

  def main(): Unit = {

    AssetManager.loadAssets(imageAssets.toList).foreach { loadedImageAssets =>

      val canvas = Renderer.createCanvas(config.viewport.width, config.viewport.height)

      WorldEvents(canvas)

      val renderer: Renderer = Renderer(
        RendererConfig(
          viewport = Viewport(config.viewport.width, config.viewport.height),
          clearColor = config.clearColor,
          magnification = config.magnification
        ),
        loadedImageAssets,
        canvas
      )

      dom.window.requestAnimationFrame(loop(renderer, 0))
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

  private def loop(renderer: Renderer, lastUpdateTime: Double)(time: Double): Unit = {
    val timeDelta = time - lastUpdateTime

    if(timeDelta > config.frameRateDeltaMillis) {
      val model = state match {
        case None =>
          initialModel

        case Some(previousState) =>
          processUpdateEvents(previousState, GameTime(time, timeDelta), GlobalEventStream.collect)
      }

      state = Some(model)

      drawScene(renderer, model, updateView)

      dom.window.requestAnimationFrame(loop(renderer, time))
    } else {
      dom.window.requestAnimationFrame(loop(renderer, lastUpdateTime))
    }
  }

  private implicit def displayObjectToList(displayObject: DisplayObject): List[DisplayObject] = List(displayObject)

  private val leafToDisplayObject: SceneGraphNodeLeaf => List[DisplayObject] = {
      case leaf: Graphic =>
        DisplayObject(
          x = leaf.x,
          y = leaf.y,
          z = -leaf.depth.zIndex,
          width = leaf.crop.map(c => c.size.x).getOrElse(leaf.bounds.size.x),
          height = leaf.crop.map(c => c.size.y).getOrElse(leaf.bounds.size.y),
          imageRef = leaf.imageAssetRef,
          alpha = leaf.effects.alpha,
          tintR = leaf.effects.tint.r,
          tintG = leaf.effects.tint.g,
          tintB = leaf.effects.tint.b,
          flipHorizontal = leaf.effects.flip.horizontal,
          flipVertical = leaf.effects.flip.vertical,
          frame = leaf.crop.map { c =>
            SpriteSheetFrame.calculateFrameOffset(
              imageSize = Vector2(leaf.bounds.size.x, leaf.bounds.size.y),
              frameSize = Vector2(c.size.x, c.size.y),
              framePosition = Vector2(c.position.x, c.position.y)
            )
          }.getOrElse(SpriteSheetFrame.defaultOffset)
        )

      case leaf: Sprite =>
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

      case leaf: Text =>
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

  private def drawScene(renderer: Renderer, gameModel: GameModel, update: GameModel => SceneGraphNode): Unit = {
    val sceneGraph: SceneGraphNode = update(gameModel)

    val displayObjects: List[DisplayObject] =
      sceneGraph
        .flatten(Nil)
        .flatMap(leafToDisplayObject)
        .sortBy(d => d.imageRef)

    renderer.drawScene(displayObjects)
  }

}

case class GameConfig(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int) {
  val frameRateDeltaMillis: Int = 1000 / frameRate
}

case class GameViewport(width: Int, height: Int)


