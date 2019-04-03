package indigo.shared

import indigo.shared

trait SharedTypeAliases {

  type AsString[A] = shared.AsString[A]
  val AsString: shared.AsString.type = shared.AsString

  type EqualTo[A] = shared.EqualTo[A]
  val EqualTo: shared.EqualTo.type = shared.EqualTo

  type AssetType = shared.AssetType
  val AssetType: shared.AssetType.type = shared.AssetType

  type ClearColor = shared.ClearColor
  val ClearColor: shared.ClearColor.type = shared.ClearColor

  type GameConfig = shared.GameConfig
  val GameConfig: shared.GameConfig.type = shared.GameConfig

  type GameViewport = shared.GameViewport
  val GameViewport: shared.GameViewport.type = shared.GameViewport

  type AdvancedGameConfig = shared.AdvancedGameConfig
  val AdvancedGameConfig: shared.AdvancedGameConfig.type = shared.AdvancedGameConfig

}
