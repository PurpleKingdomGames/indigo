package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.{GameEvent, GameTime, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{FontChar, FontInfo}

object MyView {

  def updateView(gameTime: GameTime, gameEvents: List[GameEvent], model: MyGameModel): (SceneGraphRootNode, List[ViewEvent[MyViewEventDataType]]) =
    (
      SceneGraphRootNode(
        game = gameLayer(model),
        lighting = lightingLayer(model),
        ui = uiLayer(model)
      ),
      Nil
    )

  def gameLayer(currentState: MyGameModel): SceneGraphGameLayer =
    SceneGraphGameLayer(
      SceneGraphNodeBranch(
        currentState.dude.walkDirection match {
          case d@DudeLeft =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeRight =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeUp =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeDown =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeIdle =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()
        },
        currentState.dude.dude.sprite.moveBy(8, 10)
      )
    )

  def lightingLayer(currentState: MyGameModel): SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      Graphic(0, 0, 320, 240, 1, MyAssets.light).withTint(1, 0, 0),
      Graphic(-115, -100, 320, 240, 1, MyAssets.light)
    ).withAmbientLightAmount(0.5).withAmbientLightTint(1, 1, 0)

  def uiLayer(currentState: MyGameModel): SceneGraphUiLayer =
    SceneGraphUiLayer(
      Text("ABC", 2, 2, 5,
        FontInfo(MyAssets.smallFontName, 320, 230, FontChar("A", 3, 78, 23, 23))
          .addChar(FontChar("B", 26, 78, 23, 23))
          .addChar(FontChar("C", 50, 78, 23, 23))
      )
    )

}
