package snake

import com.purplekingdomgames.indigo.gameengine.{FrameInputEvents, GameTime}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import snake.screens.{GameScreen, GameScreenFunctions, TitleScreen, TitleScreenFunctions}

object SnakeView {

  def viewUpdate: (GameTime, SnakeModel, FrameInputEvents) => SceneGraphUpdate[SnakeEvent] = (_, model, _) =>
    model.currentScreen match {
      case TitleScreen =>
        TitleScreenFunctions.View.update(model.titleScreenModel)

      case GameScreen =>
        GameScreenFunctions.View.update(model.gameScreenModel)
    }

}
