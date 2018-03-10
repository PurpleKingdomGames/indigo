package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle
import snake.{MenuScreenModel, SnakeAssets, SnakeEvent, SnakeModel}

object MenuScreenFunctions {

  object Model {

    def update(state: SnakeModel): GameEvent => SnakeModel = {
      case KeyUp(Keys.SPACE) =>
        state.copy(currentScreen = GameScreen)

      case _ =>
        state
    }

  }

  object View {

    def update: (GameTime, FrameInputEvents, MenuScreenModel) => SceneGraphUpdate[SnakeEvent] = (gameTime, frameEvents, model) =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui(gameTime, frameEvents, model)),
        Nil
      )

    def ui(gameTime: GameTime, frameEvents: FrameInputEvents, model: MenuScreenModel): SceneGraphUiLayer[SnakeEvent] =
      SceneGraphUiLayer[SnakeEvent](
        Text[SnakeEvent]("press space to start", model.gameViewport.width / 2, model.gameViewport.height - 30, 1, SnakeAssets.fontInfo).alignCenter,
      ).addChildren {
        model.menuItems.zipWithIndex.flatMap { case (menuItem, i) =>
          List(
            menuItem.button.draw(Rectangle(10, (i * 20) + 10, 16, 16), gameTime, frameEvents),
            Text[SnakeEvent](menuItem.text, 40, (i * 20) + 10, 1, SnakeAssets.fontInfo).alignLeft
          )
        }
      }

  }

}