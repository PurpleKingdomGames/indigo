package indigo.shared.events

import indigo.shared.constants.Key
import indigo.shared.audio.Volume
import indigo.shared.datatypes.Point
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetType
import indigo.shared.datatypes.BindingKey
import indigo.shared.config.GameViewport
import indigo.shared.config.RenderingTechnology
import indigo.shared.datatypes.RGBA

/**
  * A trait that tells Indigo to allow this instance into the event loop for the duration of one frame.
  */
trait GlobalEvent

/**
  * A trait whose presence signals that this event should only be routed to subsystems, not the main game.
  */
trait SubSystemEvent extends GlobalEvent

sealed trait ViewEvent extends GlobalEvent with Product with Serializable

/**
  * Tags events for input devices like mice and keyboards.
  * `InputEvent`s work in partnership with `InputState`. Events represent
  * a one time thing that happened since the last frame, while the state
  * represents the _ongoing_ state of an input.
  *
  * For example there is a mouse Move event i.e. "The mouse was moved" and
  * there is also the mouse position on the `InputState` i.e. "Where is the
  * mouse now?"
  */
sealed trait InputEvent extends GlobalEvent with Product with Serializable

/**
  * Event to inform the game which rendering choices are active.
  * For example a view may wish to behave differently depending on the rendering technology available.
  * This event is only fired once during start up.
  *
  * @param renderingTechnology WebGL 1.0 or WebGL 2.0
  * @param clearColor The clear color set during initialisation
  * @param magnification The magnification set during initialisation
  */
final case class RendererDetails(
    renderingTechnology: RenderingTechnology,
    clearColor: RGBA,
    magnification: Int
) extends ViewEvent

/**
  * A special event that happens once per frame, at the end of the frame.
  * Useful for updating anything in your model that "just happens" on every
  * frame without any other prompting event. Like gravity.
  */
case object FrameTick extends GlobalEvent

/**
  * Fired whenever the game window changes size, so that the view can respond.
  *
  * @param gameViewPort The actual size in pixels, you can ask it to apply magnification.
  */
final case class ViewportResize(gameViewPort: GameViewport) extends ViewEvent

/**
  * Attempt to enter or exit full screen mode
  */
case object ToggleFullScreen extends GlobalEvent

/**
  * Attempt to enter full screen mode
  */
case object EnterFullScreen extends GlobalEvent

/**
  * Attempt to exit full screen mode
  */
case object ExitFullScreen extends GlobalEvent

/**
  * The game entered full screen mode
  */
case object FullScreenEntered extends ViewEvent

/**
  * A problem occurred trying to enter full screen
  */
case object FullScreenEnterError extends ViewEvent

/**
  * The game exited full screen mode
  */
case object FullScreenExited extends ViewEvent

/**
  * A problem occurred trying to exit full screen
  */
case object FullScreenExitError extends ViewEvent

/**
  * Represents all mouse events
  */
sealed trait MouseEvent extends InputEvent {
  val x: Int
  val y: Int
  def position: Point = Point(x, y)
}
object MouseEvent {

  /**
    * The mouse has been clicked.
    *
    * @param x X coord relative to magnification level
    * @param y Y coord relative to magnification level
    */
  final case class Click(x: Int, y: Int) extends MouseEvent

  /**
    * The left mouse button was released.
    *
    * @param x X coord relative to magnification level
    * @param y Y coord relative to magnification level
    */
  final case class MouseUp(x: Int, y: Int) extends MouseEvent

  /**
    * The left mouse button was pressed down.
    *
    * @param x X coord relative to magnification level
    * @param y Y coord relative to magnification level
    */
  final case class MouseDown(x: Int, y: Int) extends MouseEvent

  /**
    * The mouse was moved to a new position.
    *
    * @param x X coord relative to magnification level
    * @param y Y coord relative to magnification level
    */
  final case class Move(x: Int, y: Int) extends MouseEvent
}

/**
  * Represents all keyboard events
  */
sealed trait KeyboardEvent extends InputEvent {
  val keyCode: Key
}
object KeyboardEvent {

  /**
    * A key was released during the last frame
    *
    * @param keyCode The code and the JavaScript `String` representation
    */
  final case class KeyUp(keyCode: Key) extends KeyboardEvent

  /**
    * A key was pressed down during the last frame
    *
    * @param keyCode The code and the JavaScript `String` representation
    */
  final case class KeyDown(keyCode: Key) extends KeyboardEvent
}

/**
  * Can be emitted to trigger the one time play back of a sound asset.
  *
  * @param assetName Reference to the loaded asset
  * @param volume What volume level to play at
  */
final case class PlaySound(assetName: AssetName, volume: Volume) extends GlobalEvent

/**
  * A class of events representing outbound communication over a network protocol
  */
trait NetworkSendEvent extends GlobalEvent

/**
  * A class of events representing inbound communication over a network protocol
  */
trait NetworkReceiveEvent extends GlobalEvent

/**
  * Events relating to manipulating locally stored data
  */
sealed trait StorageEvent extends GlobalEvent
object StorageEvent {

  /**
    * Save data locally, referenced by a key.
    *
    * @param key a unique key to store the data against
    * @param data the data to store.
    */
  final case class Save(key: String, data: String) extends StorageEvent

  /**
    * Load command. Request data be loaded from local storage.
    *
    * @param key the unique key to look the data up with.
    */
  final case class Load(key: String) extends StorageEvent

  /**
    * Delete a data entry from local storage
    *
    * @param key the unique key of the data to delete.
    */
  final case class Delete(key: String) extends StorageEvent

  /**
    * Clears all local data
    */
  case object DeleteAll extends StorageEvent

  /**
    * Data load response,
    *
    * @param key the unique key of the data that was loaded.
    * @param data the data retreived from local storage.
    */
  final case class Loaded(key: String, data: String) extends StorageEvent
}

/**
  * Events relating to dynamically loading assets after the game has started.
  *
  * These events are the underlying events used by the `AssetBundleLoader`
  * `SubSystem`, which makes loading assets a slightly more plesant experience.
  */
sealed trait AssetEvent extends GlobalEvent
object AssetEvent {

  /**
    * Load a single asset (alias for `LoadAssetBatch`)
    *
    * You can load assets without a key if you just want them
    * in the asset pool for future use, or you can specify a
    * key so that you can track them as they come in.
    *
    * You can also decide whether to force the assets to be available
    * or not. If they are available, then on load they're immediately
    * added to the asset registers. If not then they are downloaded
    * locally cached, but not added.
    *
    * @param asset `AssetType` information
    * @param key A tracking key.
    * @param makeAvailable Make the asset available to the game, or just download it to local cache.
    */
  final case class LoadAsset(asset: AssetType, key: BindingKey, makeAvailable: Boolean) extends AssetEvent

  /**
    * Load a batch of assets
    *
    * You can load assets without a key if you just want them
    * in the asset pool for future use, or you can specify a
    * key so that you can track them as they come in.
    *
    * You can also decide whether to force the assets to be available
    * or not. If they are available, then on load they're immediately
    * added to the asset registers. If not then they are downloaded
    * locally cached, but not added.
    *
    * @param assets a set of `AssetType`s to load
    * @param key A tracking key.
    * @param makeAvailable Make the asset available to the game, or just download it to local cache.
    */
  final case class LoadAssetBatch(assets: Set[AssetType], key: BindingKey, makeAvailable: Boolean) extends AssetEvent

  /**
    * The response event to `LoadAsset` or `LoadAssetBatch`.
    *
    * @param key The requested tracking key
    * @param available Whether or not the asset has been made available for the game to use.
    */
  final case class AssetBatchLoaded(key: BindingKey, available: Boolean) extends AssetEvent

  /**
    * If an error occurs during load, the game will be sent this event.
    *
    * @param key The requested tracking key so you know which event failed.
    * @param message Asset load error message
    */
  final case class AssetBatchLoadError(key: BindingKey, message: String) extends AssetEvent
}
