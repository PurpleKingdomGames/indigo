package snake.scenes

import indigo.*
import indigo.scenes.*
import snake.model.{ControlScheme, ViewModel}
import snake.init.{GameAssets, StartupData}
import snake.model.GameModel

object ControlsScene extends Scene[StartupData, GameModel, ViewModel]:
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
      context: SceneContext[StartupData],
      controlScheme: ControlScheme
  ): GlobalEvent => Outcome[ControlScheme] =
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(controlScheme)
        .addGlobalEvents(SceneEvent.JumpTo(GameScene.name))

    case KeyboardEvent.KeyUp(Key.UP_ARROW) | KeyboardEvent.KeyUp(Key.DOWN_ARROW) =>
      Outcome(controlScheme.swap)

    case _ =>
      Outcome(controlScheme)

  def updateViewModel(
      context: SceneContext[StartupData],
      controlScheme: ControlScheme,
      sceneViewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(sceneViewModel)

  def present(
      context: SceneContext[StartupData],
      sceneModel: ControlScheme,
      sceneViewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome {
      val horizontalCenter: Int = context.startUpData.viewConfig.horizontalCenter
      val verticalMiddle: Int   = context.startUpData.viewConfig.verticalMiddle

      SceneUpdateFragment.empty
        .addLayer(
          Layer(
            BindingKey("ui"),
            drawControlsText(24, verticalMiddle, sceneModel) ++
              Batch(drawSelectText(horizontalCenter)) ++
              SharedElements.drawHitSpaceToStart(horizontalCenter, Seconds(1), context.gameTime)
          )
        )
    }

  def drawControlsText(center: Int, middle: Int, controlScheme: ControlScheme): Batch[SceneNode] =
    Batch(
      Text("select controls", center, middle - 20, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignLeft
    ) ++ {
      controlScheme match
        case ControlScheme.Turning(_, _) =>
          Batch(
            Text("[_] direction (all arrow keys)", center, middle - 5, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignLeft,
            Text(
              "[x] turn (left and right arrows)",
              center,
              middle + 10,
              1,
              GameAssets.fontKey,
              GameAssets.fontMaterial
            ).alignLeft
          )

        case ControlScheme.Directed(_, _, _, _) =>
          Batch(
            Text("[x] direction (all arrow keys)", center, middle - 5, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignLeft,
            Text(
              "[_] turn (left and right arrows)",
              center,
              middle + 10,
              1,
              GameAssets.fontKey,
              GameAssets.fontMaterial
            ).alignLeft
          )

    }

  def drawSelectText(center: Int): SceneNode =
    Text("Up / Down arrows to select.", center, 205, 1, GameAssets.fontKey, GameAssets.fontMaterial).alignCenter
