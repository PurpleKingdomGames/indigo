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

  // gameengine
  type GameTime            = gameengine.GameTime
  type StartupErrors       = gameengine.StartupErrors
  type AssetCollection     = gameengine.assets.AssetCollection
  type FrameInputEvents    = gameengine.events.FrameInputEvents
  type GameEvent           = gameengine.events.GameEvent
  type ViewEvent           = gameengine.events.ViewEvent
  type Animations          = gameengine.scenegraph.Animations
  type SceneUpdateFragment = gameengine.scenegraph.SceneUpdateFragment
  val SceneUpdateFragment: gameengine.scenegraph.SceneUpdateFragment.type = gameengine.scenegraph.SceneUpdateFragment

  type Text = gameengine.scenegraph.Text
  val Text: gameengine.scenegraph.Text.type = gameengine.scenegraph.Text

  type FontChar = gameengine.scenegraph.datatypes.FontChar
  val FontChar: gameengine.scenegraph.datatypes.FontChar.type = gameengine.scenegraph.datatypes.FontChar

  type FontInfo = gameengine.scenegraph.datatypes.FontInfo
  val FontInfo: gameengine.scenegraph.datatypes.FontInfo.type = gameengine.scenegraph.datatypes.FontInfo

  type FontKey = gameengine.scenegraph.datatypes.FontKey
  val FontKey: gameengine.scenegraph.datatypes.FontKey.type = gameengine.scenegraph.datatypes.FontKey

  type Rectangle = gameengine.scenegraph.datatypes.Rectangle
  val Rectangle: gameengine.scenegraph.datatypes.Rectangle.type = gameengine.scenegraph.datatypes.Rectangle

}
