package snake.screens

import com.purplekingdomgames.indigo.gameengine.ViewEvent

sealed trait Screen
case object MenuScreen extends Screen
case object GameScreen extends Screen
case object GameOverScreen extends Screen

case class ChangeScreenTo(screen: Screen) extends ViewEvent