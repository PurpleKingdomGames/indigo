package indigo.shared.config

final case class AdvancedGameConfig(antiAliasing: Boolean, batchSize: Int, disableSkipModelUpdates: Boolean, disableSkipViewUpdates: Boolean)

object AdvancedGameConfig {
  val default: AdvancedGameConfig = AdvancedGameConfig(
    antiAliasing = false,
    batchSize = 256,
    disableSkipModelUpdates = false,
    disableSkipViewUpdates = false
  )
}
