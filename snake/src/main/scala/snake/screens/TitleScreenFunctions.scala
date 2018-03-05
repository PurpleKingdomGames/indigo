package snake.screens

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle
import com.purplekingdomgames.indigo.gameengine.scenegraph.{SceneGraphRootNode, SceneGraphUiLayer, SceneGraphUpdate, Text}
import com.purplekingdomgames.indigo.gameengine.{GameEvent, KeyUp}
import snake.{ButtonAssets, SnakeAssets, SnakeEvent, SnakeModel}

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

object Button {

  object Model {

    def update(): Unit = ???

  }

  object View {

    def update(): Unit = ???

  }

}

case class ButtonModel(bounds: Rectangle, state: ButtonState, assets: ButtonAssets)

sealed trait ButtonState
object ButtonState {

  case object Up extends ButtonState
  case object Over extends ButtonState
  case object Down extends ButtonState

}