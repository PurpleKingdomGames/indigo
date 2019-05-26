package ingidoexamples

import indigo._
import indigoexts.uicomponents._

object LaunchButton {

  def present(launchButton: Button, frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    val button: ButtonViewUpdate = launchButton.draw(
      bounds = Rectangle(10, 100, 16, 16), // Where should the button be on the screen?
      depth = Depth(2),                    // At what depth?
      frameInputEvents = frameInputEvents, // delegate events
      buttonAssets = ButtonAssets(         // We could cache the graphics much earlier
        up = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 0, 16, 16),
        over = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 16, 16, 16),
        down = Graphic(0, 0, 16, 16, 2, "graphics").withCrop(32, 32, 16, 16)
      )
    )

    button.toSceneUpdateFragment
  }

}
