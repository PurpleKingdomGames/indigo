package snake.scenes

import indigo._
import indigo.scenes._

import snake.init.{GameAssets, Settings}
import snake.model.{SnakeGameModel, SnakeViewModel}

object StartScene extends Scene[SnakeGameModel, SnakeViewModel] {
  type SceneModel     = SnakeGameModel
  type SceneViewModel = SnakeViewModel

  val name: SceneName = SceneName("start")

  val sceneModelLens: Lens[SnakeGameModel, SnakeGameModel] =
    Lens.keepLatest

  val sceneViewModelLens: Lens[SnakeViewModel, SnakeViewModel] =
    Lens.keepLatest

  val sceneSubSystems: Set[SubSystem] =
    Set()

  def updateSceneModel(context: FrameContext, snakeGameModel: SnakeGameModel): GlobalEvent => Outcome[SnakeGameModel] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(snakeGameModel.reset)
        .addGlobalEvents(SceneEvent.JumpTo(ControlsScene.name))

    case _ =>
      Outcome(snakeGameModel)
  }

  def updateSceneViewModel(
      context: FrameContext,
      snakeGameModel: SnakeGameModel,
      snakeViewModel: SnakeViewModel
  ): Outcome[SnakeViewModel] =
    Outcome(snakeViewModel)

  def updateSceneView(
      context: FrameContext,
      snakeGameModel: SnakeGameModel,
      snakeViewModel: SnakeViewModel
  ): SceneUpdateFragment = {
    val horizontalCenter: Int = (Settings.viewportWidth / Settings.magnificationLevel) / 2
    val verticalMiddle: Int   = (Settings.viewportHeight / Settings.magnificationLevel) / 2

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
