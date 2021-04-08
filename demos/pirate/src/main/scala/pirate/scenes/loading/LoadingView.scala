package pirate.scenes.loading

import indigo._
import pirate.core.Assets

// Handles the rendering of the loading screen.
// It can only make use of assets that were loaded before the
// game was allowed to start, so the aim is to only load what
// you must up front, and then load the rest when you can give
// your players feedback, as we do below.
object LoadingView {

  def draw(screenDimensions: Rectangle, captain: Sprite, loadingState: LoadingState): SceneUpdateFragment = {
    val x = screenDimensions.horizontalCenter
    val y = screenDimensions.verticalCenter

    val message: String =
      loadingState match {
        case LoadingState.NotStarted =>
          "Loading..."

        case LoadingState.InProgress(percent) =>
          s"Loading...${percent.toString()}%"

        case LoadingState.Complete =>
          "Loading...100%"

        case LoadingState.Error =>
          "Uh oh, loading failed..."
      }

    SceneUpdateFragment(
      Text(
        message,
        x,
        y + 10,
        1,
        Assets.Fonts.fontKey,
        Assets.Fonts.fontMaterial
      ).alignCenter,
      captain
        .modifyMaterial {
          case m: Material.ImageEffects => m.withOverlay(Fill.Color(RGBA.White))
          case m                        => m
        }
        .moveTo(x, y)
        .changeCycle(CycleLabel("Run"))
        .play()
    )
  }

}
