import indigo.runtime.{IndigoLogger, Show}

package object indigo {

  val logger: IndigoLogger.type = IndigoLogger

  implicit class WithShow[T](val t: T) extends AnyVal {
    def show(implicit showMe: Show[T]): String = showMe.show(t)
  }

  // shared
  type AssetType = shared.AssetType
  val AssetType: shared.AssetType.type = shared.AssetType

  type ClearColor = shared.ClearColor
  val ClearColor: shared.ClearColor.type = shared.ClearColor

  type GameConfig = shared.GameConfig
  val GameConfig: shared.GameConfig.type = shared.GameConfig

  type GameViewport = shared.GameViewport
  val GameViewport: shared.GameViewport.type = shared.GameViewport

  // gameengine

  type Startup[+ErrorType, +SuccessType] = gameengine.Startup[ErrorType, SuccessType]

  type GameTime         = gameengine.GameTime
  type StartupErrors    = gameengine.StartupErrors
  type AssetCollection  = gameengine.assets.AssetCollection
  type FrameInputEvents = gameengine.events.FrameInputEvents
  type GameEvent        = gameengine.events.GameEvent
  type ViewEvent        = gameengine.events.ViewEvent

  type ToReportable[T] = gameengine.ToReportable[T]
  val ToReportable: gameengine.ToReportable.type = gameengine.ToReportable

  type WebSocketEvent = gameengine.events.WebSocketEvent
  val WebSocketEvent: gameengine.events.WebSocketEvent.type = gameengine.events.WebSocketEvent

  type Animations = gameengine.scenegraph.Animations
  val Animations: gameengine.scenegraph.Animations.type = gameengine.scenegraph.Animations

  type Cycle = gameengine.scenegraph.Cycle
  val Cycle: gameengine.scenegraph.Cycle.type = gameengine.scenegraph.Cycle

  type Frame = gameengine.scenegraph.Frame
  val Frame: gameengine.scenegraph.Frame.type = gameengine.scenegraph.Frame

  type Sprite = gameengine.scenegraph.Sprite
  val Sprite: gameengine.scenegraph.Sprite.type = gameengine.scenegraph.Sprite

  type BindingKey = gameengine.scenegraph.datatypes.BindingKey
  val BindingKey: gameengine.scenegraph.datatypes.BindingKey.type = gameengine.scenegraph.datatypes.BindingKey

  type AnimationsKey = gameengine.scenegraph.AnimationsKey
  val AnimationsKey: gameengine.scenegraph.AnimationsKey.type = gameengine.scenegraph.AnimationsKey

  type SceneUpdateFragment = gameengine.scenegraph.SceneUpdateFragment
  val SceneUpdateFragment: gameengine.scenegraph.SceneUpdateFragment.type = gameengine.scenegraph.SceneUpdateFragment

  type Text = gameengine.scenegraph.Text
  val Text: gameengine.scenegraph.Text.type = gameengine.scenegraph.Text

  type Graphic = gameengine.scenegraph.Graphic
  val Graphic: gameengine.scenegraph.Graphic.type = gameengine.scenegraph.Graphic

  type FontChar = gameengine.scenegraph.datatypes.FontChar
  val FontChar: gameengine.scenegraph.datatypes.FontChar.type = gameengine.scenegraph.datatypes.FontChar

  type FontInfo = gameengine.scenegraph.datatypes.FontInfo
  val FontInfo: gameengine.scenegraph.datatypes.FontInfo.type = gameengine.scenegraph.datatypes.FontInfo

  type FontKey = gameengine.scenegraph.datatypes.FontKey
  val FontKey: gameengine.scenegraph.datatypes.FontKey.type = gameengine.scenegraph.datatypes.FontKey

  type Rectangle = gameengine.scenegraph.datatypes.Rectangle
  val Rectangle: gameengine.scenegraph.datatypes.Rectangle.type = gameengine.scenegraph.datatypes.Rectangle

  type Depth = gameengine.scenegraph.datatypes.Depth
  val Depth: gameengine.scenegraph.datatypes.Depth.type = gameengine.scenegraph.datatypes.Depth

  // networking
  type WebSocketConfig = networking.WebSocketConfig
  val WebSocketConfig: networking.WebSocketConfig.type = networking.WebSocketConfig

  type WebSocketId = networking.WebSocketId
  val WebSocketId: networking.WebSocketId.type = networking.WebSocketId

}
