package ingidoexamples

import indigo._
import indigoexts.uicomponents._

object LaunchButton {

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 80, 30, 2, "button").withCrop(0, 0, 80, 30),
      over = Graphic(0, 0, 80, 30, 2, "button").withCrop(0, 30, 80, 30),
      down = Graphic(0, 0, 80, 30, 2, "button").withCrop(0, 60, 80, 30)
    )

  def present(launchButton: Button, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    launchButton
      .draw(Rectangle(10, 100, 80, 30), Depth(2), frameInputEvents, buttonAssets)
      .toSceneUpdateFragment

}
