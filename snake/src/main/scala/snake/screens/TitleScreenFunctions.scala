package snake.screens

import com.purplekingdomgames.indigo.gameengine.scenegraph.{SceneGraphRootNode, SceneGraphUiLayer, SceneGraphUpdate, Text}
import com.purplekingdomgames.indigo.gameengine.{GameEvent, KeyUp}
import snake.{SnakeAssets, SnakeEvent, SnakeModel}

object TitleScreenFunctions {

  object Model {

    def update(state: SnakeModel): GameEvent => SnakeModel = {
      case KeyUp(_) =>
        state.copy(currentScreen = GameScreen)

      case _ =>
        state
    }

  }

  object View {

    def update: () => SceneGraphUpdate[SnakeEvent] = () =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui),
        Nil
      )

    def ui: SceneGraphUiLayer[SnakeEvent] =
      SceneGraphUiLayer[SnakeEvent](
        Text[SnakeEvent]("press any key\nto start", 10, 10, 1, SnakeAssets.fontInfo).alignLeft
      )

  }

}
