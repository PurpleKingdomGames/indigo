package snake.screens

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Depth, Rectangle}
import com.purplekingdomgames.indigoat.ui.{Button, ButtonAssets, ButtonState}
import snake._

object MenuScreenFunctions {

  object Model {

    def initialModel(startupData: SnakeStartupData): MenuScreenModel =
      MenuScreenModel(
        gameViewport = startupData.viewport,
        menuItems =
          MenuZipper(
            previous = Nil,
            current = MenuItem("demo mode", makeButton(startupData, MenuScreen), MenuScreen),
            next =
              List(
                MenuItem("1up", makeButton(startupData, GameScreen), GameScreen),
                MenuItem("1up vs cpu", makeButton(startupData, MenuScreen), MenuScreen),
                MenuItem("2up local", makeButton(startupData, MenuScreen), MenuScreen),
                MenuItem("2up network", makeButton(startupData, MenuScreen), MenuScreen)
              )
          )
      )

    private def makeButton(startupData: SnakeStartupData, screen: Screen): Button =
      Button(
        ButtonState.Up,
        ButtonAssets(
          up = startupData.staticAssets.gameScreen.player1.alive,
          over = startupData.staticAssets.gameScreen.player2.alive,
          down = startupData.staticAssets.gameScreen.player3.alive
        )
      ).withUpAction { () =>
        Option(ChangeScreenTo(screen))
      }

    def update(state: SnakeModel): GameEvent => SnakeModel = {
      case KeyUp(Keys.SPACE) =>
        state.copy(
          currentScreen = GameScreen,
          menuScreenModel = MenuScreenFunctions.Model.initialModel(state.startupData),
          gameScreenModel = GameScreenFunctions.Model.initialModel(state.startupData)
        )

      case KeyDown(Keys.UP_ARROW) =>
        state.copy(
          menuScreenModel = state.menuScreenModel.copy(
            menuItems = state.menuScreenModel.menuItems.back
          )
        )

      case KeyDown(Keys.DOWN_ARROW) =>
        state.copy(
          menuScreenModel = state.menuScreenModel.copy(
            menuItems = state.menuScreenModel.menuItems.forward
          )
        )

      case KeyUp(Keys.ENTER) =>
        state.copy(currentScreen = state.menuScreenModel.menuItems.current.goToScreen)

      case ChangeScreenTo(screen) =>
        state.copy(
          currentScreen = screen,
          menuScreenModel = MenuScreenFunctions.Model.initialModel(state.startupData),
          gameScreenModel = GameScreenFunctions.Model.initialModel(state.startupData)
        )

      case e: ViewEvent =>
        state.copy(menuScreenModel =
          state.menuScreenModel.copy(menuItems =
            state.menuScreenModel.menuItems.copy(
              previous = state.menuScreenModel.menuItems.previous.map(mi => mi.copy(
                button = Button.Model.update(mi.button, e)
              )),
              current = state.menuScreenModel.menuItems.current.copy(
                button = Button.Model.update(state.menuScreenModel.menuItems.current.button, e)
              ),
              next = state.menuScreenModel.menuItems.next.map(mi => mi.copy(
                button = Button.Model.update(mi.button, e)
              ))
            )
          )
        )

      case _ =>
        state
    }

  }

  object View {

    def update: (GameTime, FrameInputEvents, MenuScreenModel) => SceneGraphUpdate = (_, frameEvents, model) => {

      val uiLayer = ui(frameEvents, model)

      SceneGraphUpdate(
        SceneGraphRootNode.empty.addUiLayer(uiLayer._1),
        uiLayer._2
      )
    }

    def ui(frameEvents: FrameInputEvents, model: MenuScreenModel): (SceneGraphUiLayer, List[ViewEvent]) = {

      val menuItemsAndEvents: List[(SceneGraphNode, List[ViewEvent])] = {

        val draw: Boolean => ((MenuItem, Int)) => List[(SceneGraphNode, List[ViewEvent])] = p => { case (menuItem, i) =>
          List(
            menuItem.button.draw(Rectangle(10, (i * 20) + 10, 16, 16), Depth(2), frameEvents).toTuple,
            (Text(menuItem.text, 40, (i * 20) + 10, 2, SnakeAssets.fontInfo).alignLeft.withAlpha(if (p) 1 else 0.5), Nil)
          )
        }

        val p = model.menuItems.previous.reverse.zipWithIndex.flatMap(draw(false))
        val c = List((model.menuItems.current, model.menuItems.positionOfCurrent)).flatMap(draw(true))
        val n = model.menuItems.next.zipWithIndex.map(p => (p._1, p._2 + model.menuItems.positionOfCurrent + 1)).flatMap(draw(false))

        p ++ c ++ n
      }

      val uiLayer = SceneGraphUiLayer(
        Text("press space to start", model.gameViewport.width / 2, model.gameViewport.height - 30, 2, SnakeAssets.fontInfo).alignCenter
      ).addChildren(menuItemsAndEvents.map(_._1))

      (uiLayer, menuItemsAndEvents.flatMap(_._2))
    }

  }

}