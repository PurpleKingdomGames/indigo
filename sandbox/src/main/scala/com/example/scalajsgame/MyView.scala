package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{FontChar, FontInfo, Point}

object MyView {

  def updateView(currentState: MyGameModel): SceneGraphRootNode =
    SceneGraphRootNode(
      game = gameLayer(currentState),
      lighting = lightingLayer(currentState),
      ui = uiLayer(currentState)
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
        FontInfo(MyAssets.smallFontName, 320, 230, FontChar("A", 23, 23, 3, 78))
          .addChar(FontChar("B", 23, 23, 26, 78))
          .addChar(FontChar("C", 23, 23, 50, 78))
      )
    )

}
