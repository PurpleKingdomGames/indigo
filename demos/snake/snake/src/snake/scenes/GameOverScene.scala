package snake.scenes

import indigo._
import indigo.scenes._
import snake.model.ViewModel
import snake.init.{GameAssets, StartupData}
import snake.model.GameModel

object GameOverScene extends Scene[StartupData, GameModel, ViewModel] {
  type SceneModel     = Int
  type SceneViewModel = Unit

  val name: SceneName =
    SceneName("game over")

  val modelLens: Lens[GameModel, Int] =
    Lens.readOnly(_.score)

  val viewModelLens: Lens[ViewModel, Unit] =
    Lens.unit

  val eventFilters: EventFilters =
    EventFilters.Restricted
      .withViewModelFilter(_ => None)

  val subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[StartupData], pointsScored: Int): GlobalEvent => Outcome[Int] = {
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(pointsScored)
        .addGlobalEvents(SceneEvent.JumpTo(StartScene.name))

    case _ =>
      Outcome(pointsScored)
  }

  def updateViewModel(
      context: FrameContext[StartupData],
      pointsScored: Int,
      sceneViewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(sceneViewModel)

  def present(
      context: FrameContext[StartupData],
      pointsScored: Int,
      sceneViewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome {
      val horizontalCenter: Int = context.startUpData.viewConfig.horizontalCenter
      val verticalMiddle: Int   = context.startUpData.viewConfig.verticalMiddle

      SceneUpdateFragment.empty
        .addLayer(
          Layer(BindingKey("ui"))(
            Text("Game Over!", horizontalCenter, verticalMiddle - 20, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignCenter,
            Text(s"You scored: ${pointsScored.toString()} pts!", horizontalCenter, verticalMiddle - 5, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignCenter,
            Text("(hit space to restart)", horizontalCenter, 220, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignCenter
          )
        )
    }
}
