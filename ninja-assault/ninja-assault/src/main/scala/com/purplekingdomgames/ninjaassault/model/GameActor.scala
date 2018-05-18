package com.purplekingdomgames.ninjaassault.model

sealed trait GameActor {
  val row: Int
  val column: Int
}
case class Ninja(row: Int, column: Int) extends GameActor
case class Guard(row: Int, column: Int) extends GameActor
