package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
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

    def update: () => SceneGraphUpdate[SnakeEvent] = () =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui()),
        Nil
      )

    def ui: () => SceneGraphUiLayer[SnakeEvent] = () =>
      SceneGraphUiLayer[SnakeEvent](
        Text[SnakeEvent]("Game over!\nPress SPACE\nto try again", 10, 10, 1, SnakeAssets.fontInfo).alignLeft
      )

  }

}