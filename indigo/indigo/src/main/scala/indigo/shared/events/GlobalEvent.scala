package indigo.shared.events

import indigo.AssetCollection
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetType
import indigo.shared.audio.PlaybackPolicy
import indigo.shared.audio.Volume
import indigo.shared.collections.Batch
import indigo.shared.config.GameViewport
import indigo.shared.config.RenderingTechnology
import indigo.shared.constants.Key
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.events.MouseEvent.Move
import indigo.shared.events.MouseEvent.Wheel

import scala.annotation.nowarn

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

/** Represents in which direction the wheel input was rotated
  */
enum WheelDirection derives CanEqual:
  case Up, Down, Left, Right

/** Represents a wheel event, such as a mouse wheel or touchpad scroll
  */
sealed trait WheelEvent extends InputEvent
object WheelEvent:
  enum DeltaMode derives CanEqual:
    /** The delta values are in pixels */
    case Pixel

    /** The delta values are in lines */
    case Line

    /** The delta values are in pages */
    case Page

  /** Represents a wheel event that has moved in one or more directions. This event is always fired when the wheel is
    * moved, along with it's helper counterparts (Vertical, Horizontal, Depth)
    *
    * @param deltaX
    * @param deltaY
    * @param deltaZ
    * @param deltaMode
    */
  final case class Move(deltaX: Double, deltaY: Double, deltaZ: Double, deltaMode: DeltaMode) extends WheelEvent

  object Move:
    def apply(deltaX: Double, deltaY: Double, deltaZ: Double): Move =
      Move(deltaX, deltaY, deltaZ, DeltaMode.Pixel)

  /** Represents a wheel event that has moved vertically, i.e. up or down
    *
    * @param deltaY
    * @param deltaMode
    */
  final case class Vertical(deltaY: Double, deltaMode: DeltaMode) extends WheelEvent {
    val direction =
      if deltaY < 0 then WheelDirection.Up
      else WheelDirection.Down
  }

  object Vertical:
    def unapply(e: Vertical): Option[Double] =
      Option(e.deltaY)

  /** Represents a wheel event that has moved horizontally, i.e. left or right
    *
    * @param deltaX
    * @param deltaMode
    */
  final case class Horizontal(deltaX: Double, deltaMode: DeltaMode) extends WheelEvent {
    val direction =
      if deltaX < 0 then WheelDirection.Left
      else WheelDirection.Right
  }

  object Horizontal:
    def unapply(e: Horizontal): Option[Double] =
      Option(e.deltaX)

  /** Represents a wheel event that has moved in the Z axis, i.e. depth
    *
    * @param deltaZ
    * @param deltaMode
    */
  final case class Depth(deltaZ: Double, deltaMode: DeltaMode) extends WheelEvent
  object Depth:
    def unapply(e: Depth): Option[Double] =
      Option(e.deltaZ)

/** Relies on the ordinal behavior of Scala 3 enums to match the button number
  */
enum MouseButton derives CanEqual:
  case LeftMouseButton, MiddleMouseButton, RightMouseButton, BrowserBackButton, BrowserForwardButton

object MouseButton:
  def fromOrdinalOpt(ordinal: Int): Option[MouseButton] =
    if ordinal >= LeftMouseButton.ordinal && ordinal <= BrowserForwardButton.ordinal then
      Some(MouseButton.fromOrdinal(ordinal))
    else Option.empty[MouseButton]

/** The type of pointer that has emitted an input pointer event
  */
enum PointerType derives CanEqual:
  case Mouse, Pen, Touch, Unknown

/** The unique identifier for a pointer input
  */
opaque type PointerId = Double
object PointerId:
  inline def apply(id: Double): PointerId = id
  val unknown                             = PointerId(0)
  given CanEqual[PointerId, PointerId]    = CanEqual.derived

  extension (m: PointerId) inline def toDouble: Double = m

/** The unique identifier for a finger input
  */
opaque type FingerId = Double
object FingerId:
  inline def apply(id: Double): FingerId = id
  val unknown                            = FingerId(0)
  given CanEqual[FingerId, FingerId]     = CanEqual.derived

  extension (m: FingerId) inline def toDouble: Double = m

@deprecated("Use `WheelDirection` instead", "0.22.0")
enum MouseWheel derives CanEqual:
  @nowarn("msg=deprecated") case ScrollUp, ScrollDown

trait PositionalInputEvent extends InputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** Coordinates relative to the magnification level
    */
  def position: Point

  /** The X position relative to the magnification level
    */
  def x: Int = position.x

  /** The Y position relative to the magnification level
    */
  def y: Int = position.y

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
sealed trait MouseEvent extends PositionalInputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** Coordinates relative to the magnification level
    */
  def position: Point

  /** The delta position between this event and the last event relative to the magnification level
    */
  def movementPosition: Point

@nowarn("msg=deprecated")
object MouseEvent:

  /** The mouse button was clicked
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param button
    *   The button that was clicked
    */
  final case class Click(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object Click:
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int): Click =
      Click(
        PointerId.unknown,
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    @nowarn("msg=deprecated")
    def apply(position: Point): Click =
      Click(
        PointerId.unknown,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def unapply(e: Click): Option[Point] =
      Option(e.position)

  @deprecated("Use `MouseEvents.Up` instead", "0.22.0")
  final case class MouseUp(
      pointerId: PointerId,
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
    @nowarn("msg=deprecated")
    def apply(position: Point): MouseUp =
      MouseUp(
        PointerId.unknown,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int): MouseUp =
      MouseUp(
        PointerId.unknown,
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int, button: MouseButton): MouseUp =
      MouseUp(
        PointerId.unknown,
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = button
      )
    def unapply(e: MouseUp): Option[Point] =
      Option(e.position)

  /** The mouse button was released
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param button
    *   The button that was released
    */
  final case class Up(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object Up:
    def apply(position: Point): Up =
      Up(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int, button: MouseButton): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = button
      )
    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** The mouse button was pressed down.
    * @param button
    *   The button that was pressed down
    */
  @deprecated("Use `MouseEvents.Down` instead", "0.22.0")
  final case class MouseDown(
      pointerId: PointerId,
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
    @nowarn("msg=deprecated")
    def apply(position: Point): MouseDown =
      MouseDown(
        PointerId.unknown,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int): MouseDown =
      MouseDown(
        PointerId.unknown,
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int, button: MouseButton): MouseDown =
      MouseDown(
        PointerId.unknown,
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = button
      )
    def unapply(e: MouseDown): Option[Point] =
      Option(e.position)

  /** The mouse button was pressed down
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param button
    *   The button that was pressed down
    */
  final case class Down(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      button: MouseButton
  ) extends MouseEvent
  object Down:
    def apply(position: Point): Down =
      Down(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = MouseButton.LeftMouseButton
      )
    def apply(x: Int, y: Int, button: MouseButton): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        button = button
      )
    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** The mouse was moved to a new position
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    */
  final case class Move(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point
  ) extends MouseEvent
  object Move:
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int): Move =
      Move(
        PointerId.unknown,
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero
      )
    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** Mouse has moved into game boundaries. It's counterpart is [[Leave]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    */
  final case class Enter(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point
  ) extends MouseEvent
  object Enter:
    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** Mouse has left game boundaries. It's counterpart is [[Enter]].
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the mouse pointer relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    */
  final case class Leave(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point
  ) extends MouseEvent
  object Leave:
    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled, which may occur when:
    *   - the mouse is disconnected
    *   - the device orientation changes
    *   - applications are switched
    *
    * @param pointerId
    * @param position
    * @param movementPosition
    */
  final case class Cancel(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point
  ) extends MouseEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

  /** The mouse wheel was rotated a certain amount around an axis.
    *
    * @param deltaX
    *   horizontal amount of pixels, pages or other unit, depending on delta mode, the X axis was scrolled
    * @param deltaY
    *   horizontal amount of pixels, pages or other unit, depending on delta mode, the Y axis was scrolled
    * @param deltaZ
    *   horizontal amount of pixels, pages or other unit, depending on delta mode, the Z axis was scrolled
    */
  @deprecated("Use `WheelEvent.Move` instead", "0.22.0")
  final case class Wheel(
      position: Point,
      buttons: Batch[MouseButton],
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      deltaX: Double,
      deltaY: Double,
      deltaZ: Double
  ) extends MouseEvent:
    def pointerId = PointerId.unknown
  object Wheel:
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int, deltaX: Double, deltaY: Double, deltaZ: Double): Wheel =
      Wheel(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        deltaX = deltaX,
        deltaY = deltaY,
        deltaZ = deltaZ
      )

    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int, deltaY: Double): Wheel =
      Wheel(
        position = Point(x, y),
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        deltaX = 0,
        deltaY = deltaY,
        deltaZ = 0
      )
    @nowarn("msg=deprecated")
    def unapply(e: Wheel): Option[(Point, Double)] =
      Option((e.position, e.deltaY))

end MouseEvent

/** Represents all touch events
  */
sealed trait TouchEvent extends PositionalInputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** The identifier of the finger that triggered the event */
  def fingerId: FingerId

  /** Coordinates relative to the magnification level
    */
  def position: Point

  /** The delta position between this event and the last event relative to the magnification level
    */
  def movementPosition: Point

  /** The normalised pressure of the touch (between 0 and 1) */
  def pressure: Double

object TouchEvent:

  /** Represents a tap of the finger on the screen
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the tap relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the tap (between 0 and 1)
    */
  final case class Tap(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Tap:
    def apply(x: Int, y: Int): Tap =
      Tap(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def apply(position: Point): Tap =
      Tap(
        PointerId.unknown,
        FingerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1
      )
    def unapply(e: Tap): Option[Point] =
      Option(e.position)

  /** The finger was released fromm the screen
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger release relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Up(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Up:
    def apply(position: Point): Up =
      Up(
        PointerId.unknown,
        FingerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 0
      )
    def apply(x: Int, y: Int): Up =
      Up(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 0
      )
    def apply(x: Int, y: Int, button: MouseButton): Up =
      Up(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 0
      )
    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** The finger was pressed down on the screen
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger press relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Down(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Down:
    def apply(position: Point): Down =
      Down(
        PointerId.unknown,
        FingerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1
      )
    def apply(x: Int, y: Int): Down =
      Down(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def apply(x: Int, y: Int, button: MouseButton): Down =
      Down(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** The finger was moved on the screen, i.e. dragged
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Move(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Move:
    def apply(x: Int, y: Int): Move =
      Move(
        PointerId.unknown,
        FingerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** A finger has entered the game boundaries. It's counterpart is [[Leave]].
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Enter(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Enter:
    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** A finger has left the game boundaries. It's counterpart is [[Enter]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Leave(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Leave:
    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled, which may occur when:
    *   - the touch device is disconnected
    *   - the device orientation changes
    *   - a palm rejection is detected
    *   - applications are switched
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param fingerId
    *   The unique identifier for the finger input
    * @param position
    *   The position of the finger relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the touch (between 0 and 1)
    */
  final case class Cancel(
      pointerId: PointerId,
      fingerId: FingerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends TouchEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

end TouchEvent

sealed trait PenEvent extends PositionalInputEvent:
  /** Unique pointer identifier
    */
  def pointerId: PointerId

  /** Coordinates relative to the magnification level
    */
  def position: Point

  /** The normalised pressure of the pen */
  def pressure: Double

object PenEvent:

  /** The pen has been pressed and released or a button on the pen has been pressed and released. Where a button is
    * provided, it indicates which button was pressed on the pen. If a button is not provided, it indicates that the pen
    * was pressed down on the pad
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    * @param button
    *   The button that was pressed, if any
    */
  final case class Click(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double,
      button: Option[MouseButton]
  ) extends PenEvent
  object Click:
    def apply(x: Int, y: Int): Click =
      Click(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int, button: MouseButton): Click =
      Click(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def apply(position: Point): Click =
      Click(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(position: Point, button: MouseButton): Click =
      Click(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def unapply(e: Click): Option[Point] =
      Option(e.position)

  /** The pen has been released or a button on the pen has been released. Where a button is provided, it indicates which
    * button was released on the pen. If a button is not provided, it indicates that the pen was released from the pad
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    * @param button
    *   The button that was released, if any
    */
  final case class Up(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double,
      button: Option[MouseButton]
  ) extends PenEvent
  object Up:
    def apply(position: Point): Up =
      Up(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int, button: MouseButton): Up =
      Up(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** The pen was pressed down on the pad or a button on the pen was pressed down. Where a button is provided, it
    * indicates which button was pressed on the pen. If a button is not provided, it indicates that the pen was pressed
    * down on the pad
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    * @param button
    *   The button that was pressed, if any
    */
  final case class Down(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double,
      button: Option[MouseButton]
  ) extends PenEvent
  object Down:
    def apply(position: Point): Down =
      Down(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(x: Int, y: Int): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option.empty
      )
    def apply(position: Point, button: MouseButton): Down =
      Down(
        PointerId.unknown,
        position = position,
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def apply(x: Int, y: Int, button: MouseButton): Down =
      Down(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1,
        button = Option(button)
      )
    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** The pen was moved on the pad, i.e. dragged
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Move(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Move:
    @nowarn("msg=deprecated")
    def apply(x: Int, y: Int): Move =
      Move(
        PointerId.unknown,
        position = Point(x, y),
        movementPosition = Point.zero,
        pressure = 1
      )
    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** Pen has entered the game boundaries. It's counterpart is [[Leave]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Enter(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Enter:
    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** Pen has left the game boundaries. It's counterpart is [[Enter]]
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Leave(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Leave:
    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled, which may occur when:
    *   - the pen is disconnected
    *   - the device orientation changes
    *   - applications are switched
    *
    * @param pointerId
    *   The unique identifier for the pointer input
    * @param position
    *   The position of the pen relative to the magnification level
    * @param movementPosition
    *   The delta position between this event and the last event relative to the magnification level
    * @param pressure
    *   The normalised pressure of the pen (between 0 and 1)
    */
  final case class Cancel(
      pointerId: PointerId,
      position: Point,
      movementPosition: Point,
      pressure: Double
  ) extends PenEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

end PenEvent

/** Represents all mouse, pen and touch events
  */
sealed trait PointerEvent extends PositionalInputEvent:

  /** The width (magnitude on the X axis), of the contact geometry of the pointer relative to the magnification level
    */
  @deprecated("Being removed to simplify Input", "0.22.0")
  def width: Int

  /** The height (magnitude on the Y axis), of the contact geometry of the pointer relative to the magnification level
    */
  @deprecated("Being removed to simplify Input", "0.22.0")
  def height: Int

  /** The normalized pressure of the pointer input in the range 0 to 1, where 0 and 1 represent the minimum and maximum
    * pressure the hardware is capable of detecting, respectively.
    */
  @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
  def pressure: Double

  /** The normalized tangential pressure of the pointer input (also known as barrel pressure or cylinder stress) in the
    * range -1 to 1, where 0 is the neutral position of the control.
    */
  @deprecated("Being removed to simplify Input", "0.22.0")
  def tangentialPressure: Double

  /** The plane angle (in radians, in the range of -1.570796 to 1.570796 (-90 - 90 degrees)) between the Y–Z plane and
    * the plane containing both the pointer (e.g. pen stylus) axis and the Y axis.
    */
  @deprecated("Being removed to simplify Input", "0.22.0")
  def tiltX: Radians

  /** The plane angle (in radians, in the range of -1.570796 to 1.570796 (-90 - 90 degrees)) between the X–Z plane and
    * the plane containing both the pointer (e.g. pen stylus) axis and the X axis.
    */
  @deprecated("Being removed to simplify Input", "0.22.0")
  def tiltY: Radians

  /** The clockwise rotation of the pointer (e.g. pen stylus) around its major axis in degrees, with a value in the
    * range 0 to 6.265732 (0 to 359 degrees)
    */
  @deprecated("Being removed to simplify Input", "0.22.0")
  def twist: Radians

  /** Indicates the device type that caused the event (mouse, pen, touch, etc.)
    */
  def pointerType: PointerType

  /** Indicates whether the pointer is considered primary - like first finger during multi-touch gesture
    */
  @deprecated("Being removed to simplify Input", "0.22.0")
  def isPrimary: Boolean

@nowarn("msg=deprecated")
object PointerEvent:
  /** Pointing device is moved into canvas hit test boundaries. It's counterpart is [[Leave]].
    */
  final case class Enter(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean
  ) extends PointerEvent
  object Enter:
    def unapply(e: Enter): Option[Point] =
      Option(e.position)

  /** Pointing device left canvas hit test boundaries. It's counterpart is [[Enter]].
    */
  final case class Leave(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean
  ) extends PointerEvent
  object Leave:
    def unapply(e: Leave): Option[Point] =
      Option(e.position)

  /** Pointing device is in active buttons state.
    */
  final case class Down(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean,
      button: Option[MouseButton]
  ) extends PointerEvent
  object Down:
    def apply(position: Point): Down =
      Down(position, MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(x: Int, y: Int): Down =
      Down(Point(x, y), MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(position: Point, pointerType: PointerType): Down =
      Down(position, MouseButton.LeftMouseButton, pointerType)
    def apply(x: Int, y: Int, pointerType: PointerType): Down =
      Down(Point(x, y), MouseButton.LeftMouseButton, pointerType)
    def apply(position: Point, button: MouseButton): Down =
      Down(position, button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton): Down =
      Down(Point(x, y), button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton, pointerType: PointerType): Down =
      Down(Point(x, y), button, pointerType)

    @nowarn("msg=deprecated")
    def apply(position: Point, button: MouseButton, pointerType: PointerType): Down =
      Down(
        pointerId = PointerId.unknown,
        position = position,
        buttons = Batch(button),
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = Some(button),
        width = 0,
        height = 0,
        pressure = 0,
        tangentialPressure = 0,
        tiltX = Radians.zero,
        tiltY = Radians.zero,
        twist = Radians.zero,
        pointerType = pointerType,
        isPrimary = true
      )

    def unapply(e: Down): Option[Point] =
      Option(e.position)

  /** Pointing device is no longer in active buttons state.
    */
  final case class Up(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean,
      button: Option[MouseButton]
  ) extends PointerEvent
  object Up:
    def apply(position: Point): Up =
      Up(position, MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(x: Int, y: Int): Up =
      Up(Point(x, y), MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(position: Point, pointerType: PointerType): Up =
      Up(position, MouseButton.LeftMouseButton, pointerType)
    def apply(x: Int, y: Int, pointerType: PointerType): Up =
      Up(Point(x, y), MouseButton.LeftMouseButton, pointerType)
    def apply(position: Point, button: MouseButton): Up =
      Up(position, button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton): Up =
      Up(Point(x, y), button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton, pointerType: PointerType): Up =
      Up(Point(x, y), button, pointerType)

    @nowarn("msg=deprecated")
    def apply(position: Point, button: MouseButton, pointerType: PointerType): Up =
      Up(
        pointerId = PointerId.unknown,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = Some(button),
        width = 0,
        height = 0,
        pressure = 0,
        tangentialPressure = 0,
        tiltX = Radians.zero,
        tiltY = Radians.zero,
        twist = Radians.zero,
        pointerType = pointerType,
        isPrimary = true
      )

    def unapply(e: Up): Option[Point] =
      Option(e.position)

  /** Pointing device button has been clicked */
  final case class Click(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean,
      button: Option[MouseButton]
  ) extends PointerEvent
  object Click:
    def apply(position: Point): Click =
      Click(position, MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(x: Int, y: Int): Click =
      Click(Point(x, y), MouseButton.LeftMouseButton, PointerType.Mouse)
    def apply(position: Point, pointerType: PointerType): Click =
      Click(position, MouseButton.LeftMouseButton, pointerType)
    def apply(x: Int, y: Int, pointerType: PointerType): Click =
      Click(Point(x, y), MouseButton.LeftMouseButton, pointerType)
    def apply(position: Point, button: MouseButton): Click =
      Click(position, button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton): Click =
      Click(Point(x, y), button, PointerType.Mouse)
    def apply(x: Int, y: Int, button: MouseButton, pointerType: PointerType): Click =
      Click(Point(x, y), button, pointerType)

    @nowarn("msg=deprecated")
    def apply(position: Point, button: MouseButton, pointerType: PointerType): Click =
      Click(
        pointerId = PointerId.unknown,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        button = Some(button),
        width = 0,
        height = 0,
        pressure = 0,
        tangentialPressure = 0,
        tiltX = Radians.zero,
        tiltY = Radians.zero,
        twist = Radians.zero,
        pointerType = pointerType,
        isPrimary = true
      )
    def unapply(e: Click): Option[Point] =
      Option(e.position)

  /** Pointing device changed coordinates.
    */
  final case class Move(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean
  ) extends PointerEvent
  object Move:
    def apply(position: Point): Move =
      Move(position, PointerType.Mouse)
    def apply(x: Int, y: Int): Move =
      Move(Point(x, y), PointerType.Mouse)
    def apply(x: Int, y: Int, pointerType: PointerType): Move =
      Move(Point(x, y), pointerType)

    @nowarn("msg=deprecated")
    def apply(position: Point, pointerType: PointerType): Move =
      Move(
        pointerId = PointerId.unknown,
        position = position,
        buttons = Batch.empty,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false,
        movementPosition = Point.zero,
        width = 0,
        height = 0,
        pressure = 0,
        tangentialPressure = 0,
        tiltX = Radians.zero,
        tiltY = Radians.zero,
        twist = Radians.zero,
        pointerType = pointerType,
        isPrimary = true
      )

    def unapply(e: Move): Option[Point] =
      Option(e.position)

  /** The ongoing interactions was cancelled due to:
    *   - the pointer device being disconnected
    *   - device orientation change
    *   - palm rejection
    *   - switching applications
    */
  final case class Cancel(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean
  ) extends PointerEvent
  object Cancel:
    def unapply(e: Cancel): Option[Point] =
      Option(e.position)

  /** The pointer is no longer sending events because:
    *   - the pointing device is moved out of the hit boundaries
    *   - the PointerUp event was fired on a device that doesn't support hover
    *   - after firing the PointerCancel event
    *   - when a pen stylus leaves the hover range detectable by the digitizer.
    */

  @deprecated("Use `PointerEvent.Leave` instead", "0.22.0")
  final case class Out(
      pointerId: PointerId,
      position: Point,
      @deprecated("Use `InputState.mouse.buttons` instead", "0.22.0")
      buttons: Batch[MouseButton],
      @deprecated("Use `InputState.keyboard.isAltKeyDown` instead", "0.22.0")
      isAltKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isCtrlKeyDown` instead", "0.22.0")
      isCtrlKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isMetaKeyDown` instead", "0.22.0")
      isMetaKeyDown: Boolean,
      @deprecated("Use `InputState.keyboard.isShiftKeyDown` instead", "0.22.0")
      isShiftKeyDown: Boolean,
      movementPosition: Point,
      @deprecated("Being removed to simplify Input", "0.22.0")
      width: Int,
      @deprecated("Being removed to simplify Input", "0.22.0")
      height: Int,
      @deprecated("Use `TouchEvent.pressure` instead", "0.22.0")
      pressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tangentialPressure: Double,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltX: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      tiltY: Radians,
      @deprecated("Being removed to simplify Input", "0.22.0")
      twist: Radians,
      pointerType: PointerType,
      @deprecated("Being removed to simplify Input", "0.22.0")
      isPrimary: Boolean
  ) extends PointerEvent
  object Out:
    def unapply(e: Out): Option[Point] =
      Option(e.position)

/** Represents all keyboard events
  */
sealed trait KeyboardEvent extends InputEvent {
  val key: Key
  val isRepeat: Boolean
  val isAltKeyDown: Boolean
  val isCtrlKeyDown: Boolean
  val isMetaKeyDown: Boolean
  val isShiftKeyDown: Boolean
}
object KeyboardEvent {

  /** A key was released during the last frame
    *
    * @param key
    *   A `Key` instance representing the key that was released
    * @param isRepeat
    *   Whether the key was pressed repeatedly since the last frame
    * @param isAltKeyDown
    *   Whether the `alt` key was pressed when the event was fired
    * @param isCtrlKeyDown
    *   Whether the `ctrl` key was pressed when the event was fired
    * @param isMetaKeyDown
    *   Whether the meta button (Windows key, or Cmd Key) key was pressed when the event was fired
    * @param isShiftKeyDown
    *   Whether the `shift` key was pressed when the event was fired
    */
  final case class KeyUp(
      key: Key,
      isRepeat: Boolean,
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean
  ) extends KeyboardEvent
  object KeyUp:
    def apply(key: Key): KeyUp =
      KeyUp(
        key,
        isRepeat = false,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false
      )
    def unapply(e: KeyUp): Option[Key] =
      Option(e.key)

  /** A key was pressed down during the last frame
    *
    * @param key
    *   A `Key` instance representing the key that was pressed
    * @param isRepeat
    *   Whether the key was pressed repeatedly since the last frame
    * @param isAltKeyDown
    *   Whether the `alt` key was pressed when the event was fired
    * @param isCtrlKeyDown
    *   Whether the `ctrl` key was pressed when the event was fired
    * @param isMetaKeyDown
    *   Whether the meta button (Windows key, or Cmd Key) key was pressed when the event was fired
    * @param isShiftKeyDown
    *   Whether the `shift` key was pressed when the event was fired
    */
  final case class KeyDown(
      key: Key,
      isRepeat: Boolean,
      isAltKeyDown: Boolean,
      isCtrlKeyDown: Boolean,
      isMetaKeyDown: Boolean,
      isShiftKeyDown: Boolean
  ) extends KeyboardEvent
  object KeyDown:
    def apply(key: Key): KeyDown =
      KeyDown(
        key,
        isRepeat = false,
        isAltKeyDown = false,
        isCtrlKeyDown = false,
        isMetaKeyDown = false,
        isShiftKeyDown = false
      )
    def unapply(e: KeyDown): Option[Key] =
      Option(e.key)
}

/** Can be emitted to trigger the one time play back of a sound asset.
  *
  * @param assetName
  *   Reference to the loaded asset
  * @param volume
  *   What volume level to play at
  * @param policy
  *   How to handle the previous sounds
  */
final case class PlaySound(assetName: AssetName, volume: Volume, policy: PlaybackPolicy) extends GlobalEvent:
  def withVolume(newVolume: Volume): PlaySound =
    this.copy(volume = newVolume)
  def withPlaybackPolicy(newPolicy: PlaybackPolicy): PlaySound =
    this.copy(policy = newPolicy)

object PlaySound:

  def apply(assetName: AssetName): PlaySound =
    PlaySound(assetName, Volume.Max, PlaybackPolicy.Continue)

  def apply(assetName: AssetName, volume: Volume): PlaySound =
    PlaySound(assetName, volume, PlaybackPolicy.Continue)

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
    * @param assets
    *   The assets that were loaded
    * @param available
    *   Whether or not the asset has been made available for the game to use.
    */
  final case class AssetBatchLoaded(key: BindingKey, assets: Set[AssetType], available: Boolean) extends AssetEvent

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
  case Rebuild(assetCollection: AssetCollection, nextEvent: GlobalEvent)
