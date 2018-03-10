package snake

import com.purplekingdomgames.indigo.gameengine.{FrameInputEvents, GameTime}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import snake.screens._

object SnakeView {

  def viewUpdate: (GameTime, SnakeModel, FrameInputEvents) => SceneGraphUpdate[SnakeEvent] = (gameTime, model, frameInputEvents) =>
    model.currentScreen match {
      case MenuScreen =>
        MenuScreenFunctions.View.update(gameTime, frameInputEvents, model.menuScreenModel)

      case GameScreen =>
        GameScreenFunctions.View.update(model.gameScreenModel)

      case GameOverScreen =>
        GameOverScreenFunctions.View.update(model.menuScreenModel.gameViewport)
    }

}
