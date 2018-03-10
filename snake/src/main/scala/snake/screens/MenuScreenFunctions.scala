package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle
import com.purplekingdomgames.indigoat.ui.Button
import snake.{MenuScreenModel, SnakeAssets, SnakeModel}

object MenuScreenFunctions {

  object Model {

    def update(state: SnakeModel): GameEvent => SnakeModel = {
      case KeyUp(Keys.SPACE) =>
        state.copy(currentScreen = GameScreen)

      case e: ViewEvent =>
        state.copy(menuScreenModel =
          state.menuScreenModel.copy(menuItems =
            state.menuScreenModel.menuItems.map(mi => mi.copy(
              button = Button.Model.update(mi.button, e)
            ))
          )
        )

      case _ =>
        state
    }

  }

  object View {

    def update: (GameTime, FrameInputEvents, MenuScreenModel) => SceneGraphUpdate = (gameTime, frameEvents, model) =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(ui(gameTime, frameEvents, model)),
        Nil
      )

    //TODO: Buttons need to emit an event into the SceneGraphUpdate above
    def ui(gameTime: GameTime, frameEvents: FrameInputEvents, model: MenuScreenModel): SceneGraphUiLayer =
      SceneGraphUiLayer(
        Text("press space to start", model.gameViewport.width / 2, model.gameViewport.height - 30, 1, SnakeAssets.fontInfo).alignCenter,
      ).addChildren {
        model.menuItems.zipWithIndex.flatMap { case (menuItem, i) =>
          List(
            menuItem.button.draw(Rectangle(10, (i * 20) + 10, 16, 16), gameTime, frameEvents),
            Text(menuItem.text, 40, (i * 20) + 10, 1, SnakeAssets.fontInfo).alignLeft
          )
        }
      }

  }

}