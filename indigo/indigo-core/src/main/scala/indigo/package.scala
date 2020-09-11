import indigo.shared.events.EventTypeAliases
import indigo.shared.scenegraph.SceneGraphTypeAliases
import indigo.shared.datatypes.DataTypeAliases
import indigo.shared.networking.NetworkingTypeAliases
import indigo.shared.SharedTypeAliases

package object indigo extends DataTypeAliases with SceneGraphTypeAliases with NetworkingTypeAliases with SharedTypeAliases with EventTypeAliases {

  val logger: indigo.shared.IndigoLogger.type = indigo.shared.IndigoLogger

  type Startup[SuccessType] = shared.Startup[SuccessType]
  val Startup: shared.Startup.type = shared.Startup

  type GameTime = shared.time.GameTime
  val GameTime: shared.time.GameTime.type = shared.time.GameTime

  type Millis = shared.time.Millis
  val Millis: shared.time.Millis.type = shared.time.Millis

  type Seconds = shared.time.Seconds
  val Seconds: shared.time.Seconds.type = shared.time.Seconds

  type Dice = shared.dice.Dice
  val Dice: shared.dice.Dice.type = shared.dice.Dice

  type AssetCollection = platform.assets.AssetCollection

  type AssetName = shared.assets.AssetName
  val AssetName: shared.assets.AssetName.type = shared.assets.AssetName

  type AssetPath = shared.assets.AssetPath
  val AssetPath: shared.assets.AssetPath.type = shared.assets.AssetPath

  type AssetTag = shared.assets.AssetTag
  val AssetTag: shared.assets.AssetTag.type = shared.assets.AssetTag

  type Material = shared.datatypes.Material
  val Material: shared.datatypes.Material.type = shared.datatypes.Material

  type Texture = shared.datatypes.Texture
  val Texture: shared.datatypes.Texture.type = shared.datatypes.Texture

  type Outcome[T] = shared.Outcome[T]
  val Outcome: shared.Outcome.type = shared.Outcome

  val Keys: shared.constants.Keys.type = shared.constants.Keys

  type Key = shared.constants.Key
  val Key: shared.constants.Key.type = shared.constants.Key

  type PowerOfTwo = shared.PowerOfTwo
  val PowerOfTwo: shared.PowerOfTwo.type = shared.PowerOfTwo

  type NonEmptyList[A] = shared.collections.NonEmptyList[A]
  val NonEmptyList: shared.collections.NonEmptyList.type = shared.collections.NonEmptyList

  type Signal[A] = shared.temporal.Signal[A]
  val Signal: shared.temporal.Signal.type = shared.temporal.Signal

  type SignalFunction[A, B] = shared.temporal.SignalFunction[A, B]
  val SignalFunction: shared.temporal.SignalFunction.type = shared.temporal.SignalFunction

  type SubSystem = shared.subsystems.SubSystem
  val SubSystem: shared.subsystems.SubSystem.type = shared.subsystems.SubSystem

  /**
    * defaultGameConfig Provides a useful default config set up:
    * - Game Viewport = 550 x 400
    * - FPS = 30
    * - Clear color = Black
    * - Magnification = 1
    * - No advanced settings enabled
    * @return A GameConfig instance
    */
  val defaultGameConfig: indigo.shared.config.GameConfig =
    indigo.shared.config.GameConfig.default

  /**
    * noRender Convenience value, alias for SceneUpdateFragment.empty
    * @return An Empty SceneUpdateFragment
    */
  val noRender: indigo.shared.scenegraph.SceneUpdateFragment =
    indigo.shared.scenegraph.SceneUpdateFragment.empty

}
