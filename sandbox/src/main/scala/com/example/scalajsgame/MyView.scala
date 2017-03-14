package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{FontChar, FontInfo, Point}

object MyView {

  def updateView(currentState: MyGameModel): SceneGraphRootNode =
    SceneGraphRootNode(
      game = gameLayer(currentState),
      lighting = SceneGraphLightingLayer.empty,//lightingLayer(currentState),
      ui = SceneGraphUiLayer.empty//uiLayer(currentState)
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
      Graphic(0, 0, 320, 240, 1, MyAssets.light),//.withTint(0, 0, 1),//.withAlpha(0.5),
      Graphic(-115, -100, 320, 240, 1, MyAssets.light)//.withTint(0.5, 1, 0.5)
    ).withAmbientLightAmount(0.8)//.withAmbientLightTint(1, 0, 0)

  def uiLayer(currentState: MyGameModel): SceneGraphUiLayer =
    SceneGraphUiLayer(
      Text("ABC", 2, 2, 5,
        FontInfo(23, 23, MyAssets.smallFontName, 320, 230, FontChar("A", 3, 78))
          .addChar(FontChar("B", Point(26, 78)))
          .addChar(FontChar("C", Point(50, 78)))
      )
    )

}
