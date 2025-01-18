package indigo

import indigo.entry.StandardFrameProcessor
import indigo.gameengine.GameEngine
import indigo.shared.subsystems.SubSystemsRegister
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.concurrent.Future

/** A trait representing a minimal set of functions to get your game running
  *
  * @example
  *   `object MyGame extends IndigoSandbox[StartUpData, Model]`
  *
  * @tparam StartUpData
  *   The class type representing your successful startup data
  * @tparam Model
  *   The class type representing your game's model
  */
trait IndigoSandbox[StartUpData, Model] extends GameLauncher[StartUpData, Model, Unit] {

  /** Your game's configuration settings.
    */
  def config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts
    */
  def assets: Set[AssetType]

  /** A fixed set of fonts that your game will be able to render
    */
  def fonts: Set[FontInfo]

  /** A fixed set of animations your game will be able to play
    */
  def animations: Set[Animation]

  /** A fixed set of custom shaders you will be able to render with
    */
  def shaders: Set[ShaderProgram]

  /** The `setup` function is your only opportunity to do an initial work to set up your game. For example, perhaps one
    * of your assets was a JSON description of a map or an animation sequence, you could process that now, which is why
    * you have access to the `AssetCollection` object. `setup` is typically only called when new assets are loaded. In a
    * simple game this may only be once, but if assets are dynamically loaded, set up will be called again.
    *
    * @param assetCollection
    *   Access to the Asset collection in order to, for example, parse text files.
    * @param dice
    *   Psuedorandom number generator
    * @return
    *   Return start up data, which can include animations and fonts that could not be declared statically declared.
    */
  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartUpData]]

  /** Set up of your initial model state
    *
    * @param startupData
    *   Access to Startup data in case you need it for the model
    * @return
    *   An instance of your game model
    */
  def initialModel(startupData: StartUpData): Outcome[Model]

  /** A pure function for updating your game's model in the context of the running frame and the events acting upon it.
    *
    * @param context
    *   The context the frame should be produced in, including the time, input state, a dice instance, the state of the
    *   inputs, and a read only reference to your start up data.
    * @param model
    *   The latest version of the model to read from.
    * @return
    *   A function that maps GlobalEvent's to the next version of your model, and encapsuates failures or resulting
    *   events within the Outcome wrapper.
    */
  def updateModel(context: Context[StartUpData], model: Model): GlobalEvent => Outcome[Model]

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
    *   A function that produces a description of what to present next, and encapsuates failures or resulting events
    *   within the Outcome wrapper.
    */
  def present(context: Context[StartUpData], model: Model): Outcome[SceneUpdateFragment]

  private def indigoGame: GameEngine[StartUpData, Model, Unit] = {

    val updateViewModel: (Context[StartUpData], Model, Unit) => GlobalEvent => Outcome[Unit] =
      (_, _, vm) => _ => Outcome(vm)

    val eventFilters: EventFilters =
      EventFilters(
        { case e => Some(e) },
        { case _ => None }
      )

    val frameProcessor: StandardFrameProcessor[StartUpData, Model, Unit] =
      new StandardFrameProcessor(
        new SubSystemsRegister(),
        eventFilters,
        (ctx, m) => (e: GlobalEvent) => updateModel(ctx, m)(e),
        updateViewModel,
        (ctx, m, _) => present(ctx, m)
      )

    new GameEngine[StartUpData, Model, Unit](
      fonts,
      animations,
      shaders,
      (ac: AssetCollection) => (d: Dice) => setup(ac, d),
      (sd: StartUpData) => initialModel(sd),
      (_: StartUpData) => (_: Model) => Outcome(()),
      frameProcessor,
      Batch.empty
    )
  }

  protected def ready(flags: Map[String, String]): Element => GameEngine[StartUpData, Model, Unit] = parentElement =>
    indigoGame.start(parentElement, config, Future(None), assets, Future(Set()), Batch.empty)

}
