package snake.screens

import com.purplekingdomgames.indigo.gameengine.scenegraph.{SceneGraphRootNode, SceneGraphUpdate}
import snake.{SnakeEvent, TitleScreenModel}

object TitleScreenFunctions {

  object Model {

  }

  object View {

    def update: TitleScreenModel => SceneGraphUpdate[SnakeEvent] = _ =>
      SceneGraphUpdate(
        SceneGraphRootNode.empty,
        Nil
      )

  }

}
