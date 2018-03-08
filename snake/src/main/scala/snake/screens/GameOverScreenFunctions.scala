package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.shared.GameViewport
import snake._

object GameOverScreenFunctions {

  object Model {

    def update(state: SnakeModel): GameEvent => SnakeModel = {
      case KeyUp(Keys.SPACE) =>
        state.copy(currentScreen = MenuScreen, gameScreenModel = state.gameScreenModel.reset)

      case _ =>
        state
    }

  }

  object View {

    def update: GameViewport => SceneGraphUpdate[SnakeEvent] = viewport =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui(viewport)),
        Nil
      )

    def ui: GameViewport => SceneGraphUiLayer[SnakeEvent] = viewport =>
      SceneGraphUiLayer[SnakeEvent](
        Text[SnakeEvent]("Game over!\nPress SPACE!", viewport.width / 2, (viewport.height / 2) - 30, 1, SnakeAssets.fontInfo).alignCenter
      )

  }

}