package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.assets._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.renderer._
import com.purplekingdomgames.indigo.util._
import com.purplekingdomgames.indigo.util.metrics._
import com.purplekingdomgames.shared.{AssetType, GameConfig}
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class GameTime(running: Double, delta: Double)

object GameTime {
  def now: GameTime = GameTime(System.currentTimeMillis().toDouble, 0)
  def zero: GameTime = GameTime(0, 0)
  def is(running: Double, delta: Double): GameTime = GameTime(running, delta)
}

trait GameTypeHolder[T] {
  type View = T
}

class GameEngine[StartupData, StartupError, GameModel, ViewEventDataType](config: GameConfig,
                                                                          configAsync: Future[Option[GameConfig]],
                                                                          assets: Set[AssetType],
                                                                          assetsAsync: Future[Set[AssetType]],
                                                                          initialise: AssetCollection => Startup[StartupError, StartupData],
                                                                          initialModel: StartupData => GameModel,
                                                                          updateModel: (GameTime, GameModel) => GameEvent => GameModel,
                                                                          updateView: (GameTime, GameModel, FrameInputEvents) => SceneGraphUpdate[ViewEventDataType]) {

  implicit val gameTypeHolder: GameTypeHolder[ViewEventDataType] = new GameTypeHolder[ViewEventDataType] {}

  private var state: Option[GameModel] = None

  private var animationStates: AnimationStates = AnimationStates(Nil)

  def start(): Unit = {

    configAsync.map(_.getOrElse(config)).foreach { gameConfig =>

      implicit val metrics: IMetrics = Metrics.getInstance(gameConfig.advanced.recordMetrics, gameConfig.advanced.logMetricsReportIntervalMs)

      Logger.info("Starting Indigo")
      Logger.info("Configuration: " + gameConfig.asString)

      if(gameConfig.viewport.width % 2 != 0 || gameConfig.viewport.height % 2 != 0) {
        Logger.info("WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!")
      }

      assetsAsync.flatMap(aa => AssetManager.loadAssets(aa ++ assets)).foreach { assetCollection =>

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
            val loopFunc = loop(gameConfig, x.success, assetMapping) _

            val canvas = Renderer.createCanvas(gameConfig.viewport.width, gameConfig.viewport.height)

            Logger.info("Starting world events")
            WorldEvents(canvas, gameConfig.magnification)

            Logger.info("Starting renderer")
            val renderer: IRenderer = Renderer(
              RendererConfig(
                viewport = Viewport(gameConfig.viewport.width, gameConfig.viewport.height),
                clearColor = gameConfig.clearColor,
                magnification = gameConfig.magnification
              ),
              loadedTextureAssets,
              canvas
            )

            Logger.info("Starting main loop, there will be no more log messages.")
            dom.window.requestAnimationFrame(loopFunc(renderer, 0))
        }
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

  private def loop(gameConfig: GameConfig, startupData: StartupData, assetMapping: AssetMapping)(renderer: IRenderer, lastUpdateTime: Double)(implicit metrics: IMetrics): Double => Int = { time =>
    val timeDelta = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if(timeDelta > gameConfig.frameRateDeltaMillis) {

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if(gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

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
        if(gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt) {

          metrics.record(CallUpdateViewStartMetric)

          val view = updateView(
            gameTime,
            model,
            FrameInputEvents(collectedEvents.filterNot(_.isInstanceOf[ViewEvent[_]]))
          )

          metrics.record(CallUpdateViewEndMetric)
          metrics.record(ProcessViewStartMetric)

          val processUpdatedView: SceneGraphUpdate[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] =
            persistGlobalViewEvents(metrics) andThen
              flattenNodes andThen
              persistNodeViewEvents(metrics)(collectedEvents) andThen
              applyAnimationStates(metrics) andThen
              processAnimationCommands(metrics)(gameTime) andThen
              persistAnimationStates(metrics)

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

      dom.window.requestAnimationFrame(loop(gameConfig, startupData, assetMapping)(renderer, time))
    } else {
      dom.window.requestAnimationFrame(loop(gameConfig, startupData, assetMapping)(renderer, lastUpdateTime))
    }
  }

  private val persistGlobalViewEvents: IMetrics => SceneGraphUpdate[ViewEventDataType] => SceneGraphRootNode[ViewEventDataType] = metrics => update => {
    metrics.record(PersistGlobalViewEventsStartMetric)
    update.viewEvents.foreach(GlobalEventStream.push)
    metrics.record(PersistGlobalViewEventsEndMetric)
    update.rootNode
  }

  private val flattenNodes: SceneGraphRootNode[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = root => root.flatten

  private val persistNodeViewEvents: IMetrics => List[GameEvent] => SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = metrics => gameEvents => rootNode => {
    metrics.record(PersistNodeViewEventsStartMetric)
    rootNode.collectViewEvents(gameEvents).foreach(GlobalEventStream.push)
    metrics.record(PersistNodeViewEventsEndMetric)
    rootNode
  }

  private val applyAnimationStates: IMetrics => SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = metrics => sceneGraph =>
    sceneGraph.applyAnimationMemento(animationStates)(metrics)

  private val processAnimationCommands: IMetrics => GameTime => SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = metrics => gameTime => sceneGraph =>
    sceneGraph.runAnimationActions(gameTime)(metrics)

  private val persistAnimationStates: IMetrics => SceneGraphRootNodeFlat[ViewEventDataType] => SceneGraphRootNodeFlat[ViewEventDataType] = metrics => sceneGraph => {
    metrics.record(PersistAnimationStatesStartMetric)

    animationStates = AnimationState.extractAnimationStates(sceneGraph)
    
    metrics.record(PersistAnimationStatesEndMetric)

    sceneGraph
  }

  private def convertSceneGraphToDisplayable(rootNode: SceneGraphRootNodeFlat[ViewEventDataType], assetMapping: AssetMapping): Displayable =
    Displayable(
      GameDisplayLayer(
        rootNode.game.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject[ViewEventDataType](assetMapping))
      ),
      LightingDisplayLayer(
        rootNode.lighting.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject[ViewEventDataType](assetMapping)),
        rootNode.lighting.ambientLight
      ),
      UiDisplayLayer(
        rootNode.ui.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject[ViewEventDataType](assetMapping))
      )
    )

  private def drawScene(renderer: IRenderer, displayable: Displayable)(implicit metrics: IMetrics): Unit =
    renderer.drawScene(displayable)

}