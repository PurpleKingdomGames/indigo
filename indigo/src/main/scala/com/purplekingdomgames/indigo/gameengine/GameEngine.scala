package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.assets._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.renderer._
import com.purplekingdomgames.indigo.util._
import com.purplekingdomgames.indigo.util.metrics._
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import scala.scalajs.js.JSApp

case class GameTime(running: Double, delta: Double)

object GameTime {
  def now: GameTime = GameTime(System.currentTimeMillis(), 0)
  def zero: GameTime = GameTime(0, 0)
  def is(running: Double, delta: Double): GameTime = GameTime(running, delta)
}

trait GameTypeHolder[T] {
  type View = T
}

trait GameEngine[StartupData, StartupError, GameModel, ViewEventDataType] extends JSApp {

  implicit val gameTypeHolder = new GameTypeHolder[ViewEventDataType] {}

  def config: GameConfig

  def assets: Set[AssetType]

  def initialise(assetCollection: AssetCollection): Startup[StartupError, StartupData]

  def initialModel(startupData: StartupData): GameModel

  def updateModel(gameTime: GameTime, gameModel: GameModel): GameEvent => GameModel

  def updateView(gameTime: GameTime, gameModel: GameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate[ViewEventDataType]

  private var state: Option[GameModel] = None

  private var animationStates: AnimationStates = AnimationStates(Nil)

  private implicit val metrics: IMetrics = Metrics.getInstance(config.advanced.recordMetrics, config.advanced.logMetricsReportIntervalMs)

  def main(): Unit = {

    Logger.info("Starting Indigo")
    Logger.info("Configuration: " + config.asString)

    if(config.viewport.width % 2 != 0 || config.viewport.height % 2 != 0) {
      Logger.info("WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!")
    }

    AssetManager.loadAssets(assets).foreach { assetCollection =>

      Logger.info("Asset load complete")

      val textureAtlas = TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height)),
        AssetManager.findByName(assetCollection),
        TextureAtlasFunctions.createAtlasData
      )

      val loadedTextureAssets = textureAtlas.atlases
        .toList
        .map(a => a._2.imageData.map(data => LoadedTextureAsset(a._1.id, data)))
        .collect { case Some(s) => s }

      val assetMapping = AssetMapping(
        mappings =
          textureAtlas.legend
            .map { p =>
              p._1 -> TextureRefAndOffset(
                atlasName = p._2.id.id,
                atlasSize = textureAtlas.atlases.get(p._2.id).map(_.size.value).map(Vector2.apply).getOrElse(Vector2.one),
                offset = p._2.offset
              )
            }
      )

      initialise(assetCollection) match {
        case e: StartupFailure[_] =>
          Logger.info("Game initialisation failed")
          Logger.info(e.report)

        case x: StartupSuccess[StartupData] =>
          Logger.info("Game initialisation succeeded")
          val loopFunc = loop(x.success, assetMapping) _

          val canvas = Renderer.createCanvas(config.viewport.width, config.viewport.height)

          Logger.info("Starting world events")
          WorldEvents(canvas, config.magnification)

          Logger.info("Starting renderer")
          val renderer: IRenderer = Renderer(
            RendererConfig(
              viewport = Viewport(config.viewport.width, config.viewport.height),
              clearColor = config.clearColor,
              magnification = config.magnification
            ),
            loadedTextureAssets,
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

  private def loop(startupData: StartupData, assetMapping: AssetMapping)(renderer: IRenderer, lastUpdateTime: Double): Double => Unit = { time =>
    val timeDelta = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if(timeDelta > config.frameRateDeltaMillis) {

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if(config.advanced.disableSkipModelUpdates || timeDelta < config.haltModelUpdatesAt) {

        metrics.record(UpdateStartMetric)

        val gameTime: GameTime = GameTime(time, timeDelta)

        val collectedEvents = GlobalEventStream.collect

        GlobalSignalsManager.update(collectedEvents)

        metrics.record(CallUpdateGameModelStartMetric)

        val model = state match {
          case None =>
            initialModel(startupData)

          case Some(previousModel) =>
            processModelUpdateEvents(gameTime, previousModel, collectedEvents)
        }

        state = Some(model)

        metrics.record(CallUpdateGameModelEndMetric)
        metrics.record(UpdateEndMetric)

        // View updates cut off
        if(config.advanced.disableSkipViewUpdates || timeDelta < config.haltViewUpdatesAt) {

          metrics.record(CallUpdateViewStartMetric)

          val view = updateView(
            gameTime,
            model,
            FrameInputEvents(collectedEvents.filterNot(_.isInstanceOf[ViewEvent[_]]))
          )

          metrics.record(CallUpdateViewEndMetric)
          metrics.record(ProcessViewStartMetric)

          val processUpdatedView: SceneGraphUpdate[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] =
            persistGlobalViewEvents andThen
              flattenNodes andThen
              persistNodeViewEvents(collectedEvents) andThen
              applyAnimationStates andThen
              processAnimationCommands(gameTime) andThen
              persistAnimationStates

          val processedView: SceneGraphRootNodeFlat[ViewEventDataType] = processUpdatedView(view)

          metrics.record(ProcessViewEndMetric)
          metrics.record(ToDisplayableStartMetric)

          val displayable: Displayable = convertSceneGraphToDisplayable(processedView, assetMapping)

          metrics.record(ToDisplayableEndMetric)
          metrics.record(RenderStartMetric)

          drawScene(renderer, displayable)

          metrics.record(RenderEndMetric)
        } else {
          metrics.record(SkippedViewUpdateMetric)
        }

      } else {
        metrics.record(SkippedModelUpdateMetric)
      }

      metrics.record(FrameEndMetric)

      dom.window.requestAnimationFrame(loop(startupData, assetMapping)(renderer, time))
    } else {
      dom.window.requestAnimationFrame(loop(startupData, assetMapping)(renderer, lastUpdateTime))
    }
  }

  private val persistGlobalViewEvents: SceneGraphUpdate[ViewEventDataType] => SceneGraphRootNode[ViewEventDataType] = update => {
    metrics.record(PersistGlobalViewEventsStartMetric)
    update.viewEvents.foreach(GlobalEventStream.push)
    metrics.record(PersistGlobalViewEventsEndMetric)
    update.rootNode
  }

  private val flattenNodes: SceneGraphRootNode[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = root => root.flatten

  private val persistNodeViewEvents: List[GameEvent] => SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = gameEvents => rootNode => {
    metrics.record(PersistNodeViewEventsStartMetric)
    rootNode.collectViewEvents(gameEvents).foreach(GlobalEventStream.push)
    metrics.record(PersistNodeViewEventsEndMetric)
    rootNode
  }

  private val applyAnimationStates: SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = sceneGraph =>
    sceneGraph.applyAnimationMemento(animationStates)

  private val processAnimationCommands: GameTime => SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = gameTime => sceneGraph =>
    sceneGraph.runAnimationActions(gameTime)

  private val persistAnimationStates: SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = sceneGraph => {
    metrics.record(PersistAnimationStatesStartMetric)

    animationStates = AnimationState.extractAnimationStates(sceneGraph)
    
    metrics.record(PersistAnimationStatesEndMetric)

    sceneGraph
  }

  private def convertSceneGraphToDisplayable(rootNode: SceneGraphRootNodeFlat[ViewEventDataType], assetMapping: AssetMapping): Displayable =
    Displayable(
      GameDisplayLayer(rootNode.game.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject[ViewEventDataType](assetMapping))),
      LightingDisplayLayer(rootNode.lighting.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject[ViewEventDataType](assetMapping)), rootNode.lighting.ambientLight),
      UiDisplayLayer(rootNode.ui.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject[ViewEventDataType](assetMapping)))
    )

  private def drawScene(renderer: IRenderer, displayable: Displayable): Unit =
    renderer.drawScene(displayable)

}