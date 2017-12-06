package snake

import com.purplekingdomgames.shared.{AssetType, ImageAsset}

object SnakeAssets {

  val snakeTexture: String = "snakeTexture"

  def assets: Set[AssetType] =
    Set(
      ImageAsset(snakeTexture, "assets/snake.png")
    )

}
