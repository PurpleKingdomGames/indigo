package snake.scenes

import indigo._
import indigo.scenes._
import snake.model.{ControlScheme, SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, Settings, SnakeStartupData}

object ControlsScene extends Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel] {
  type SceneModel     = ControlScheme
  type SceneViewModel = Unit

  val name: SceneName = SceneName("controls")

  val sceneModelLens: Lens[SnakeGameModel, ControlScheme] =
    SnakeGameModel.Lenses.controlSchemeAccessors

  val sceneViewModelLens: Lens[SnakeViewModel, Unit] =
    Lens.fixed(())

  val sceneSubSystems: Set[SubSystem] =
    Set()

  def updateSceneModel(context: FrameContext[SnakeStartupData], controlScheme: ControlScheme): GlobalEvent => Outcome[ControlScheme] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(controlScheme)
        .addGlobalEvents(SceneEvent.JumpTo(GameScene.name))

    case KeyboardEvent.KeyUp(Keys.UP_ARROW) | KeyboardEvent.KeyUp(Keys.DOWN_ARROW) =>
      Outcome(controlScheme.swap)

    case _ =>
      Outcome(controlScheme)
  }

  def updateSceneViewModel(
      context: FrameContext[SnakeStartupData],
      controlScheme: ControlScheme,
      sceneViewModel: Unit
  ): Outcome[Unit] =
    Outcome(())

  def updateSceneView(
      context: FrameContext[SnakeStartupData],
      sceneModel: ControlScheme,
      sceneViewModel: Unit
  ): SceneUpdateFragment = {
    val horizontalCenter: Int = (Settings.viewportWidth / Settings.magnificationLevel) / 2
    val verticalMiddle: Int   = (Settings.viewportHeight / Settings.magnificationLevel) / 2

    SceneUpdateFragment.empty
      .addUiLayerNodes(drawControlsText(24, verticalMiddle, sceneModel))
      .addUiLayerNodes(drawSelectText(horizontalCenter))
      .addUiLayerNodes(SharedElements.drawHitSpaceToStart(horizontalCenter, Seconds(1), context.gameTime))
  }

  def drawControlsText(center: Int, middle: Int, controlScheme: ControlScheme): List[SceneGraphNode] =
    List(
      Text("select controls", center, middle - 20, 1, GameAssets.fontKey).alignLeft
    ) ++ {
      controlScheme match {
        case ControlScheme.Turning(_, _) =>
          List(
            Text("[_] direction (all arrow keys)", center, middle - 5, 1, GameAssets.fontKey).alignLeft,
            Text("[x] turn (left and right arrows)", center, middle + 10, 1, GameAssets.fontKey).alignLeft
          )

        case ControlScheme.Directed(_, _, _, _) =>
          List(
            Text("[x] direction (all arrow keys)", center, middle - 5, 1, GameAssets.fontKey).alignLeft,
            Text("[_] turn (left and right arrows)", center, middle + 10, 1, GameAssets.fontKey).alignLeft
          )
      }
    }

  def drawSelectText(center: Int): SceneGraphNode =
    Text("Up / Down arrows to select.", center, 205, 1, GameAssets.fontKey).alignCenter

}
