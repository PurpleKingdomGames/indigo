import indigo.gameengine.events.EventTypeAliases
import indigo.gameengine.scenegraph.SceneGraphTypeAliases
import indigo.gameengine.scenegraph.datatypes.DataTypeAliases
import indigo.networking.NetworkingTypeAliases
import indigo.runtime.IndigoLogger
import indigo.shared.SharedTypeAliases

package object indigo extends DataTypeAliases with SceneGraphTypeAliases with NetworkingTypeAliases with SharedTypeAliases with EventTypeAliases {

  val logger: IndigoLogger.type = IndigoLogger

  type Show[A] = runtime.Show[A]
  val Show: runtime.Show.type = runtime.Show

  implicit class WithShow[T](val t: T) extends AnyVal {
    def show(implicit showMe: Show[T]): String = showMe.show(t)
  }

  type Startup[+ErrorType, +SuccessType] = gameengine.Startup[ErrorType, SuccessType]

  type GameTime = gameengine.GameTime
  val GameTime: gameengine.GameTime.type = gameengine.GameTime

  type SubSystem = gameengine.subsystems.SubSystem

  type UpdatedSubSystem = gameengine.subsystems.UpdatedSubSystem
  val UpdatedSubSystem: gameengine.subsystems.UpdatedSubSystem.type = gameengine.subsystems.UpdatedSubSystem

  type AssetCollection = gameengine.assets.AssetCollection
  val AssetCollection: gameengine.assets.AssetCollection.type = gameengine.assets.AssetCollection

  type ToReportable[T] = gameengine.ToReportable[T]
  val ToReportable: gameengine.ToReportable.type = gameengine.ToReportable

  type StartupErrors = gameengine.StartupErrors
  val StartupErrors: gameengine.StartupErrors.type = gameengine.StartupErrors

  type UpdatedModel[T] = gameengine.UpdatedModel[T]
  val UpdatedModel: gameengine.UpdatedModel.type = gameengine.UpdatedModel

  type UpdatedViewModel[T] = gameengine.UpdatedViewModel[T]
  val UpdatedViewModel: gameengine.UpdatedViewModel.type = gameengine.UpdatedViewModel

  // Constants

  val Keys: gameengine.constants.Keys.type = gameengine.constants.Keys

  type KeyCode = gameengine.constants.KeyCode
  val KeyCode: gameengine.constants.KeyCode.type = gameengine.constants.KeyCode

}
