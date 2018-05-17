package snake

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.events.FrameInputEvents
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import snake.screens._

object SnakeView {

  def viewUpdate: (GameTime, SnakeModel, FrameInputEvents) => SceneUpdateFragment =
    (gameTime, model, frameInputEvents) =>
      model.currentScreen match {
        case MenuScreen =>
          MenuScreenFunctions.View.update(gameTime, frameInputEvents, model)

        case GameScreen =>
          GameScreenFunctions.View.update(gameTime, model.gameScreenModel)

        case GameOverScreen =>
          GameOverScreenFunctions.View.update(model.menuScreenModel.gameViewport)
    }

}
