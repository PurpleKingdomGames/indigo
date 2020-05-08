package snake.scenes

import indigo._
import indigoexts.lenses.Lens
import indigoexts.scenes._

import snake.init.{GameAssets, Settings}
import snake.model.{SnakeGameModel, SnakeViewModel}
import indigoexts.subsystems.SubSystem

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

  def updateSceneModel(gameTime: GameTime, snakeGameModel: SnakeGameModel, inputState: InputState, dice: Dice): GlobalEvent => Outcome[SnakeGameModel] = {
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(snakeGameModel.reset)
        .addGlobalEvents(SceneEvent.JumpTo(ControlsScene.name))

    case _ =>
      Outcome(snakeGameModel)
  }

  def updateSceneViewModel(
      gameTime: GameTime,
      snakeGameModel: SnakeGameModel,
      snakeViewModel: SnakeViewModel,
      inputState: InputState,
      dice: Dice
  ): Outcome[SnakeViewModel] =
    Outcome(snakeViewModel)

  def updateSceneView(
      gameTime: GameTime,
      snakeGameModel: SnakeGameModel,
      snakeViewModel: SnakeViewModel,
      inputState: InputState
  ): SceneUpdateFragment = {
    val horizontalCenter: Int = (Settings.viewportWidth / Settings.magnificationLevel) / 2
    val verticalMiddle: Int   = (Settings.viewportHeight / Settings.magnificationLevel) / 2

    SceneUpdateFragment.empty
      .addUiLayerNodes(drawTitleText(horizontalCenter, verticalMiddle))
      .addUiLayerNodes(SharedElements.drawHitSpaceToStart(horizontalCenter, Seconds(1), gameTime))
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
