package indigo

import indigo.entry.ScenesFrameProcessor
import indigo.gameengine.GameEngine
import indigo.scenes.Scene
import indigo.scenes.SceneManager
import indigo.scenes.SceneName
import indigo.shared.subsystems.SubSystemsRegister
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.concurrent.Future

/** A trait representing a game with scene management baked in
  *
  * @example
  *   `object MyGame extends IndigoGame[BootData, StartUpData, Model, ViewModel]`
  *
  * @tparam BootData
  *   The class type representing you a successful game boot up
  * @tparam StartUpData
  *   The class type representing your successful startup data
  * @tparam Model
  *   The class type representing your game's model
  * @tparam ViewModel
  *   The class type representing your game's view model
  */
trait IndigoGame[BootData, StartUpData, Model, ViewModel] extends GameLauncher[StartUpData, Model, ViewModel] {

  /** A non-empty ordered list of scenes
    *
    * @param bootData
    *   Data created during initial game boot.
    * @return
    *   A list of scenes that ensures at least one scene exists.
    */
  def scenes(bootData: BootData): NonEmptyList[Scene[StartUpData, Model, ViewModel]]

  /** Optional name of the first scene. If None is provided then the first scene is the head of the scenes list.
    *
    * @param bootData
    *   Data created during initial game boot.
    * @return
    *   Optionally return the scene to start the game on, otherwise the first scene is used.
    */
  def initialScene(bootData: BootData): Option[SceneName]

  /** Event filters represent a mapping from events to possible events, and act like a firewall to prevent unnecessary
    * event processing by the model or view model.
    */
  def eventFilters: EventFilters

  /** `boot` provides the initial boot up function for your game, accepting commandline-like arguments and allowing you
    * to declare pre-request assets assets and data that must be in place for your game to get going.
    *
    * @param flags
    *   A simply key-value object/map passed in during initial boot.
    * @return
    *   Bootup data consisting of a custom data type, animations, subsystems, assets, fonts, and the game's config.
    */
  def boot(flags: Map[String, String]): Outcome[BootResult[BootData, Model]]

  /** The `setup` function is your only opportunity to do an initial work to set up your game. For example, perhaps one
    * of your assets was a JSON description of a map or an animation sequence, you could process that now, which is why
    * you have access to the `AssetCollection` object. `setup` is typically only called when new assets are loaded. In a
    * simple game this may only be once, but if assets are dynamically loaded, set up will be called again.
    *
    * @param bootData
    *   Data created during initial game boot.
    * @param assetCollection
    *   Access to the Asset collection in order to, for example, parse text files.
    * @param dice
    *   Pseudorandom number generator
    * @return
    *   Return start up data, which can include animations and fonts that could not be declared at boot time.
    */
  def setup(bootData: BootData, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartUpData]]

  /** Set up of your initial model state
    *
    * @param startupData
    *   Access to Startup data in case you need it for the model
    * @return
    *   An instance of your game model
    */
  def initialModel(startupData: StartUpData): Outcome[Model]

  /** Set up of your initial view model state
    *
    * @param startupData
    *   Access to Startup data in case you need it for the view model
    * @return
    *   An instance of your game's view model
    */
  def initialViewModel(startupData: StartUpData, model: Model): Outcome[ViewModel]

  /** A pure function for updating your game's model in the context of the running frame and the events acting upon it.
    *
    * @param context
    *   The context the frame should be produced in, including the time, input state, a dice instance, the state of the
    *   inputs, and a read only reference to your start up data.
    * @param model
    *   The latest version of the model to read from.
    * @return
    *   A function that maps GlobalEvent's to the next version of your model, and encapsulates failures or resulting
    *   events within the Outcome wrapper.
    */
  def updateModel(context: Context[StartUpData], model: Model): GlobalEvent => Outcome[Model]

  /** A pure function for updating your game's view model in the context of the running frame and the events acting upon
    * it.
    *
    * @param context
    *   The context the frame should be produced in, including the time, input state, a dice instance, the state of the
    *   inputs, and a read only reference to your start up data.
    * @param model
    *   The latest version of the model to read from.
    * @param viewModel
    *   The latest version of the view model to read from.
    * @return
    *   A function that maps GlobalEvent's to the next version of your view model, and encapsulates failures or
    *   resulting events within the Outcome wrapper.
    */
  def updateViewModel(
      context: Context[StartUpData],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel]

  /** A pure function for presenting your game. The result is a side effect free declaration of what you intend to be
    * presented to the player next.
    *
    * @param context
    *   The context the frame should be produced in, including the time, input state, a dice instance, the state of the
    *   inputs, and a read only reference to your start up data.
    * @param model
    *   The latest version of the model to read from.
    * @param viewModel
    *   The latest version of the view model to read from.
    * @return
    *   A function that produces a description of what to present next, and encapsulates failures or resulting events
    *   within the Outcome wrapper.
    */
  def present(context: Context[StartUpData], model: Model, viewModel: ViewModel): Outcome[SceneUpdateFragment]

  private val subSystemsRegister: SubSystemsRegister[Model] =
    new SubSystemsRegister()

  private def indigoGame(bootUp: BootResult[BootData, Model]): GameEngine[StartUpData, Model, ViewModel] = {

    val subSystemEvents = subSystemsRegister.register(Batch.fromSet(bootUp.subSystems))

    val sceneManager: SceneManager[StartUpData, Model, ViewModel] = {
      val s = scenes(bootUp.bootData)

      initialScene(bootUp.bootData) match {
        case Some(name) =>
          SceneManager(s, name)

        case None =>
          SceneManager(s, s.head.name)
      }
    }

    val frameProcessor: ScenesFrameProcessor[StartUpData, Model, ViewModel] =
      new ScenesFrameProcessor(
        subSystemsRegister,
        sceneManager,
        eventFilters,
        updateModel,
        updateViewModel,
        present
      )

    new GameEngine[StartUpData, Model, ViewModel](
      bootUp.fonts,
      bootUp.animations,
      bootUp.shaders,
      (ac: AssetCollection) => (d: Dice) => setup(bootUp.bootData, ac, d),
      (sd: StartUpData) => initialModel(sd),
      (sd: StartUpData) => (m: Model) => initialViewModel(sd, m),
      frameProcessor,
      subSystemEvents
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  protected def ready(flags: Map[String, String]): Element => GameEngine[StartUpData, Model, ViewModel] =
    parentElement =>
      boot(flags) match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("Error during boot - Halting")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result(b, evts) =>
          indigoGame(b).start(parentElement, b.gameConfig, Future(None), b.assets, Future(Set()), evts)

}
