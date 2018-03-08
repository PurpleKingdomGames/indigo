package snake.screens

sealed trait Screen
case object MenuScreen extends Screen
case object GameScreen extends Screen
case object GameOverScreen extends Screen
