package indigo

import indigo.scenes.{SceneManager, SceneName, Scene}
import indigo.gameengine.GameEngine
import indigo.shared.subsystems.SubSystemsRegister
import indigo.entry.ScenesFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A trait representing a game with scene management baked in
  * @example `object MyGame extends IndigoGame`
  */
trait IndigoGame[BootData, StartUpData, Model, ViewModel] extends GameLauncher {

  /**
    * A non-empty ordered list of scenes
    * @param flags A simply key-value object/map passed in during initial boot.
    * @return Bootup data consisting of a custom data type, animations, subsytems,
    *         assets, fonts, and the games config.
    */
  def boot(flags: Map[String, String]): BootResult[BootData]

  /**
    * A non-empty ordered list of scenes
    * @param bootData Data created during initial game boot.
    * @return A list of scenes that ensures at least one scene exists.
    */
  def scenes(bootData: BootData): NonEmptyList[Scene[StartUpData, Model, ViewModel]]

  /**
    * Optional name of the first scene. If None is provided
    * then the first scene is the head of the scenes list.
    * @param bootData Data created during initial game boot.
    * @return Optionally return the scene to start the game on,
    *         otherwise the first scene is used.
    */
  def initialScene(bootData: BootData): Option[SceneName]

  /**
    * The `setup` function is your only opportunity to do an initial work
    * to set up your game. For example, perhaps one of your assets was a
    * JSON description of a map or an animation sequence, you could process
    * that now, which is why you have access to the `AssetCollection` object.
    * `setup` is also the only place the game is expected to to potentially
    * fail with error and report any errors.
    * @param bootData Data created during initial game boot.
    * @param assetCollection Access to the Asset collection in order to,
    *                        for example, parse text files.
    * @param dice Psuedorandom number generator
    * @return Either an `Startup.Success[...your startup data...]` or a
    *         `Startup.Failure[StartupErrors]`.
    */
  def setup(bootData: BootData, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartUpData]

  /**
    * Set up of your initial model
    * @param startupData Access to Startup data in case you need it for the model
    * @return An instance of your game model
    */
  def initialModel(startupData: StartUpData): Model

  /**
    * Set up of your initial view model
    * @param startupData Access to Startup data in case you need it for the view model
    * @return An instance of your game's view model
    */
  def initialViewModel(startupData: StartUpData, model: Model): ViewModel

  private val subSystemsRegister: SubSystemsRegister =
    new SubSystemsRegister(Nil)

  private def indigoGame(bootUp: BootResult[BootData]): GameEngine[StartUpData, StartupErrors, Model, ViewModel] = {
    subSystemsRegister.register(bootUp.subSystems.toList)

    val sceneManager: SceneManager[StartUpData, Model, ViewModel] = {
      val s = scenes(bootUp.bootData)

      initialScene(bootUp.bootData) match {
        case Some(name) =>
          SceneManager(s, name)

        case None =>
          SceneManager(s, s.head.name)
      }
    }

    val frameProcessor: ScenesFrameProcessor[StartUpData, Model, ViewModel] = {
      new ScenesFrameProcessor(
        subSystemsRegister,
        sceneManager
      )
    }

    new GameEngine[StartUpData, StartupErrors, Model, ViewModel](
      bootUp.fonts,
      bootUp.animations,
      (ac: AssetCollection) => (d: Dice) => setup(bootUp.bootData, ac, d),
      (sd: StartUpData) => initialModel(sd),
      (sd: StartUpData) => (m: Model) => initialViewModel(sd, m),
      frameProcessor
    )
  }

  final protected def ready(flags: Map[String, String]): Unit = {
    val b = boot(flags)
    indigoGame(b).start(b.gameConfig, Future(None), b.assets, Future(Set()))
  }

}
