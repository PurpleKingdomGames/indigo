import indigo.gameengine.scenegraph.SceneGraphTypeAliases
import indigo.gameengine.scenegraph.datatypes.DataTypeAliases
import indigo.networking.NetworkingTypeAliases
import indigo.runtime.{IndigoLogger, Show}
import indigo.shared.SharedTypeAliases

package object indigo extends DataTypeAliases with SceneGraphTypeAliases with NetworkingTypeAliases with SharedTypeAliases {

  val logger: IndigoLogger.type = IndigoLogger

  implicit class WithShow[T](val t: T) extends AnyVal {
    def show(implicit showMe: Show[T]): String = showMe.show(t)
  }

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

  val Keys: gameengine.constants.Keys.type = gameengine.constants.Keys

  type KeyCode = gameengine.constants.KeyCode
  val KeyCode: gameengine.constants.KeyCode.type = gameengine.constants.KeyCode

}
