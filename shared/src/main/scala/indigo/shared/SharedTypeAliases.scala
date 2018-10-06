package indigo.shared
import indigo.shared

trait SharedTypeAliases {

  type AssetType = shared.AssetType
  val AssetType: shared.AssetType.type = shared.AssetType

  type ClearColor = shared.ClearColor
  val ClearColor: shared.ClearColor.type = shared.ClearColor

  type GameConfig = shared.GameConfig
  val GameConfig: shared.GameConfig.type = shared.GameConfig

  type GameViewport = shared.GameViewport
  val GameViewport: shared.GameViewport.type = shared.GameViewport

}
