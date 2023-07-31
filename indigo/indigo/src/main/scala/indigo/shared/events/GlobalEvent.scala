package indigo.shared.events

import indigo.AssetCollection
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetType
import indigo.shared.audio.Volume
import indigo.shared.collections.Batch
import indigo.shared.config.GameViewport
import indigo.shared.config.RenderingTechnology
import indigo.shared.constants.Key
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Size

/** A trait that tells Indigo to allow this instance into the event loop for the duration of one frame.
  */
trait GlobalEvent
object GlobalEvent:
  given CanEqual[GlobalEvent, GlobalEvent] = CanEqual.derived

/** A trait that tells Indigo that an error has occurred
  */
trait GlobalEventError extends GlobalEvent

/** A trait whose presence signals that this event should only be routed to subsystems, not the main game.
  */
trait SubSystemEvent extends GlobalEvent

sealed trait ViewEvent extends GlobalEvent with Product with Serializable

/** Tags events for input devices like mice and keyboards. `InputEvent`s work in partnership with `InputState`. Events
  * represent a one time thing that happened since the last frame, while the state represents the _ongoing_ state of an
  * input.
  *
  * For example there is a mouse Move event i.e. "The mouse was moved" and there is also the mouse position on the
  * `InputState` i.e. "Where is the mouse now?"
  */
sealed trait InputEvent extends GlobalEvent with Product with Serializable

/** Event to inform the game which rendering choices are active. For example a view may wish to behave differently
  * depending on the rendering technology available. This event is only fired once during start up.
  *
  * @param renderingTechnology
  *   WebGL 1.0 or WebGL 2.0
  * @param clearColor
  *   The clear color set during initialisation
  * @param magnification
  *   The magnification set during initialisation
  */
final case class RendererDetails(
    renderingTechnology: RenderingTechnology,
    clearColor: RGBA,
    magnification: Int
) extends ViewEvent

/** A special event that happens once per frame, at the end of the frame. Useful for updating anything in your model
  * that "just happens" on every frame without any other prompting event. Like gravity.
  */
case object FrameTick extends GlobalEvent

/** Fired whenever the game window changes size, so that the view can respond.
  *
  * @param gameViewPort
  *   The actual size in pixels, you can ask it to apply magnification.
  */
final case class ViewportResize(gameViewPort: GameViewport) extends ViewEvent

/** Attempt to enter or exit full screen mode
  */
case object ToggleFullScreen extends GlobalEvent

/** Attempt to enter full screen mode
  */
case object EnterFullScreen extends GlobalEvent

/** Attempt to exit full screen mode
  */
case object ExitFullScreen extends GlobalEvent

/** The game entered full screen mode
  */
case object FullScreenEntered extends ViewEvent

/** A problem occurred trying to enter full screen
  */
case object FullScreenEnterError extends ViewEvent with GlobalEventError

/** The game exited full screen mode
  */
case object FullScreenExited extends ViewEvent

/** A problem occurred trying to exit full screen
  */
case object FullScreenExitError extends ViewEvent with GlobalEventError

/** The application has received focus
  */
case object ApplicationGainedFocus extends GlobalEvent

/** The game canvas has received focus
  */
case object CanvasGainedFocus extends GlobalEvent

/** The application has lost focus
  */
case object ApplicationLostFocus extends GlobalEvent

/** The game canvas has lost focus
  */
case object CanvasLostFocus extends GlobalEvent

/** Follows the MDN spec values https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/button Relies on the ordinal
  * behavior of Scala 3 enums to match the button number
  */
enum MouseButton derives CanEqual:
  case LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton

enum PointerType derives CanEqual:
  case Mouse, Pen, Touch, Unknown

/** Represents in which direction the mouse wheel was rotated
  */
enum MouseWheel derives CanEqual:
  case ScrollUp, ScrollDown

object MouseButton:
  def fromOrdinalOpt(ordinal: Int): Option[MouseButton] =
    if ordinal >= LeftMouseButton.ordinal && ordinal <= BrowserForwardButton.ordinal then
      Some(MouseButton.fromOrdinal(ordinal))
    else Option.empty[MouseButton]

trait MouseOrPointerEvent:
  /** Coordinates relative to the magnification level
    */
  def position: Point

  /** The X position relative to the magnification level
    */
  def x: Int = position.x

  /** The Y position relative to the magnification level
    */
  def y: Int = position.y

  /** Pressed buttons
    */
  def buttons: Batch[MouseButton]

  /** Indicates whether buttons are in active state
    */
  def isActive: Boolean = buttons.isEmpty == false

  /** Whether the `alt` key was pressed when the event was fired
    */
  def isAltKeyDown: Boolean

  /** Whether the `ctrl` key was pressed when the event was fired
    */
  def isCtrlKeyDown: Boolean

  /** Whether the meta button (Windows key, or Cmd Key) key was pressed when the event was fired
    */
  def isMetaKeyDown: Boolean

  /** Whether the `shift` key was pressed when the event was fired
    */
  def isShiftKeyDown: Boolean

  /** The delta position between this event and the last event relative to the magnification level
    */
  def movementPosition: Point

  /** The delta X position between this event and the last event relative to the magnification level
    */
  def movementX: Int = movementPosition.x

  /** The delta Y position between this event and the last event relative to the magnification level
    */
  def movementY: Int = movementPosition.y

/** Represents all mouse events
  */
sealed trait MouseEvent extends InputEvent with MouseOrPointerEvent
object MouseEvent:

  /** The mouse has been clicked.
    *
    * @param button
    *   The button that was used for the click
    */
  final case class Click(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object Click:
    def apply(x: Int, y: Int): Click =
      Click(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(position: Point): Click =
      Click(
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )

  /** The mouse button was released.
    * @param button
    *   The button that was released
    */
  final case class MouseUp(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object MouseUp:
    def apply(position: Point): MouseUp =
      MouseUp(
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int): MouseUp =
      MouseUp(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int, button: MouseButton): MouseUp =
      MouseUp(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = button
      )

  /** The mouse button was pressed down.
    * @param button
    *   The button that was pressed down
    */
  final case class MouseDown(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object MouseDown:
    def apply(position: Point): MouseDown =
      MouseDown(
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int): MouseDown =
      MouseDown(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int, button: MouseButton): MouseDown =
      MouseDown(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = button
      )

  /** The mouse was moved to a new position.
    */
  final case class Move(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point
  ) extends MouseEvent
  object Move:
    def apply(x: Int, y: Int): Move =
      Move(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero
      )

  /** Mouse has moved into canvas hit test boundaries. It's counterpart is [[Leave]].
    */
  final case class Enter(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point
  ) extends MouseEvent

  /** Mouse has left canvas hit test boundaries. It's counterpart is [[Enter]].
    */
  final case class Leave(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point
  ) extends MouseEvent

  /** The mouse wheel was rotated a certain amount into the Y axis.
    *
    * @param amount
    *   vertical amount of pixels, pages or other unit, depending on delta mode, the Y axis was scrolled
    */
  final case class Wheel(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      amount: Double
  ) extends MouseEvent
  object Wheel:
    def apply(x: Int, y: Int, amount: Double): Wheel =
      Wheel(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        amount = amount
      )

end MouseEvent

/** Represents all mouse, pen and touch events
  */
sealed trait PointerEvent extends InputEvent with MouseOrPointerEvent:
  import PointerEvent.*

  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** The width (magnitude on the X axis), of the contact geometry of the pointer relative to the magnification level
    */
  def width: Int

  /** The height (magnitude on the Y axis), of the contact geometry of the pointer relative to the magnification level
    */
  def height: Int

  /** The normalized pressure of the pointer input in the range 0 to 1, where 0 and 1 represent the minimum and maximum
    * pressure the hardware is capable of detecting, respectively.
    */
  def pressure: Double

  /** The normalized tangential pressure of the pointer input (also known as barrel pressure or cylinder stress) in the
    * range -1 to 1, where 0 is the neutral position of the control.
    */
  def tangentialPressure: Double

  /** The plane angle (in radians, in the range of -1.570796 to 1.570796 (-90 - 90 degrees)) between the Y–Z plane and
    * the plane containing both the pointer (e.g. pen stylus) axis and the Y axis.
    */
  def tiltX: Radians

  /** The plane angle (in radians, in the range of -1.570796 to 1.570796 (-90 - 90 degrees)) between the X–Z plane and
    * the plane containing both the pointer (e.g. pen stylus) axis and the X axis.
    */
  def tiltY: Radians

  /** The clockwise rotation of the pointer (e.g. pen stylus) around its major axis in degrees, with a value in the
    * range 0 to 6.265732 (0 to 359 degrees)
    */
  def twist: Radians

  /** Indicates the device type that caused the event (mouse, pen, touch, etc.)
    */
  def pointerType: PointerType

  /** Indicates whether the pointer is considered primary - like first finger during multi-touch gesture
    */
  def isPrimary: Boolean

object PointerEvent:
  /** Unique pointer identifier. Could be used to distinguish between pointers in multi-touch interactions
    */
  opaque type PointerId = Double
  object PointerId:
    inline def apply(id: Double): PointerId = id

    given CanEqual[PointerId, PointerId] = CanEqual.derived

  /** Pointing device is moved into canvas hit test boundaries. It's counterpart is [[PointerLeave]].
    */
  final case class PointerEnter(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      pointerId: PointerId,
      width: Int,
      height: Int,
      pressure: Double,
      tangentialPressure: Double,
      tiltX: Radians,
      tiltY: Radians,
      twist: Radians,
      pointerType: PointerType,
      isPrimary: Boolean
  ) extends PointerEvent

  /** Pointing device left canvas hit test boundaries. It's counterpart is [[PointerEnter]].
    */
  final case class PointerLeave(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      pointerId: PointerId,
      width: Int,
      height: Int,
      pressure: Double,
      tangentialPressure: Double,
      tiltX: Radians,
      tiltY: Radians,
      twist: Radians,
      pointerType: PointerType,
      isPrimary: Boolean
  ) extends PointerEvent

  /** Pointing device is in active buttons state.
    */
  final case class PointerDown(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      pointerId: PointerId,
      width: Int,
      height: Int,
      pressure: Double,
      tangentialPressure: Double,
      tiltX: Radians,
      tiltY: Radians,
      twist: Radians,
      pointerType: PointerType,
      isPrimary: Boolean,
      button: Option[MouseButton]
  ) extends PointerEvent

  /** Pointing device is no longer in active buttons state.
    */
  final case class PointerUp(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      pointerId: PointerId,
      width: Int,
      height: Int,
      pressure: Double,
      tangentialPressure: Double,
      tiltX: Radians,
      tiltY: Radians,
      twist: Radians,
      pointerType: PointerType,
      isPrimary: Boolean,
      button: Option[MouseButton]
  ) extends PointerEvent

  /** Pointing device changed coordinates.
    */
  final case class PointerMove(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      pointerId: PointerId,
      width: Int,
      height: Int,
      pressure: Double,
      tangentialPressure: Double,
      tiltX: Radians,
      tiltY: Radians,
      twist: Radians,
      pointerType: PointerType,
      isPrimary: Boolean
  ) extends PointerEvent

  /** The ongoing interactions was cancelled due to:
    *   - the pointer device being disconnected
    *   - device orientation change
    *   - palm rejection
    *   - the browser taking over the manipulations like scroll, drag & drop, pinch & zoom or other
    */
  final case class PointerCancel(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      pointerId: PointerId,
      width: Int,
      height: Int,
      pressure: Double,
      tangentialPressure: Double,
      tiltX: Radians,
      tiltY: Radians,
      twist: Radians,
      pointerType: PointerType,
      isPrimary: Boolean
  ) extends PointerEvent

/** Represents all keyboard events
  */
sealed trait KeyboardEvent extends InputEvent {
  val keyCode: Key
}
object KeyboardEvent {

  /** A key was released during the last frame
    *
    * @param keyCode
    *   The code and the JavaScript `String` representation
    */
  final case class KeyUp(keyCode: Key) extends KeyboardEvent

  /** A key was pressed down during the last frame
    *
    * @param keyCode
    *   The code and the JavaScript `String` representation
    */
  final case class KeyDown(keyCode: Key) extends KeyboardEvent
}

/** Can be emitted to trigger the one time play back of a sound asset.
  *
  * @param assetName
  *   Reference to the loaded asset
  * @param volume
  *   What volume level to play at
  */
final case class PlaySound(assetName: AssetName, volume: Volume) extends GlobalEvent

/** A class of events representing general networking events
  */
sealed trait NetworkEvent extends GlobalEvent
object NetworkEvent:
  /** The network has come online and is now available
    */
  case object Online extends NetworkEvent

  /** The network has gone offline and is now unavailable
    */
  case object Offline extends NetworkEvent

/** A class of events representing outbound communication over a network protocol
  */
trait NetworkSendEvent extends GlobalEvent

/** A class of events representing inbound communication over a network protocol
  */
trait NetworkReceiveEvent extends GlobalEvent

/** Events relating to manipulating locally stored data
  */

enum StorageActionType:
  case Save, Load, Delete, Find

enum StorageKey:
  case Index(value: Int)
  case Key(value: String)

sealed trait StorageEventError extends GlobalEventError:
  /** The identifier of the storage item accessed. Either the index (an Int), or the key (a String)
    */
  val key: Option[StorageKey]

  /** The way the storage was being accessed when the error occurred
    */
  val actionType: StorageActionType

object StorageEventError {

  /** An error was experienced denoting that there is not enough room on the device
    * @param key
    *   The identifier of the storage item accessed. Either the index (an Int), or the key (a String)
    * @param actionType
    *   The way the storage was being accessed when the error occurred
    */
  final case class QuotaExceeded(key: Option[StorageKey], actionType: StorageActionType) extends StorageEventError

  /** An error was experienced denoting that there were not enough permissions granted by the user that allows access to
    * the storage
    *
    * @param key
    *   The identifier of the storage item accessed. Either the index (an Int), or the key (a String)
    * @param actionType
    *   The way the storage was being accessed when the error occurred
    */
  final case class InvalidPermissions(key: Option[StorageKey], actionType: StorageActionType) extends StorageEventError

  /** An error was experienced denoting that the particular storage feature is not available
    *
    * @param key
    *   The identifier of the storage item accessed. Either the index (an Int), or the key (a String)
    * @param actionType
    *   The way the storage was being accessed when the error occurred
    */
  final case class FeatureNotAvailable(key: Option[StorageKey], actionType: StorageActionType) extends StorageEventError

  object FeatureNotAvailable:
    def apply(key: String | Int, actionType: StorageActionType): FeatureNotAvailable =
      key match {
        case i: Int    => FeatureNotAvailable(Some(StorageKey.Index(i)), actionType)
        case s: String => FeatureNotAvailable(Some(StorageKey.Key(s)), actionType)
      }

  /** An error was experienced that did not fall into one of the predefined categories
    *
    * @param key
    *   The identifier of the storage item accessed. Either the index (an Int), or the key (a String)
    * @param actionType
    *   The way the storage was being accessed when the error occurred
    * @param message
    *   The message of the error that was experienced
    */
  final case class Unspecified(key: Option[StorageKey], actionType: StorageActionType, message: String)
      extends StorageEventError
}

sealed trait StorageEvent extends GlobalEvent
object StorageEvent {

  /** Return the name of the key at the given index
    *
    * @param index
    *   the index to check
    */
  final case class FetchKeyAt(index: Int) extends StorageEvent

  /** Key check response.
    *
    * @param index
    *   the index checked
    * @param key
    *   the unique key name found at the index position.
    */
  final case class KeyFoundAt(index: Int, key: Option[String]) extends StorageEvent

  /** Return the name of the key at the given index
    *
    * @param index
    *   the index to check
    */
  final case class FetchKeys(from: Int, to: Int) extends StorageEvent

  /** Key check response.
    *
    * @param found
    *   list of (index -> maybe key)
    */
  final case class KeysFound(found: List[(Int, Option[String])]) extends StorageEvent

  /** Save data locally, referenced by a key.
    *
    * @param key
    *   a unique key to store the data against
    * @param data
    *   the data to store.
    */
  final case class Save(key: String, data: String) extends StorageEvent

  /** Load command. Request data be loaded from local storage.
    *
    * @param key
    *   the unique key to look the data up with.
    */
  final case class Load(key: String) extends StorageEvent

  /** Delete a data entry from local storage
    *
    * @param key
    *   the unique key of the data to delete.
    */
  final case class Delete(key: String) extends StorageEvent

  /** Clears all local data
    */
  case object DeleteAll extends StorageEvent

  /** Successful data load response.
    *
    * @param key
    *   the unique key of the data that was loaded.
    * @param data
    *   the data retrieved from local storage, if it exists.
    */
  final case class Loaded(key: String, data: Option[String]) extends StorageEvent
}

/** Events relating to dynamically loading assets after the game has started.
  *
  * These events are the underlying events used by the `AssetBundleLoader` `SubSystem`, which makes loading assets a
  * slightly more pleasant experience.
  */
sealed trait AssetEvent extends GlobalEvent
object AssetEvent {

  /** Load a single asset (alias for `LoadAssetBatch`)
    *
    * You can load assets without a key if you just want them in the asset pool for future use, or you can specify a key
    * so that you can track them as they come in.
    *
    * You can also decide whether to force the assets to be available or not. If they are available, then on load
    * they're immediately added to the asset registers. If not then they are downloaded locally cached, but not added.
    *
    * @param asset
    *   `AssetType` information
    * @param key
    *   A tracking key.
    * @param makeAvailable
    *   Make the asset available to the game, or just download it to local cache.
    */
  final case class LoadAsset(asset: AssetType, key: BindingKey, makeAvailable: Boolean) extends AssetEvent

  /** Load a batch of assets
    *
    * You can load assets without a key if you just want them in the asset pool for future use, or you can specify a key
    * so that you can track them as they come in.
    *
    * You can also decide whether to force the assets to be available or not. If they are available, then on load
    * they're immediately added to the asset registers. If not then they are downloaded locally cached, but not added.
    *
    * @param assets
    *   a set of `AssetType`s to load
    * @param key
    *   A tracking key.
    * @param makeAvailable
    *   Make the asset available to the game, or just download it to local cache.
    */
  final case class LoadAssetBatch(assets: Set[AssetType], key: BindingKey, makeAvailable: Boolean) extends AssetEvent

  /** The response event to `LoadAsset` or `LoadAssetBatch`.
    *
    * @param key
    *   The requested tracking key
    * @param available
    *   Whether or not the asset has been made available for the game to use.
    */
  final case class AssetBatchLoaded(key: BindingKey, available: Boolean) extends AssetEvent

  /** If an error occurs during load, the game will be sent this event.
    *
    * @param key
    *   The requested tracking key so you know which event failed.
    * @param message
    *   Asset load error message
    */
  final case class AssetBatchLoadError(key: BindingKey, message: String) extends AssetEvent
}

enum IndigoSystemEvent extends GlobalEvent:
  case Rebuild(assetCollection: AssetCollection)
