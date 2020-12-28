package snake.scenes

import indigo._
import indigo.scenes._
import snake.model.{ControlScheme, ViewModel}
import snake.init.{GameAssets, SnakeStartupData}
import snake.model.GameModel

object ControlsScene extends Scene[SnakeStartupData, GameModel, ViewModel] {
  type SceneModel     = ControlScheme
  type SceneViewModel = Unit

  val name: SceneName =
    SceneName("controls")

  val modelLens: Lens[GameModel, ControlScheme] =
    Lens(_.controlScheme, (m, c) => m.copy(controlScheme = c))

  val viewModelLens: Lens[ViewModel, Unit] =
    Lens.unit

  val eventFilters: EventFilters =
    EventFilters.Restricted
      .withViewModelFilter(_ => None)

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SnakeStartupData],
      controlScheme: ControlScheme
  ): GlobalEvent => Outcome[ControlScheme] = {
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(controlScheme)
        .addGlobalEvents(SceneEvent.JumpTo(GameScene.name))

    case KeyboardEvent.KeyUp(Key.UP_ARROW) | KeyboardEvent.KeyUp(Key.DOWN_ARROW) =>
      Outcome(controlScheme.swap)

    case _ =>
      Outcome(controlScheme)
  }

  def updateViewModel(
      context: FrameContext[SnakeStartupData],
      controlScheme: ControlScheme,
      sceneViewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(sceneViewModel)

  def present(
      context: FrameContext[SnakeStartupData],
      sceneModel: ControlScheme,
      sceneViewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome {
      val horizontalCenter: Int = context.startUpData.viewConfig.horizontalCenter
      val verticalMiddle: Int   = context.startUpData.viewConfig.verticalMiddle

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
