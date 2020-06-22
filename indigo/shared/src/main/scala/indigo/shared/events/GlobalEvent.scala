package indigo.shared.events

import indigo.shared.constants.Key
import indigo.shared.audio.Volume
import indigo.shared.datatypes.Point
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetType
import indigo.shared.datatypes.BindingKey
import indigo.shared.config.GameViewport
import indigo.shared.config.RenderingTechnology
import indigo.shared.ClearColor

/**
  * A trait that tells Indigo to allow this instance into the event loop for the duration of one frame.
  */
trait GlobalEvent

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
    clearColor: ClearColor,
    magnification: Int
) extends GlobalEvent

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
final case class ViewportResize(gameViewPort: GameViewport) extends GlobalEvent

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
  final case class Click(x: Int, y: Int)     extends MouseEvent

  /**
    * The left mouse button was released.
    *
    * @param x X coord relative to magnification level
    * @param y Y coord relative to magnification level
    */
  final case class MouseUp(x: Int, y: Int)   extends MouseEvent

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
  final case class Move(x: Int, y: Int)      extends MouseEvent
}

sealed trait KeyboardEvent extends InputEvent {
  val keyCode: Key
}
object KeyboardEvent {
  final case class KeyUp(keyCode: Key)   extends KeyboardEvent
  final case class KeyDown(keyCode: Key) extends KeyboardEvent
}

final case class PlaySound(assetName: AssetName, volume: Volume) extends GlobalEvent

trait NetworkSendEvent    extends GlobalEvent
trait NetworkReceiveEvent extends GlobalEvent

sealed trait StorageEvent extends GlobalEvent

object StorageEvent {
  final case class Save(key: String, data: String) extends StorageEvent
  final case class Load(key: String)               extends StorageEvent
  final case class Delete(key: String)             extends StorageEvent
  final case object DeleteAll                      extends StorageEvent
  final case class Loaded(data: String)            extends StorageEvent
}

sealed trait AssetEvent extends GlobalEvent

object AssetEvent {
  final case class LoadAsset(asset: AssetType, key: Option[BindingKey], makeAvailable: Boolean)            extends AssetEvent
  final case class LoadAssetBatch(assets: Set[AssetType], key: Option[BindingKey], makeAvailable: Boolean) extends AssetEvent
  final case class AssetBatchLoaded(key: Option[BindingKey], available: Boolean)                           extends AssetEvent
  final case class AssetBatchLoadError(key: Option[BindingKey])                                            extends AssetEvent
}
