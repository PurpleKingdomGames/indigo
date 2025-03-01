package indigo.gameengine

import indigo.platform.Platform
import indigo.platform.assets.*
import indigo.platform.audio.AudioPlayer
import indigo.platform.events.GlobalEventStream
import indigo.platform.input.GamepadInputCaptureImpl
import indigo.platform.renderer.Renderer
import indigo.platform.storage.Storage
import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.Startup
import indigo.shared.animation.*
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetType
import indigo.shared.collections.Batch
import indigo.shared.config.GameConfig
import indigo.shared.datatypes.FontInfo
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.input.GamepadInputCapture
import indigo.shared.platform.AssetMapping
import indigo.shared.platform.SceneProcessor
import indigo.shared.shader.BlendShader
import indigo.shared.shader.EntityShader
import indigo.shared.shader.ShaderProgram
import indigo.shared.shader.ShaderRegister
import indigo.shared.shader.StandardShaders
import indigo.shared.shader.UltravioletShader
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.compiletime.uninitialized
import scala.concurrent.Future

final class GameEngine[StartUpData, GameModel, ViewModel](
    fonts: Set[FontInfo],
    animations: Set[Animation],
    shaders: Set[ShaderProgram],
    initialise: AssetCollection => Dice => Outcome[Startup[StartUpData]],
    initialModel: StartUpData => Outcome[GameModel],
    initialViewModel: StartUpData => GameModel => Outcome[ViewModel],
    frameProccessor: FrameProcessor[StartUpData, GameModel, ViewModel],
    initialisationEvents: Batch[GlobalEvent]
) {

  val animationsRegister: AnimationsRegister =
    new AnimationsRegister()
  val fontRegister: FontRegister =
    new FontRegister()
  val shaderRegister: ShaderRegister =
    new ShaderRegister()

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(animationsRegister, fontRegister)
  val sceneProcessor: SceneProcessor =
    new SceneProcessor(boundaryLocator, animationsRegister, fontRegister)

  val audioPlayer: AudioPlayer =
    AudioPlayer.init

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var gameConfig: GameConfig = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var storage: Storage = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var globalEventStream: GlobalEventStream = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var gamepadInputCapture: GamepadInputCapture = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var gameLoop: Double => Double => Unit = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var gameLoopInstance: GameLoop[StartUpData, GameModel, ViewModel] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var accumulatedAssetCollection: AssetCollection = AssetCollection.empty
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var assetMapping: AssetMapping = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var renderer: Renderer = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var startUpData: StartUpData = uninitialized
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  var platform: Platform = null

  def kill(): Unit =
    platform.kill()
    gameLoopInstance.kill()
    animationsRegister.kill()
    fontRegister.kill()
    shaderRegister.kill()
    boundaryLocator.purgeCache()
    sceneProcessor.purgeCaches()
    audioPlayer.kill()
    globalEventStream.kill()

    ()

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def start(
      parentElement: Element,
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      bootEvents: Batch[GlobalEvent]
  ): GameEngine[StartUpData, GameModel, ViewModel] = {

    IndigoLogger.info("Starting Indigo")

    storage = Storage.default
    globalEventStream = new GlobalEventStream(audioPlayer, storage, platform)
    gamepadInputCapture = GamepadInputCaptureImpl()

    // Intialisation / Boot events
    initialisationEvents.foreach(globalEventStream.pushGlobalEvent)
    bootEvents.foreach(globalEventStream.pushGlobalEvent)

    if (config.advanced.autoLoadStandardShaders)
      StandardShaders.all.foreach(shaderRegister.register)
    else shaderRegister.register(StandardShaders.NormalBlend)

    // Arrange config
    configAsync.map(_.getOrElse(config)).foreach { gc =>
      gameConfig = gc

      IndigoLogger.info("Configuration: " + gameConfig.asString)

      if ((gameConfig.viewport.width % 2 != 0) || (gameConfig.viewport.height % 2 != 0))
        IndigoLogger.info(
          "WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!"
        )

      // Arrange initial asset load
      IndigoLogger.info("Attempting to load assets")

      assetsAsync.flatMap(aa => AssetLoader.loadAssets(aa ++ assets)).foreach { assetCollection =>
        IndigoLogger.info("Asset load complete")

        rebuildGameLoop(parentElement, true)(assetCollection)

        if (gameLoop != null)
          platform.tick(gameLoop(0.0d))
      }

    }

    this
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def rebuildGameLoop(parentElement: Element, firstRun: Boolean): AssetCollection => Unit =
    ac => {
      if (!firstRun) gameLoopInstance.lock()

      fontRegister.clearRegister()
      boundaryLocator.purgeCache()
      sceneProcessor.purgeCaches()

      accumulatedAssetCollection = accumulatedAssetCollection |+| ac

      audioPlayer.addAudioAssets(accumulatedAssetCollection.sounds)

      val dice = if firstRun then Dice.default else Dice.fromSeed(gameLoopInstance.runningTimeReference.toLong)

      if firstRun then platform = new Platform(parentElement, gameConfig, globalEventStream)

      initialise(accumulatedAssetCollection)(dice) match {
        case oe @ Outcome.Error(error, _) =>
          IndigoLogger.error(
            if (firstRun) "Error during first initialisation - Halting."
            else "Error during re-initialisation - Halting."
          )
          IndigoLogger.error("Crash report:")
          IndigoLogger.error(oe.reportCrash)
          throw error

        case Outcome.Result(startupData, globalEvents) =>
          globalEvents.foreach(globalEventStream.pushGlobalEvent)

          GameEngine.registerAnimations(animationsRegister, animations ++ startupData.additionalAnimations)
          GameEngine.registerFonts(fontRegister, fonts ++ startupData.additionalFonts)
          GameEngine.registerShaders(
            shaderRegister,
            shaders ++ startupData.additionalShaders,
            accumulatedAssetCollection
          )

          def modelToUse(startUpSuccessData: => StartUpData): Outcome[GameModel] =
            if (firstRun) initialModel(startUpSuccessData)
            else Outcome(gameLoopInstance.gameModelState)

          def viewModelToUse(startUpSuccessData: => StartUpData, m: GameModel): Outcome[GameModel => ViewModel] =
            if (firstRun) initialViewModel(startUpSuccessData)(m).map(vm => (_: GameModel) => vm)
            else Outcome((_: GameModel) => gameLoopInstance.viewModelState)

          val loop: Outcome[Double => Double => Unit] =
            for {
              rendererAndAssetMapping <- platform.initialise(firstRun, shaderRegister.toSet, accumulatedAssetCollection)
              startUpSuccessData      <- GameEngine.initialisedGame(startupData)
              m                       <- modelToUse(startUpSuccessData)
              vm                      <- viewModelToUse(startUpSuccessData, m)
              initialisedGameLoop <- GameEngine.initialiseGameLoop(
                parentElement,
                this,
                boundaryLocator,
                sceneProcessor,
                gameConfig,
                m,
                vm,
                frameProccessor,
                !firstRun, // If this isn't the first run, start with it frame locked.
                renderer
              )
            } yield {
              renderer = rendererAndAssetMapping._1
              assetMapping = rendererAndAssetMapping._2
              gameLoopInstance = initialisedGameLoop
              startUpData = startUpSuccessData
              initialisedGameLoop.loop
            }

          loop match {
            case Outcome.Result(firstTick, events) =>
              IndigoLogger.info("Starting main loop, there will be no more info log messages.")
              IndigoLogger.info("You may get first occurrence error logs.")

              events.foreach(globalEventStream.pushGlobalEvent)

              gameLoop = firstTick

              gameLoopInstance.unlock()
              ()

            case oe @ Outcome.Error(e, _) =>
              IndigoLogger.error(if (firstRun) "Error during first engine start up" else "Error during engine restart")
              IndigoLogger.error(oe.reportCrash)
              throw e
          }

      }
    }

}

object GameEngine {

  def registerAnimations(animationsRegister: AnimationsRegister, animations: Set[Animation]): Unit =
    animations.foreach(animationsRegister.register)

  def registerFonts(fontRegister: FontRegister, fonts: Set[FontInfo]): Unit =
    fonts.foreach(fontRegister.register)

  def registerShaders(
      shaderRegister: ShaderRegister,
      shaders: Set[ShaderProgram],
      assetCollection: AssetCollection
  ): Unit =
    shaders.foreach {
      case s: EntityShader.Source =>
        shaderRegister.remove(s.id)
        shaderRegister.registerEntityShader(s)

      case s: EntityShader.External =>
        shaderRegister.remove(s.id)
        shaderRegister.registerEntityShader(externalEntityShaderToSource(s, assetCollection))

      case s: BlendShader.Source =>
        shaderRegister.remove(s.id)
        shaderRegister.registerBlendShader(s)

      case s: BlendShader.External =>
        shaderRegister.remove(s.id)
        shaderRegister.registerBlendShader(externalBlendShaderToSource(s, assetCollection))

      case s: UltravioletShader =>
        shaderRegister.remove(s.id)
        shaderRegister.registerUVShader(s)
    }

  def externalEntityShaderToSource(
      external: EntityShader.External,
      assetCollection: AssetCollection
  ): EntityShader.Source =
    EntityShader.Source(
      id = external.id,
      vertex = external.vertex
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-vertex", a))
        .getOrElse(ShaderProgram.defaultVertexProgram),
      fragment = external.fragment
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-fragment", a))
        .getOrElse(ShaderProgram.defaultFragmentProgram),
      prepare = external.prepare
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-prepare", a))
        .getOrElse(ShaderProgram.defaultPrepareProgram),
      light = external.light
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-light", a))
        .getOrElse(ShaderProgram.defaultLightProgram),
      composite = external.composite
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-composite", a))
        .getOrElse(ShaderProgram.defaultCompositeProgram)
    )

  def externalBlendShaderToSource(
      external: BlendShader.External,
      assetCollection: AssetCollection
  ): BlendShader.Source =
    BlendShader.Source(
      id = external.id,
      vertex = external.vertex
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-vertex", a))
        .getOrElse(ShaderProgram.defaultVertexProgram),
      fragment = external.fragment
        .map(a => extractShaderCode(assetCollection.findTextDataByName(a), "indigo-fragment", a))
        .getOrElse(ShaderProgram.defaultFragmentProgram)
    )

  private given CanEqual[Option[String], Option[String]] = CanEqual.derived

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def extractShaderCode(maybeText: Option[String], tag: String, assetName: AssetName): String =
    maybeText.flatMap(s"""//<$tag>\n((.|\n|\r)*)//</$tag>""".r.findFirstIn) match {
      case Some(program) =>
        program

      case None =>
        val msg = s"Error parsing external shader could not match '$tag' tag pair in asset '$assetName' - Halting."
        IndigoLogger.error(msg)
        throw new Exception(msg)
    }

  def initialisedGame[StartUpData](startupData: Startup[StartUpData]): Outcome[StartUpData] =
    startupData match {
      case e: Startup.Failure =>
        IndigoLogger.info("Game initialisation failed")
        IndigoLogger.info(e.report)
        Outcome.raiseError(new Exception("Game aborted due to start up failure"))

      case x: Startup.Success[?] =>
        IndigoLogger.info("Game initialisation succeeded")
        Outcome(x.success)
    }

  def initialiseGameLoop[StartUpData, GameModel, ViewModel](
      parentElement: Element,
      gameEngine: GameEngine[StartUpData, GameModel, ViewModel],
      boundaryLocator: BoundaryLocator,
      sceneProcessor: SceneProcessor,
      gameConfig: GameConfig,
      initialModel: GameModel,
      initialViewModel: GameModel => ViewModel,
      frameProccessor: FrameProcessor[StartUpData, GameModel, ViewModel],
      startFrameLocked: Boolean,
      renderer: => Renderer
  ): Outcome[GameLoop[StartUpData, GameModel, ViewModel]] =
    Outcome(
      new GameLoop[StartUpData, GameModel, ViewModel](
        gameEngine.rebuildGameLoop(parentElement, false),
        boundaryLocator,
        sceneProcessor,
        gameEngine,
        gameConfig,
        initialModel,
        initialViewModel(initialModel),
        frameProccessor,
        startFrameLocked,
        renderer
      )
    )

}
