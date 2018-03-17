package snake.screens

import com.purplekingdomgames.indigo.gameengine.constants.Keys
import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, KeyboardEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.shared.GameViewport
import snake._

object GameOverScreenFunctions {

  object Model {

    def update(state: SnakeModel): GameEvent => SnakeModel = {
      case KeyboardEvent.KeyUp(Keys.SPACE) =>
        state.copy(
          currentScreen = MenuScreen,
          menuScreenModel = MenuScreenFunctions.Model.initialModel(state.startupData),
          gameScreenModel = GameScreenFunctions.Model.initialModel(state.startupData)
        )

      case _ =>
        state
    }

  }

  object View {

    def update: GameViewport => SceneGraphUpdate = viewport =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui(viewport)),
        Nil
      )

    def ui: GameViewport => SceneGraphUiLayer = viewport =>
      SceneGraphUiLayer(
        Text("Game over!\nPress SPACE!", viewport.width / 2, (viewport.height / 2) - 30, 1, SnakeAssets.fontInfo).alignCenter
      )

  }

}