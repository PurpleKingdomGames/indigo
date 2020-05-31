package indigo

import indigo._
import indigo.scenes.{SceneManager, SceneName, Scene}
import indigo.gameengine.GameEngine
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemsRegister
import indigo.entry.GameWithSubSystems
import indigo.entry.StandardFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A trait representing a game with scene management baked in
  * @example `object MyGame extends IndigoGame`
  */
trait IndigoGame[FlagData, StartupData, Model, ViewModel] extends GameLauncher {

  def parseFlags(flags: Map[String, String]): FlagData

  /**
    * A non-empty ordered list of scenes
    */
  def scenes(flagData: FlagData): NonEmptyList[Scene[Model, ViewModel]]

  /**
    * Optional name of the first scene. If None is provided
    * then the first scene is the head of the scenes list.
    */
  def initialScene(flagData: FlagData): Option[SceneName]

  /**
    * Fixed initial config.
    */
  def config(flagData: FlagData): GameConfig

  /**
    * A Set of assets to be loaded.
    */
  def assets(flagData: FlagData): Set[AssetType]

  /**
    * A Set of FontInfo's describing the fonts for your game.
    * Please note that more fonts can be added to the `Startup` object
    * resulting from the `setup` method below.
    */
  def fonts: Set[FontInfo]

  /**
    * A Set of initial, predefined animations for your game.
    * Please note that more animations can be added to the `Startup` object
    * resulting from the `setup` method below.
    */
  def animations: Set[Animation]

  /**
    * A Set of SubSystems for your game.
    */
  def subSystems: Set[SubSystem]

  /**
    * The `setup` function is your only opportunity to do an initial work
    * to set up your game. For example, perhaps one of your assets was a
    * JSON description of a map or an animation sequence, you could process
    * that now, which is why you have access to the `AssetCollection` object.
    * `setup` is also the only place the game is expected to to potentially
    * fail with error and report any errors.
    * @param assetCollection Access to the Asset collection in order to,
    *                        for example, parse text files.
    * @return Either an `Startup.Success[...your startup data...]` or a
    *         `Startup.Failure[StartupErrors]`.
    */
  def setup(flagData: FlagData, gameConfig: GameConfig, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartupData]

  /**
    * Set up of your initial model
    * @param startupData Access to Startup data in case you need it for the model
    * @return An instance of your game model
    */
  def initialModel(startupData: StartupData): Model

  /**
    * Set up of your initial view model
    * @param startupData Access to Startup data in case you need it for the view model
    * @return An instance of your game's view model
    */
  def initialViewModel(startupData: StartupData, model: Model): ViewModel

  private def indigoGame(flagData: FlagData, gameConfig: GameConfig): GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel] = {
    val sceneManager: SceneManager[Model, ViewModel] = {
      val s = scenes(flagData)

      initialScene(flagData) match {
        case Some(name) =>
          SceneManager(s, name)

        case None =>
          SceneManager(s, s.head.name)
      }
    }

    val frameProcessor: StandardFrameProcessor[GameWithSubSystems[Model], ViewModel] = {
      new StandardFrameProcessor(
        GameWithSubSystems.update(sceneManager.updateModel),
        GameWithSubSystems.updateViewModel(sceneManager.updateViewModel),
        GameWithSubSystems.present(sceneManager.updateView)
      )
    }

    new GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel](
      fonts,
      animations,
      (ac: AssetCollection) => (d: Dice) => setup(flagData, gameConfig, ac, d),
      (sd: StartupData) => new GameWithSubSystems(initialModel(sd), new SubSystemsRegister(subSystems.toList)),
      (sd: StartupData) => (m: GameWithSubSystems[Model]) => initialViewModel(sd, m.model),
      frameProcessor
    )
  }

  final protected def ready(flags: Map[String, String]): Unit = {
    val flagData: FlagData = parseFlags(flags)
    val gameConfig: GameConfig = config(flagData)
    indigoGame(flagData, gameConfig).start(gameConfig, Future(None), assets(flagData), Future(Set()))
  }

}
