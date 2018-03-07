package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import snake.{SnakeAssets, SnakeEvent, SnakeModel, TitleScreenModel}

object TitleScreenFunctions {

  object Model {

    def update(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = {
      case KeyUp(_) =>
        state.copy(currentScreen = GameScreen)

      case e @ MouseDown(_, _) =>
        state.copy(
          titleScreenModel = state.titleScreenModel.copy(
            button = state.titleScreenModel.button.update(gameTime, e)
          )
        )

      case e @ MouseUp(_, _) =>
        state.copy(
          titleScreenModel = state.titleScreenModel.copy(
            button = state.titleScreenModel.button.update(gameTime, e)
          )
        )

      case e @ MousePosition(_, _) =>
        state.copy(
          titleScreenModel = state.titleScreenModel.copy(
            button = state.titleScreenModel.button.update(gameTime, e)
          )
        )

      case _ =>
        state
    }

  }

  object View {

    def update: TitleScreenModel => SceneGraphUpdate[SnakeEvent] = model =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui(model)),
        Nil
      )

    def ui: TitleScreenModel => SceneGraphUiLayer[SnakeEvent] = model =>
      SceneGraphUiLayer[SnakeEvent](
        Text[SnakeEvent]("press any key\nto start", 10, 10, 1, SnakeAssets.fontInfo).alignLeft,
        model.button.draw
      )

  }

}