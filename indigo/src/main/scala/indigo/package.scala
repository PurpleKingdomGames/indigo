import indigo.shared.events.EventTypeAliases
import indigo.scenegraph.SceneGraphTypeAliases
import indigo.shared.datatypes.DataTypeAliases
import indigo.shared.networking.NetworkingTypeAliases
import indigo.shared.SharedTypeAliases

package object indigo extends DataTypeAliases with SceneGraphTypeAliases with NetworkingTypeAliases with SharedTypeAliases with EventTypeAliases {

  val logger: indigo.shared.IndigoLogger.type = indigo.shared.IndigoLogger

  type Startup[ErrorType, SuccessType] = shared.Startup[ErrorType, SuccessType]
  val Startup: shared.Startup.type = shared.Startup

  type GameTime = shared.time.GameTime
  val GameTime: shared.time.GameTime.type = shared.time.GameTime

  type Millis = shared.time.Millis
  val Millis: shared.time.Millis.type = shared.time.Millis

  type Seconds = shared.time.Seconds
  val Seconds: shared.time.Seconds.type = shared.time.Seconds

  type Dice = shared.dice.Dice
  val Dice: shared.dice.Dice.type = shared.dice.Dice

  type AssetCollection = gameengine.AssetCollection
  val AssetCollection: gameengine.AssetCollection.type = gameengine.AssetCollection

  type ToReportable[T] = shared.ToReportable[T]
  val ToReportable: shared.ToReportable.type = shared.ToReportable

  type StartupErrors = shared.StartupErrors
  val StartupErrors: shared.StartupErrors.type = shared.StartupErrors

  type Outcome[T] = shared.Outcome[T]
  val Outcome: shared.Outcome.type = shared.Outcome

  val Keys: shared.constants.Keys.type = shared.constants.Keys

  type KeyCode = shared.constants.KeyCode
  val KeyCode: shared.constants.KeyCode.type = shared.constants.KeyCode

  type PowerOfTwo = shared.PowerOfTwo
  val PowerOfTwo: shared.PowerOfTwo.type = shared.PowerOfTwo

  type NonEmptyList[A] = shared.collections.NonEmptyList[A]
  val NonEmptyList: shared.collections.NonEmptyList.type = shared.collections.NonEmptyList

  val WebSockets: gameengine.WebSockets.type = gameengine.WebSockets
  val Http: gameengine.Http.type             = gameengine.Http
}
