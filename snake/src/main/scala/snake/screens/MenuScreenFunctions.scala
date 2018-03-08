package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import snake.{SnakeAssets, SnakeEvent, SnakeModel, MenuScreenModel}

object MenuScreenFunctions {

  object Model {

    def update(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = {
      case KeyUp(Keys.SPACE) =>
        state.copy(currentScreen = GameScreen)

      case e @ MouseDown(_, _) =>
        state.copy(
          menuScreenModel = state.menuScreenModel.copy(
            button = state.menuScreenModel.button.update(gameTime, e)
          )
        )

      case e @ MouseUp(_, _) =>
        state.copy(
          menuScreenModel = state.menuScreenModel.copy(
            button = state.menuScreenModel.button.update(gameTime, e)
          )
        )

      case e @ MousePosition(_, _) =>
        state.copy(
          menuScreenModel = state.menuScreenModel.copy(
            button = state.menuScreenModel.button.update(gameTime, e)
          )
        )

      case _ =>
        state
    }

  }

  object View {

    def update: MenuScreenModel => SceneGraphUpdate[SnakeEvent] = model =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui(model)),
        Nil
      )

    def ui: MenuScreenModel => SceneGraphUiLayer[SnakeEvent] = model =>
      SceneGraphUiLayer[SnakeEvent](
        Text[SnakeEvent]("press space\nto start", 10, 10, 1, SnakeAssets.fontInfo).alignLeft,
        model.button.draw
      )

  }

}