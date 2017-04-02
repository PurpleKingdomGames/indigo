package com.example.perf

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{FontChar, FontInfo, Point}
import com.purplekingdomgames.indigo.gameengine.{FrameInputEvents, GameTime, GlobalSignals}

import scala.util.Random

object PerfView {

  def updateView(gameTime: GameTime, model: MyGameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate[MyViewEventDataType] = {
    frameInputEvents.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + position)
      case None => ()
    }
    
    SceneGraphUpdate(
      SceneGraphRootNode(
        game = gameLayer(model),
        lighting = lightingLayer(model),
        ui = uiLayer(frameInputEvents, model)
      ),
      Nil
    )
  }

  private val herdCount: Int = 250

  private val positions: List[Point] =
    (1 to herdCount).toList.map { _ =>
      Point((Random.nextFloat() * PerfGame.viewportWidth).toInt, (Random.nextFloat() * PerfGame.viewportHeight).toInt)
    }

  private val theHerd: MyGameModel => List[Sprite[MyViewEventDataType]] = currentState =>
    positions.map { pt =>
      currentState.dude.dude.sprite
        .moveTo(pt)
        .withDepth(herdCount - pt.y)
    }

  def gameLayer(currentState: MyGameModel): SceneGraphGameLayer =
    SceneGraphGameLayer(
      SceneGraphNodeBranch(
        List(
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
          }
        ) ++ theHerd(currentState)
      )
    )

  def lightingLayer(currentState: MyGameModel): SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      Graphic(0, 0, 320, 240, 1, PerfAssets.light).withTint(1, 0, 0),
      Graphic(-115, -100, 320, 240, 1, PerfAssets.light),
      Graphic(GlobalSignals.MousePosition.x - 160, GlobalSignals.MousePosition.y - 120, 320, 240, 1, PerfAssets.light)
    ).withAmbientLightAmount(0.5).withAmbientLightTint(1, 1, 0)

  private val fontInfo: FontInfo =
    FontInfo(PerfAssets.smallFontName, 320, 230, FontChar("?", 93, 52, 23, 23))
      .addChar(FontChar("A", 3, 78, 23, 23))
      .addChar(FontChar("B", 26, 78, 23, 23))
      .addChar(FontChar("C", 50, 78, 23, 23))
      .addChar(FontChar("D", 73, 78, 23, 23))
      .addChar(FontChar("E", 96, 78, 23, 23))
      .addChar(FontChar("F", 119, 78, 23, 23))
      .addChar(FontChar("G", 142, 78, 23, 23))
      .addChar(FontChar("H", 165, 78, 23, 23))
      .addChar(FontChar("I", 188, 78, 15, 23))
      .addChar(FontChar("J", 202, 78, 23, 23))
      .addChar(FontChar("K", 225, 78, 23, 23))
      .addChar(FontChar("L", 248, 78, 23, 23))
      .addChar(FontChar("M", 271, 78, 23, 23))
      .addChar(FontChar("N", 3, 104, 23, 23))
      .addChar(FontChar("O", 29, 104, 23, 23))
      .addChar(FontChar("P", 54, 104, 23, 23))
      .addChar(FontChar("Q", 75, 104, 23, 23))
      .addChar(FontChar("R", 101, 104, 23, 23))
      .addChar(FontChar("S", 124, 104, 23, 23))
      .addChar(FontChar("T", 148, 104, 23, 23))
      .addChar(FontChar("U", 173, 104, 23, 23))
      .addChar(FontChar("V", 197, 104, 23, 23))
      .addChar(FontChar("W", 220, 104, 23, 23))
      .addChar(FontChar("X", 248, 104, 23, 23))
      .addChar(FontChar("Y", 271, 104, 23, 23))
      .addChar(FontChar("Z", 297, 104, 23, 23))
      .addChar(FontChar("0", 3, 26, 23, 23))
      .addChar(FontChar("1", 26, 26, 15, 23))
      .addChar(FontChar("2", 41, 26, 23, 23))
      .addChar(FontChar("3", 64, 26, 23, 23))
      .addChar(FontChar("4", 87, 26, 23, 23))
      .addChar(FontChar("5", 110, 26, 23, 23))
      .addChar(FontChar("6", 133, 26, 23, 23))
      .addChar(FontChar("7", 156, 26, 23, 23))
      .addChar(FontChar("8", 179, 26, 23, 23))
      .addChar(FontChar("9", 202, 26, 23, 23))
      .addChar(FontChar("?", 93, 52, 23, 23))
      .addChar(FontChar("!", 3, 0, 15, 23))
      .addChar(FontChar(".", 286, 0, 15, 23))
      .addChar(FontChar(",", 248, 0, 15, 23))
      .addChar(FontChar(" ", 145, 52, 23, 23))

  def uiLayer(frameInputEvents: FrameInputEvents, currentState: MyGameModel): SceneGraphUiLayer =
    SceneGraphUiLayer(
      Text((herdCount + 1) + " Naked\ndudes", 10, 10, 5, fontInfo).alignLeft,
      Text("Thundering Herd!", PerfGame.viewportWidth / 2, 10, 5, fontInfo).alignCenter,
      Text("use arrow\nkeys", PerfGame.viewportWidth - 10, 10, 5, fontInfo).alignRight
    )

}
