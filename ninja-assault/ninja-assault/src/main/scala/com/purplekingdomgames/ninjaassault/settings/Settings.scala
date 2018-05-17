package com.purplekingdomgames.ninjaassault.settings

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle
import com.purplekingdomgames.shared.{ClearColor, GameConfig, GameViewport}

object Settings {

  val gameSetup: GameConfig = GameConfig(
    viewport = GameViewport(800, 600),
    frameRate = 30,
    clearColor = ClearColor.Black,
    magnification = 2
  )

  val activeScreenWidth: Int      = gameSetup.viewport.width / gameSetup.magnification
  val activeScreenHeight: Int     = gameSetup.viewport.height / gameSetup.magnification
  val activeHorizontalCenter: Int = activeScreenWidth / 2
  val activeVerticalCenter: Int   = activeScreenHeight / 2
  val screenArea: Rectangle       = Rectangle(0, 0, gameSetup.viewport.width, gameSetup.viewport.height)
  val activeScreenArea: Rectangle = Rectangle(0, 0, activeScreenWidth, activeScreenHeight)

}
