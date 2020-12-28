package snake.scenes

import indigo._
import indigo.scenes._

import snake.init.GameAssets
import snake.model.{SnakeGameModel, SnakeViewModel}
import snake.init.SnakeStartupData
import snake.GameReset

object StartScene extends Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel] {
  type SceneModel     = Unit
  type SceneViewModel = Unit

  val name: SceneName =
    SceneName("start")

  val modelLens: Lens[SnakeGameModel, Unit] =
    Lens.unit

  val viewModelLens: Lens[SnakeViewModel, Unit] =
    Lens.unit

  val eventFilters: EventFilters =
    EventFilters.Restricted
      .withViewModelFilter(_ => None)

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SnakeStartupData],
      snakeGameModel: Unit
  ): GlobalEvent => Outcome[Unit] = {
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(snakeGameModel)
        .addGlobalEvents(
          GameReset,
          SceneEvent.JumpTo(ControlsScene.name)
        )

    case _ =>
      Outcome(snakeGameModel)
  }

  def updateViewModel(
      context: FrameContext[SnakeStartupData],
      snakeGameModel: Unit,
      snakeViewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(snakeViewModel)

  def present(
      context: FrameContext[SnakeStartupData],
      snakeGameModel: Unit,
      snakeViewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome {
      val horizontalCenter: Int = context.startUpData.viewConfig.horizontalCenter
      val verticalMiddle: Int   = context.startUpData.viewConfig.verticalMiddle

      SceneUpdateFragment.empty
        .addUiLayerNodes(drawTitleText(horizontalCenter, verticalMiddle))
        .addUiLayerNodes(SharedElements.drawHitSpaceToStart(horizontalCenter, Seconds(1), context.gameTime))
        .withAudio(
          SceneAudio(
            SceneAudioSource(
              BindingKey("intro music"),
              PlaybackPattern.SingleTrackLoop(
                Track(GameAssets.soundIntro)
              )
            )
          )
        )
    }

  def drawTitleText(center: Int, middle: Int): List[SceneGraphNode] =
    List(
      Text("snake!", center, middle - 20, 1, GameAssets.fontKey).alignCenter,
      Text("presented in glorious 1 bit graphics", center, middle - 5, 1, GameAssets.fontKey).alignCenter,
      Text("Made by Dave", center, middle + 10, 1, GameAssets.fontKey).alignCenter
    )
}
