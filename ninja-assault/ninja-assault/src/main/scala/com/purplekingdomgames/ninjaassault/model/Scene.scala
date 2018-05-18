package com.purplekingdomgames.ninjaassault.model

sealed trait Scene extends Product with Serializable {
  val active: Boolean
}
object Scene {
  case class Logo(active: Boolean) extends Scene
  case class Menu(active: Boolean) extends Scene
  case class Game(active: Boolean) extends Scene
}
