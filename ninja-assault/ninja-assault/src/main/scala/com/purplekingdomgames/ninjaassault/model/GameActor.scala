package com.purplekingdomgames.ninjaassault.model

sealed trait GameActor {
  val row: Int
  val column: Int
  val hasFocus: Boolean
}
case class Ninja(row: Int, column: Int, hasFocus: Boolean) extends GameActor
case class Guard(row: Int, column: Int, hasFocus: Boolean) extends GameActor
