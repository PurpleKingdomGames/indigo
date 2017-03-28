package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import org.scalajs.dom
import com.purplekingdomgames.indigo.renderer._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSApp
import scala.language.implicitConversions

case class GameTime(running: Double, delta: Double)

import com.purplekingdomgames.indigo.Logger

trait GameEngine[StartupData, StartupError, GameModel, ViewEventDataType] extends JSApp {

  def config: GameConfig

  def assets: Set[AssetType]

  def initialise(assetCollection: AssetCollection): Startup[StartupError, StartupData]

  def initialModel(startupData: StartupData): GameModel

  def updateModel(gameTime: GameTime, gameModel: GameModel): GameEvent => GameModel

  def updateView(gameTime: GameTime, gameModel: GameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate[ViewEventDataType]

  private var state: Option[GameModel] = None

  private var animationStates: AnimationStates = AnimationStates(Nil)

  protected var assetCollection: AssetCollection = AssetCollection(Nil, Nil)

  def main(): Unit = {

    Logger.info("Starting Indigo")
    Logger.info("Configuration: " + config.asString)

    if(config.viewport.width % 2 != 0 || config.viewport.height % 2 != 0) {
      Logger.info("WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!")
    }

    AssetManager.loadAssets(assets).foreach { ac =>

      Logger.info("Asset load complete")

      assetCollection = ac

      initialise(assetCollection) match {
        case e: StartupFailure[_] =>
          Logger.info("Game initialisation failed")
          Logger.info(e.report)

        case x: StartupSuccess[StartupData] =>
          Logger.info("Game initialisation succeeded")
          val loopFunc = loop(x.success) _

          val canvas = Renderer.createCanvas(config.viewport.width, config.viewport.height)

          Logger.info("Starting world events")
          WorldEvents(canvas)

          Logger.info("Starting renderer")
          val renderer: IRenderer = Renderer(
            RendererConfig(
              viewport = Viewport(config.viewport.width, config.viewport.height),
              clearColor = config.clearColor,
              magnification = config.magnification
            ),
            assetCollection.images.map(_.toTexture),
            canvas
          )

          Logger.info("Starting main loop, there will be no more log messages.")
          dom.window.requestAnimationFrame(loopFunc(renderer, 0))
      }
    }

  }

  private def processModelUpdateEvents(gameTime: GameTime, previousModel: GameModel, remaining: List[GameEvent]): GameModel = {
    remaining match {
      case Nil =>
        updateModel(gameTime, previousModel)(FrameTick)

      case x :: xs =>
        processModelUpdateEvents(gameTime, updateModel(gameTime, previousModel)(x), xs)
    }
  }

  private def loop(startupData: StartupData)(renderer: IRenderer, lastUpdateTime: Double): Double => Unit = { time =>
    val timeDelta = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if(timeDelta > config.frameRateDeltaMillis) {

      // Model updates cut off
      if(timeDelta < config.haltModelUpdatesAt) {
        val gameTime: GameTime = GameTime(time, timeDelta)

        val collectedEvents = GlobalEventStream.collect

        GlobalSignalsManager.update(collectedEvents)

        val model = state match {
          case None =>
            initialModel(startupData)

          case Some(previousModel) =>
            processModelUpdateEvents(gameTime, previousModel, collectedEvents)
        }

        state = Some(model)

        // View updates cut off
        if(timeDelta < config.haltViewUpdatesAt) {
          val processUpdatedView: SceneGraphUpdate[ViewEventDataType] => SceneGraphRootNodeInternal =
            persistEvents andThen
              convertToInternalFormat andThen
              applyAnimationStates andThen
              processAnimationCommands(gameTime) andThen
              persistAnimationStates

          val view = updateView(
            gameTime,
            model,
            FrameInputEvents(collectedEvents.filterNot(_.isInstanceOf[ViewEvent[_]]))
          )

          drawScene(renderer, model, view, processUpdatedView)
        }
      }

      dom.window.requestAnimationFrame(loop(startupData)(renderer, time))
    } else {
      dom.window.requestAnimationFrame(loop(startupData)(renderer, lastUpdateTime))
    }
  }

  private val persistEvents: SceneGraphUpdate[ViewEventDataType] => SceneGraphRootNode = update => {
    update.viewEvents.foreach(GlobalEventStream.push)
    update.rootNode
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

        val alignmentOffsetX: Rectangle => Int = lineBounds =>
          leaf.alignment match {
            case AlignLeft => 0

            case AlignCenter => -(lineBounds.size.x / 2)

            case AlignRight => -lineBounds.size.x
          }

        val converterFunc: (TextLine, Int, Int) => List[DisplayObject] =
          textLineToDisplayObjects(leaf)

        leaf.lines.foldLeft(0 -> List[DisplayObject]()) { (acc, textLine) =>
          (acc._1 + textLine.lineBounds.height, acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1))
        }._2

    }

  private def textLineToDisplayObjects(leaf: TextInternal): (TextLine, Int, Int) => List[DisplayObject] = (line, alignmentOffsetX, yOffset) =>
    zipWithCharDetails(line.text.toList, leaf.fontInfo).map { case (fontChar, xPosition) =>
      DisplayObject(
        x = leaf.position.x + xPosition + alignmentOffsetX,
        y = leaf.position.y + yOffset,
        z = leaf.depth.zIndex,
        width = fontChar.bounds.width,
        height = fontChar.bounds.height,
        imageRef = leaf.imageAssetRef,
        alpha = leaf.effects.alpha,
        tintR = leaf.effects.tint.r,
        tintG = leaf.effects.tint.g,
        tintB = leaf.effects.tint.b,
        flipHorizontal = leaf.effects.flip.horizontal,
        flipVertical = leaf.effects.flip.vertical,
        frame = SpriteSheetFrame.calculateFrameOffset(
          imageSize = Vector2(leaf.fontInfo.fontSpriteSheet.size.x, leaf.fontInfo.fontSpriteSheet.size.y),
          frameSize = Vector2(fontChar.bounds.width, fontChar.bounds.height),
          framePosition = Vector2(fontChar.bounds.x, fontChar.bounds.y)
        )
      )
    }

  private def zipWithCharDetails(charList: List[Char], fontInfo: FontInfo): List[(FontChar, Int)] = {
    def rec(remaining: List[(Char, FontChar)], nextX: Int, acc: List[(FontChar, Int)]): List[(FontChar, Int)] =
      remaining match {
        case Nil => acc
        case x :: xs => rec(xs, nextX + x._2.bounds.width, (x._2, nextX) :: acc)
      }

    rec(charList.map(c => (c, fontInfo.findByCharacter(c))), 0, Nil)
  }

  private def convertSceneGraphToDisplayable(rootNode: SceneGraphRootNodeInternal): Displayable =
    Displayable(
      GameDisplayLayer(rootNode.game.node.flatten.flatMap(leafToDisplayObject)),
      LightingDisplayLayer(rootNode.lighting.node.flatten.flatMap(leafToDisplayObject), rootNode.lighting.ambientLight),
      UiDisplayLayer(rootNode.ui.node.flatten.flatMap(leafToDisplayObject))
    )

  private def drawScene(renderer: IRenderer, gameModel: GameModel, view: SceneGraphUpdate[ViewEventDataType], processUpdatedView: SceneGraphUpdate[ViewEventDataType] => SceneGraphRootNodeInternal): Unit =
    renderer.drawScene(
      convertSceneGraphToDisplayable(
        processUpdatedView(view)
      )
    )

}

case class GameConfig(viewport: GameViewport, frameRate: Int, clearColor: ClearColor, magnification: Int) {
  val frameRateDeltaMillis: Int = 1000 / frameRate

  val haltViewUpdatesAt: Int = frameRateDeltaMillis * 2
  val haltModelUpdatesAt: Int = frameRateDeltaMillis * 3

  val asString: String =
    s"""
       |Viewpoint:      [${viewport.width}, ${viewport.height}]
       |FPS:            $frameRate
       |frameRateDelta: $frameRateDeltaMillis (view updates stop at: $haltViewUpdatesAt, model at: $haltModelUpdatesAt
       |Clear color:    {red: ${clearColor.r}, green: ${clearColor.g}, blue: ${clearColor.b}, alpha: ${clearColor.a}}
       |Magnification:  $magnification
       |""".stripMargin
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